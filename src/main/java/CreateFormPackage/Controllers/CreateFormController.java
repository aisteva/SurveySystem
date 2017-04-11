package CreateFormPackage.Controllers;

import CreateFormPackage.Dao.AnswerConnectionDao;
import CreateFormPackage.Dao.OfferedAnswerDao;
import CreateFormPackage.Dao.QuestionDao;
import CreateFormPackage.Dao.SurveyDao;
import dao.PersonDAO;
import entitiesJPA.*;
import lombok.Getter;
import lombok.Setter;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by vdeiv on 2017-04-07.
 */
//@Model // tas pats kaip: @Named ir @RequestScoped
//@Slf4j
@ManagedBean
@ViewScoped
@Getter
public class CreateFormController implements Serializable {

    private Survey survey = new Survey();

    @Inject
    private PersonDAO personDAO;

    public List<OfferedAnswer> getOfferedAnswers(final int questionIndex) {
        return survey.getQuestionList().get(questionIndex).getOfferedAnswerList();
    }

    public List<Question> getQuestions() {
        return survey.getQuestionList();
    }

    public void onButtonRemoveQuestionClick(Question question) {
        survey.getQuestionList().remove(question);
    }

    public void onButtonAddQuestionClick() {
        Question question = new Question();
        question.setSurveyID(survey);
        survey.getQuestionList().add(question);
        onButtonAddOfferedAnswerClick(survey.getQuestionList().size() - 1);
    }

    public void onButtonRemoveAnswerClick(final int questionIndex, final int answerIndex){
        survey.getQuestionList().get(questionIndex).getOfferedAnswerList().remove(answerIndex);
    }

    public void onButtonAddOfferedAnswerClick(final int questionIndex) {
        OfferedAnswer offeredAnswer = new OfferedAnswer();
        Question question = survey.getQuestionList().get(questionIndex);

        offeredAnswer.setQuestionID(question);
        question.getOfferedAnswerList().add(offeredAnswer);

        AnswerConnection answerConnection = new AnswerConnection();
        question.getAnswerConnectionList().add(answerConnection);
        offeredAnswer.getAnswerConnectionList().add(answerConnection);
        answerConnection.setQuestionID(question);
        answerConnection.setOfferedAnswerID(offeredAnswer);
    }

    public void removeAllOfferedAnswers(final int questionIndex) {
        survey.getQuestionList().get(questionIndex).getOfferedAnswerList().clear();
    }

    @Transactional
    public String createForm(final String personEmail) {
        Person person = personDAO.FindPersonByEmail(personEmail);
        survey.setPersonID(person);
        survey.setSurveyURL(getSaltString());
        person.getSurveyList().add(survey);
        personDAO.UpdateUser(person);
        return "/create/formCreated.xhtml"; //TODO: not sure if correct navigation
    }

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}
