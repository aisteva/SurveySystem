package CreateFormPackage.Controllers;

import DAO.Implementations.PersonDAO;
import DAO.Implementations.SurveyDAO;
import entitiesJPA.*;
import interceptor.LogInterceptor;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;
import services.SaltGenerator;
import services.excel.IExcelSurveyImport;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by vdeiv on 2017-04-07.
 */
@ManagedBean
@ViewScoped
@Getter
@LogInterceptor
public class CreateFormController implements Serializable {

    @Inject
    private SaltGenerator sg;

    private Survey survey = new Survey();

    private int page = 1;

    @Inject
    private PersonDAO personDAO;
    @Inject
    private SurveyDAO surveyDAO;

    @Getter
    private Map<Integer, List<Question>> questions = new HashMap<>();

    private File excelFile;

    @Inject
    IExcelSurveyImport excelSurveyImport;

    @Getter @Setter
    boolean isImported;

    public CreateFormController(){
        questions.put(1, new ArrayList<>());
        addQuestion(-1);
    }

    public List<OfferedAnswer> getOfferedAnswers(final int questionIndex) {
        return questions.get(page).get(questionIndex).getOfferedAnswerList();
    }

    public void nextPage(){
        page += 1;
        if (!questions.containsKey(page)){
            questions.put(page, new ArrayList<>());
            addQuestion(-1);
        }
    }

    public void prevPage() {
        page -= 1;
    }

    public void removeQuestion(final int questionIndex) {
        questions.get(page).remove(questionIndex);
    }

    public void addQuestion(final int prevQuestionIndex) {
        Question question = new Question();
        question.setSurveyID(survey);
        String type = Question.QUESTION_TYPE.TEXT.toString();
        if (prevQuestionIndex >= 0) {
            type = questions.get(page).get(prevQuestionIndex).getType();
        };
        question.setType(type);
        question.setNewType(type);
        question.setQuestionNumber(prevQuestionIndex + 1 + 1);  // (clicked) question index + starts with 1 + next question
        question.setPage(page);
        questions.get(page).add(prevQuestionIndex+1, question);
        addOfferedAnswer(prevQuestionIndex+1);
        if (type==Question.QUESTION_TYPE.SCALE.toString()){
            addOfferedAnswer(prevQuestionIndex+1);
        }
    }

    public void addChildQuestion(final int offeredAnswerIndex, final int prevQuestionIndex) {
        addQuestion(prevQuestionIndex);
        Question question = questions.get(page).get(prevQuestionIndex+1);

        AnswerConnection answerConnection = new AnswerConnection();
        question.getAnswerConnectionList().add(answerConnection);
        answerConnection.setQuestionID(question);
        OfferedAnswer parentOfferedAnswer = getOfferedAnswers(prevQuestionIndex).get(offeredAnswerIndex);
        parentOfferedAnswer.getAnswerConnectionList().add(answerConnection);
        answerConnection.setOfferedAnswerID(parentOfferedAnswer);
    }

    public void removeAnswer(int questionIndex, final int answerIndex){
        if (questions.get(page).get(questionIndex).getOfferedAnswerList().size() > 1) {
            OfferedAnswer offeredAnswer = questions.get(page).get(questionIndex).getOfferedAnswerList().get(answerIndex);
            for (AnswerConnection answerConnection : offeredAnswer.getAnswerConnectionList()) {
                answerConnection.getQuestionID().getAnswerConnectionList().remove(answerConnection); // Deletes from question answerconnections
            }
            questions.get(page).get(questionIndex).getOfferedAnswerList().remove(offeredAnswer);
        }
    }

    public void addOfferedAnswer(final int questionIndex) {
        OfferedAnswer offeredAnswer = new OfferedAnswer();
        Question question = questions.get(page).get(questionIndex);

        offeredAnswer.setQuestionID(question);
        question.getOfferedAnswerList().add(offeredAnswer);
    }

    public void removeAllOfferedAnswers(final int questionIndex) {
        questions.get(page).get(questionIndex).getOfferedAnswerList().clear();
    }

    public void moveQuestionUp(final int questionIndex) {
        if (questionIndex != 0) {
            if (questions.get(page).get(questionIndex).getAnswerConnectionList().size() > 0 &&
                    questions.get(page).get(questionIndex).getAnswerConnectionList().get(0) // Can't be higher than parent question
                    .getOfferedAnswerID().getQuestionID().getQuestionNumber()-1 >= questionIndex-1) {
                return;
            }
            questions.get(page).get(questionIndex).setQuestionNumber(questionIndex - 1+1);
            questions.get(page).get(questionIndex - 1).setQuestionNumber(questionIndex+1);
            Collections.swap(questions.get(page), questionIndex, questionIndex - 1);
        }
    }
    public void moveQuestionDown(final int questionIndex) {
        if (questionIndex != questions.get(page).size()-1){
            if (questions.get(page).get(questionIndex).getOfferedAnswerList().size()> 0){
                for (OfferedAnswer oa : questions.get(page).get(questionIndex).getOfferedAnswerList()) {
                    if (oa.getAnswerConnectionList().size() > 0) {
                        for (AnswerConnection ac : oa.getAnswerConnectionList()) {
                            if (questionIndex + 1 >= ac.getQuestionID().getQuestionNumber() - 1) {
                                return;
                            }
                        }
                    }
                }
            }
            questions.get(page).get(questionIndex).setQuestionNumber(questionIndex + 1 + 1);
            questions.get(page).get(questionIndex + 1).setQuestionNumber(questionIndex + 1);
            Collections.swap(questions.get(page), questionIndex, questionIndex+1);
        }
    }

    public String getQuestionParentMessage(final int questionIndex){
        if (questionIndex != questions.get(page).size()) {
            Question question = questions.get(page).get(questionIndex);
            if (question.getAnswerConnectionList().size() > 0){
                return "Jeigu prieš tai buvo atsakyta "+ question.getAnswerConnectionList().get(0).getOfferedAnswerID().getText();
            }else{
                return "";
            }
        }
        return "";
    }

    public void validate(FacesContext context, UIComponent component, Object object) {
        //surandam apklausą pagal url
        try {
            survey = surveyDAO.getSurveyById((Long) object);
            questions.clear();
            for (Question q : survey.getQuestionList()){
                if (!questions.containsKey(q.getPage())){
                    questions.put(q.getPage(), new ArrayList<>());
                }
                questions.get(q.getPage()).add(q.getQuestionNumber()-1, q);
            }
        } catch (Exception e) {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
        }
    }

    @Transactional
    public String createForm(final String personEmail) {
        //Merging scale offeredAnswer
        if (!surveyIsCorrect()) return null; //TODO: pagal įdėją turėtų būti kažkokie messagai jei blogai.
        Person person = personDAO.FindPersonByEmail(personEmail);
        survey.setPersonID(person);
        survey.setSurveyURL(sg.getRandomString(8));
        person.getSurveyList().add(survey);
        personDAO.UpdateUser(person);
        return "/create/formCreated.xhtml?faces-redirect=true&id="+survey.getSurveyURL(); //TODO: not sure if correct navigation
    }

    private boolean surveyIsCorrect(){
        if (survey.getStartDate() == null){
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            System.out.println(dateFormat.format(date));
            survey.setStartDate(date);
        }
        boolean isZeroQuestions = true;
        survey.getQuestionList().clear();
        for (Integer page : questions.keySet()) {
            List<Question> lst = questions.get(page);
            for (Question q : lst){
                isZeroQuestions = false;
                if (q.getType().equals(Question.QUESTION_TYPE.SCALE.toString())){
                    OfferedAnswer offeredAnswer = new OfferedAnswer();
                    offeredAnswer.setText(q.getOfferedAnswerList().get(0).getText() + ";" + q.getOfferedAnswerList().get(1).getText());
                    offeredAnswer.setQuestionID(q);
                    q.getOfferedAnswerList().clear();
                    q.getOfferedAnswerList().add(offeredAnswer);
                }
                if (q.getQuestionText() == null || q.getQuestionText().isEmpty()){
                    return false;
                }
                for (OfferedAnswer o : q.getOfferedAnswerList()){
                    if (o.getQuestionID().getType().equals(Question.QUESTION_TYPE.TEXT.toString()))
                        continue;
                    if (o.getText() == null || o.getText().isEmpty()){
                        return false;
                    }
                }
            }
            if (isZeroQuestions) return false;
            survey.getQuestionList().addAll(lst);
        }
        return true;
    }

    public void changeQuestionType(final int questionIndex){
        Question question = questions.get(page).get(questionIndex);
        if (question.getType().equals(Question.QUESTION_TYPE.TEXT.toString())){ //If was text
            if (question.getNewType().equals(Question.QUESTION_TYPE.SCALE.toString())) {
                addOfferedAnswer(questionIndex);
            }
        }
        else if (question.getType().equals(Question.QUESTION_TYPE.CHECKBOX.toString()) //If was checkbox or multiple
                || question.getType().equals(Question.QUESTION_TYPE.MULTIPLECHOICE.toString())){
            if (question.getNewType().equals(Question.QUESTION_TYPE.TEXT.toString())) {
                removeAllOfferedAnswers(questionIndex);
                addOfferedAnswer(questionIndex);
            }
            else if (question.getNewType().equals(Question.QUESTION_TYPE.SCALE.toString())) {
                removeAllOfferedAnswers(questionIndex);
                addOfferedAnswer(questionIndex);
                addOfferedAnswer(questionIndex);
            }
        }
        else if (question.getType().equals(Question.QUESTION_TYPE.SCALE.toString())){ //If was scale
            removeAllOfferedAnswers(questionIndex);
            addOfferedAnswer(questionIndex);
        }
        question.setType(question.getNewType());
    }

    public void importExcelFile(FileUploadEvent event) throws IOException
    {
        isImported = false;
        File tempFile = File.createTempFile("temp","");
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        InputStream in = event.getFile().getInputstream();
        IOUtils.copy(in, out);
        excelFile = tempFile;
        try
        {
            survey = excelSurveyImport.importSurveyIntoEntity(excelFile).get();
            isImported = true;
        } catch (InterruptedException | InvalidFormatException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            if(e.getCause() instanceof InvalidFormatException)
            {
                FacesContext.getCurrentInstance().addMessage("messages",
                        new FacesMessage(e.getCause().getMessage()));
            }
            else
            {
                e.printStackTrace();
            }

        }
    }

}
