package controllers;

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
import services.excel.ExcelSurveyExport;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by vdeiv on 2017-04-29.
 */
@Named
@ViewScoped
@Slf4j
public class SurveyInfoController implements Serializable{

    @Getter
    @Setter
    private String surveyUrl;

    @Getter
    private Survey survey;

    @Inject
    private SurveyDAO surveyDao;

    @Inject
    private ExcelSurveyExport excelSurveyExport;

    @Getter
    private Map<Long, List<AnswerCounter>> answerCounterMap = new HashMap<>();

    @Getter
    private Map<Long, QuestionStats> questionStatsMap = new HashMap<>();

    public class QuestionStats{
        public QuestionStats(float avg, float mediana, List<Integer> modaLst, int maxModa){
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
        public AnswerCounter(String answerText, int countAnswers){
            this.answerText = answerText;
            this.countAnswers = countAnswers;
        }
        @Getter private String answerText;
        @Getter private int countAnswers;

        public void addToCountAnswers(){
            countAnswers++;
        }
    }

    private void addOnlyUnique(List<Answer> lst, List<AnswerCounter> rez){
        Set<String> texts = new HashSet<>();
        for (Answer a : lst) {
            if (texts.contains(a.getText())){
                rez.stream().filter(x -> x.getAnswerText().equals(a.getText())).findFirst().get().addToCountAnswers();
            } else {
                texts.add(a.getText());
                rez.add(new AnswerCounter(a.getText(), 1));
            }
        }
    }

    private void calculateStats(Long questionId, List<AnswerCounter> answerCounterList){
        float mediana;
        if (answerCounterList.size() % 2 == 0){
            mediana = (float)(Integer.parseInt(answerCounterList.get(answerCounterList.size()/2-1).answerText) +
                    Integer.parseInt(answerCounterList.get(answerCounterList.size()/2).answerText))/2;
        } else {
            mediana = Integer.parseInt(answerCounterList.get(answerCounterList.size()/2).answerText);
        }
        float sum =0, n = 0;
        List<Integer> modaLst = new ArrayList<>();
        int maxModa = -1;
        for (AnswerCounter ac : answerCounterList){
            sum += Integer.parseInt(ac.getAnswerText())*ac.countAnswers;
            n += ac.countAnswers;
            if (modaLst.isEmpty()){
                modaLst.add(Integer.parseInt(ac.getAnswerText()));
                maxModa = ac.countAnswers;
            } else if (maxModa == ac.countAnswers){
                modaLst.add(Integer.parseInt(ac.getAnswerText()));
            } else if (maxModa < ac.countAnswers){
                modaLst.clear();
                modaLst.add(Integer.parseInt(ac.getAnswerText()));
                maxModa = ac.countAnswers;
            }
        }
        questionStatsMap.put(questionId, new QuestionStats(sum/n, mediana, modaLst, maxModa));
    }

    private void addToAnswerCounterMap(Question question){
        answerCounterMap.put(question.getQuestionID(), new ArrayList<>());
        List<OfferedAnswer> offeredAnswers = question.getOfferedAnswerList();
        List<AnswerCounter> answerCounterList = new ArrayList<>();
        for (OfferedAnswer o : offeredAnswers){
            if (question.getType().equals(Question.QUESTION_TYPE.TEXT.toString())){ // Only for text
                addOnlyUnique(o.getAnswerList(), answerCounterList);
            }
            else if (question.getType().equals(Question.QUESTION_TYPE.SCALE.toString())){ // Only for scale
                addOnlyUnique(o.getAnswerList(), answerCounterList);
            }
            else { // Checkbox or multiple
                answerCounterList.add(new AnswerCounter(o.getText(), o.getAnswerList().size()));
            }
        }

        if (question.getType().equals(Question.QUESTION_TYPE.SCALE.toString())){ // Only for scale
            Collections.sort(answerCounterList, (x, y) -> Integer.compare(Integer.parseInt(x.answerText), Integer.parseInt(y.answerText)));
            calculateStats(question.getQuestionID(), answerCounterList);
        }
        answerCounterMap.get(question.getQuestionID()).addAll(answerCounterList);
    }

    public String load(FacesContext context, UIComponent component, Object object){
        //Long ind = Long.parseLong(surveyId);
        survey = surveyDao.getSurveyByUrl((String) object);
        if(survey!= null){
            for (Question q : survey.getQuestionList()){
                addToAnswerCounterMap(q);
            }
            return null;
        }
        else {
            return "/errorPage.xhtml?faces-redirect=true";
        }
    }

    public void exportSurvey()
    {
        try
        {
            File file = new File("apklausa.xlsx");
            Workbook wb = excelSurveyExport.exportSurveyIntoExcelFile(survey).get();
            FileOutputStream fileOut = new FileOutputStream(file);
            wb.write(fileOut);
            fileOut.close();
            Faces.sendFile(file, true);
            file.delete();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }

    }

}
