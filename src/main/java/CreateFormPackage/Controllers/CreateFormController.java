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
import java.util.ArrayList;
import java.util.Collections;
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
public class CreateFormController implements Serializable {

    @Inject
    private SaltGenerator sg;

    private Survey survey = new Survey();

    private int page = 1;

    @Inject
    private PersonDAO personDAO;

    public List<OfferedAnswer> getOfferedAnswers(final int questionIndex) {
        return survey.getQuestionList().get(questionIndex).getOfferedAnswerList();
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
        question.setQuestionNumber(questionIndex + 2);    // current (clicked) question index + next question (1) + 1
        question.setPage(page);
        survey.getQuestionList().add(questionIndex + 1, question);
        addOfferedAnswer(questionIndex + 1);
    }

    public void removeAnswer(int questionIndex, final int answerIndex){
        survey.getQuestionList().get(questionIndex).getOfferedAnswerList().remove(answerIndex);
    }

    public void addOfferedAnswer(final int questionIndex) {
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

    public void removeAllAnswerConnections(final int questionIndex){
        survey.getQuestionList().get(questionIndex).getAnswerConnectionList().clear();
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
    @Transactional
    public String createForm(final String personEmail) {
        Person person = personDAO.FindPersonByEmail(personEmail);
        survey.setPersonID(person);
        survey.setSurveyURL(sg.getRandomString(8));
        person.getSurveyList().add(survey);
        personDAO.UpdateUser(person);
        return "/create/formCreated.xhtml"; //TODO: not sure if correct navigation
    }
}
