package CreateFormPackage.Controllers;

import dao.PersonDAO;
import dao.SurveyDAO;
import entitiesJPA.*;
import interceptor.LogInterceptor;
import lombok.Getter;
import services.SaltGenerator;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by vdeiv on 2017-04-07.
 */
//@Model // tas pats kaip: @Named ir @RequestScoped
//@Slf4j
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

    public List<OfferedAnswer> getOfferedAnswers(final int questionIndex) {
        return survey.getQuestionList().stream().
                filter(x -> x.getPage() == page).
                collect(Collectors.toList()).
                get(questionIndex).getOfferedAnswerList();
    }

    public List<Question> getQuestions() {
        if (survey.getQuestionList().stream().filter(x -> x.getPage() == page).collect(Collectors.toList()).size() == 0)
            addQuestion(-1);
        return survey.getQuestionList().stream().filter(x -> x.getPage() == page).collect(Collectors.toList());
    }

    public void nextPage(){
        page += 1;
    }
    public void prevPage(){
        page -= 1;
    }
    public void removeQuestion(final int questionIndex) {
        survey.getQuestionList().remove(questionIndex);
    }

    public void addQuestion(final int questionIndex) {
        Question question = new Question();
        question.setSurveyID(survey);
        question.setType(Question.QUESTION_TYPE.TEXT.toString());
        question.setNewType(Question.QUESTION_TYPE.TEXT.toString());
        question.setQuestionNumber(questionIndex + 1);    // current (clicked) question index + next question
        question.setPage(page);
        survey.getQuestionList().add(questionIndex+1, question);
        addOfferedAnswer(questionIndex+1);
    }

    public void addChildQuestion(final int offeredAnswerIndex, final int questionIndex) {
        addQuestion(questionIndex);
        Question question = survey.getQuestionList().get(questionIndex+1);

        AnswerConnection answerConnection = new AnswerConnection();
        question.getAnswerConnectionList().add(answerConnection);
        answerConnection.setQuestionID(question);
        OfferedAnswer parentOfferedAnswer = getOfferedAnswers(questionIndex).get(offeredAnswerIndex);
        parentOfferedAnswer.getAnswerConnectionList().add(answerConnection);
        answerConnection.setOfferedAnswerID(parentOfferedAnswer);
    }

    public void removeAnswer(int questionIndex, final int answerIndex){
        OfferedAnswer offeredAnswer = survey.getQuestionList().get(questionIndex).getOfferedAnswerList().get(answerIndex);
        for (AnswerConnection answerConnection : offeredAnswer.getAnswerConnectionList()){
            answerConnection.getQuestionID().getAnswerConnectionList().remove(answerConnection); // Deletes from question answerconnections
        }
        survey.getQuestionList().get(questionIndex).getOfferedAnswerList().remove(offeredAnswer);
    }

    public void addOfferedAnswer(final int questionIndex) {
        OfferedAnswer offeredAnswer = new OfferedAnswer();
        Question question = survey.getQuestionList().get(questionIndex);

        offeredAnswer.setQuestionID(question);
        question.getOfferedAnswerList().add(offeredAnswer);
    }

    public void removeAllOfferedAnswers(final int questionIndex) {
        survey.getQuestionList().get(questionIndex).getOfferedAnswerList().clear();
    }

    public void moveQuestionUp(final int questionIndex) {
        if (questionIndex != 0) {
            survey.getQuestionList().get(questionIndex).setQuestionNumber(questionIndex - 1);
            survey.getQuestionList().get(questionIndex - 1).setQuestionNumber(questionIndex);
            Collections.swap(survey.getQuestionList(), questionIndex, questionIndex - 1);
        }
    }
    public void moveQuestionDown(final int questionIndex) {
        if (questionIndex != survey.getQuestionList().size()-1){
            survey.getQuestionList().get(questionIndex).setQuestionNumber(questionIndex + 1);
            survey.getQuestionList().get(questionIndex + 1).setQuestionNumber(questionIndex);
            Collections.swap(survey.getQuestionList(), questionIndex, questionIndex+1);
        }
    }

    public String getQuestionParentMessage(final int questionIndex){
        if (questionIndex != survey.getQuestionList().size()) {
            Question question = survey.getQuestionList().get(questionIndex);
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
        return "/create/formCreated.xhtml?id="+survey.getSurveyURL(); //TODO: not sure if correct navigation
    }

    private boolean surveyIsCorrect(){
        if (survey.getStartDate() == null){
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            System.out.println(dateFormat.format(date));
            survey.setStartDate(date);
        }
        for (Question q : survey.getQuestionList()){
            q.setQuestionNumber(q.getQuestionNumber()+1);
            if (q.getType().equals(Question.QUESTION_TYPE.SCALE.toString())){
                OfferedAnswer offeredAnswer = new OfferedAnswer();
                offeredAnswer.setText(q.getOfferedAnswerList().get(0).getText() + ";" + q.getOfferedAnswerList().get(1).getText());
                q.getOfferedAnswerList().clear();
                offeredAnswer.setQuestionID(q);
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
        return true;
    }

    public void changeQuestionType(final int questionIndex){
        Question question = survey.getQuestionList().get(questionIndex);
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
            addOfferedAnswer(questionIndex);
        }
        question.setType(question.getNewType());
    }

}
