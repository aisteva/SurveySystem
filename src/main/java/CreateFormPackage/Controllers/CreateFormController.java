package CreateFormPackage.Controllers;

import CreateFormPackage.Dao.AnswerConnectionDao;
import CreateFormPackage.Dao.OfferedAnswerDao;
import CreateFormPackage.Dao.QuestionDao;
import CreateFormPackage.Dao.SurveyDao;
import dao.PersonDAO;
import entitiesJPA.*;
import lombok.Getter;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
    private PersonDAO personDAO;

    private List<Question> questionList = new ArrayList<>();

    private List<OfferedAnswer> offeredAnswerList = new ArrayList<>();

    public void onButtonRemoveQuestionClick(final Question question) {
        questionList.remove(question);
    }

    public void onButtonAddQuestionClick(AjaxBehaviorEvent p_oEvent) {
        questionList.add(new Question());
    }

    public void onButtonAddOfferedAnswerClick(final Question question) {
        OfferedAnswer answer = new OfferedAnswer();
        answer.setQuestionID(question);
        question.getOfferedanswerList().add(answer);
        offeredAnswerList.add(answer);
    }

    @Transactional
    public void createForm() {
        Person person = personDAO.FindPersonByEmail("a");
        if (person == null) person = new Person("a","a", "a", "a", "a", new Date());
        Survey survey = new Survey("sf", new Date(), "fsfds564", true, true, false, person);
        survey.setPersonID(person);
        person.getSurveyList().add(survey);
        for (Question question : questionList) {
            question.setSurveyID(survey);
            question.setType(question.getQuestionType().toString());
            survey.getQuestionList().add(question);
            for (OfferedAnswer offAnsw : question.getOfferedanswerList()){
                AnswerConnection conn = new AnswerConnection();
                question.getAnswerconnectionList().add(conn);
                offAnsw.getAnswerconnectionList().add(conn);
                conn.setQuestionID(question);
                conn.setOfferedAnswerID(offAnsw);
            }
        }

        personDAO.UpdateUser(person);
    }
}
