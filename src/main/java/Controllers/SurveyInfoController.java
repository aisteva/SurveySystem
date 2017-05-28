package Controllers;

import Controllers.Interfaces.ISurveyInfoController;
import DAO.Implementations.SurveyDAO;
import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.omnifaces.util.Faces;
import services.interfaces.MessageGenerator;
import services.excel.ExcelSurveyExport;
import userModule.SignInPerson;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by vdeiv on 2017-04-29.
 */
@Named
@ViewScoped
@Slf4j
public class SurveyInfoController implements ISurveyInfoController, Serializable {

    @Getter
    @Setter
    private String surveyUrl;

    @Inject
    private EntityManager em;

    @Getter
    private Survey survey;

    @Inject
    private SignInPerson signInPerson;

    @Inject
    private SurveyDAO surveyDao;

    @Inject
    private ExcelSurveyExport excelSurveyExport;

    @Getter
    private Map<Long, List<AnswerCounter>> answerCounterMap = new HashMap<>();

    @Getter
    private Map<Long, QuestionStats> questionStatsMap = new HashMap<>();

    @Inject
    private MessageGenerator mesg;

    public class QuestionStats {
        public QuestionStats(float avg, float mediana, List<Integer> modaLst, int maxModa) {
            this.avg = avg;
            this.mediana = mediana;
            this.modaLst = modaLst;
            this.modaRepeated = maxModa;
        }
        @Getter private float avg = 0;
        @Getter private float mediana = 0;
        @Getter private List<Integer> modaLst;
        @Getter private int modaRepeated;
    }

    public class AnswerCounter {
        public AnswerCounter(String answerText, int countAnswers) {
            this.answerText = answerText;
            this.countAnswers = countAnswers;
        }
        @Getter private String answerText;
        @Getter private int countAnswers;
        @Getter @Setter private String percentage;

        public void addToCountAnswers() {
            countAnswers++;
        }
    }

    private void addOnlyUnique(List<Answer> lst, List<AnswerCounter> rez) {
        Set<String> texts = new HashSet<>();
        for (Answer a : lst) {
            if (!a.isFinished()) {
                continue;
            }
            if (a.getText() == null || a.getText().isEmpty()) {
                mesg.redirectToErrorPage("Apklausos atidaryti neįmanoma");
            }
            if (texts.contains(a.getText())) {
                rez.stream().filter(x -> x.getAnswerText().equals(a.getText())).findFirst().get().addToCountAnswers();
            } else {
                texts.add(a.getText());
                rez.add(new AnswerCounter(a.getText(), 1));
            }
        }
    }

    private void calculateStats(Long questionId, List<AnswerCounter> answerCounterList) {
        float mediana;
        if (answerCounterList.size() == 0) return;
        if (answerCounterList.size() % 2 == 0) {
            mediana = (float) (Integer.parseInt(answerCounterList.get(answerCounterList.size() / 2 - 1).answerText) +
                    Integer.parseInt(answerCounterList.get(answerCounterList.size() / 2).answerText)) / 2;
        } else {
            mediana = Integer.parseInt(answerCounterList.get(answerCounterList.size() / 2).answerText);
        }
        float sum = 0, n = 0;
        List<Integer> modaLst = new ArrayList<>();
        int maxModa = -1;
        for (AnswerCounter ac : answerCounterList) {
            sum += Integer.parseInt(ac.getAnswerText()) * ac.countAnswers;
            n += ac.countAnswers;
            if (modaLst.isEmpty()) {
                modaLst.add(Integer.parseInt(ac.getAnswerText()));
                maxModa = ac.countAnswers;
            } else if (maxModa == ac.countAnswers) {
                modaLst.add(Integer.parseInt(ac.getAnswerText()));
            } else if (maxModa < ac.countAnswers) {
                modaLst.clear();
                modaLst.add(Integer.parseInt(ac.getAnswerText()));
                maxModa = ac.countAnswers;
            }
        }
        questionStatsMap.put(questionId, new QuestionStats(sum / n, mediana, modaLst, maxModa));
    }

    private void addToAnswerCounterMap(Question question) {
        answerCounterMap.put(question.getQuestionID(), new ArrayList<>());
        List<OfferedAnswer> offeredAnswers = question.getOfferedAnswerList();
        List<AnswerCounter> answerCounterList = new ArrayList<>();
        for (OfferedAnswer o : offeredAnswers) {
            if (question.getType().equals(Question.QUESTION_TYPE.TEXT.toString())) { // Only for text
                addOnlyUnique(o.getAnswerList(), answerCounterList);
            } else if (question.getType().equals(Question.QUESTION_TYPE.SCALE.toString())) { // Only for scale
                addOnlyUnique(o.getAnswerList(), answerCounterList);
            } else { // Checkbox or multiple
                int i = 0;
                // Add only finished
                for (Answer answer : o.getAnswerList()) {
                    if (answer.isFinished()) {
                        i++;
                    }
                }
                answerCounterList.add(new AnswerCounter(o.getText(), i));
            }
        }

        // Only for scale
        if (question.getType().equals(Question.QUESTION_TYPE.SCALE.toString())) {
            Collections.sort(answerCounterList, (x, y) -> Integer.compare(Integer.parseInt(x.answerText), Integer.parseInt(y.answerText)));
            calculateStats(question.getQuestionID(), answerCounterList);
        }
        answerCounterMap.get(question.getQuestionID()).addAll(answerCounterList);
    }

    public void load(FacesContext context, UIComponent component, Object object) throws IOException {
        survey = surveyDao.getSurveyByUrl((String) object);

        //jei useris yra ne adminas ir nekurejas, o apklausa privati, jis info matyti negali
        if (!(survey.getPersonID().equals(signInPerson.getLoggedInPerson()) || signInPerson.getLoggedInPerson().getUserType() == "ADMIN") && survey.isSurveyPrivate())
            mesg.redirectToErrorPage("Jūs neturite teisių matyti šios apklausos atsakymų");

        if (survey != null) {
            for (Question q : survey.getQuestionList()) {
                addToAnswerCounterMap(q);
                calculatePercentage(q.getQuestionID());
            }
        }
        //jei neranda apklausos, išmeta errora
        else {
            mesg.redirectToErrorPage("Tokios apklausos ataskaitos nėra");
        }
    }

    private void calculatePercentage(Long questionID) {
        long total = 0;
        for (AnswerCounter ac : answerCounterMap.get(questionID)) {
            total += ac.countAnswers;
        }
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        for (AnswerCounter ac : answerCounterMap.get(questionID)) {
            ac.setPercentage(df.format((float) ac.countAnswers / total * 100f));
        }
    }

    public void exportSurvey() {
        try {
            File file = new File("apklausa.xlsx");
            Workbook wb = excelSurveyExport.exportSurveyIntoExcelFile(survey).get();
            FileOutputStream fileOut = new FileOutputStream(file);
            wb.write(fileOut);
            fileOut.close();
            Faces.sendFile(file, true);
            file.delete();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    //Metodas ištrinantis apklausą
    @Transactional
    public void deleteSurvey() {
        Survey survey1 = surveyDao.getSurveyByUrl(survey.getSurveyURL());
        surveyDao.delete(survey1);
        mesg.redirectToSuccessPage("Apklausa sėkmingai ištrinta");

    }

}
