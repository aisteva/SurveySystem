package CreateFormPackage.Controllers;

import dao.PersonDAO;
import entitiesJPA.*;
import lombok.Getter;
import services.SaltGenerator;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

/**
 * Created by vdeiv on 2017-04-07.
 */
//@Model // tas pats kaip: @Named ir @RequestScoped
//@Slf4j
@ManagedBean
@ViewScoped
@Getter
public class CreateFormController implements Serializable {

    @Inject
    private SaltGenerator sg;

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
        survey.setSurveyURL(sg.getSaltString());
        person.getSurveyList().add(survey);
        personDAO.UpdateUser(person);
        return "/create/formCreated.xhtml"; //TODO: not sure if correct navigation
    }
}
