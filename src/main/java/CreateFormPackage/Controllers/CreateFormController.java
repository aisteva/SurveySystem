package CreateFormPackage.Controllers;

import CreateFormPackage.Dao.OfferedAnswerDao;
import CreateFormPackage.Dao.QuestionDao;
import CreateFormPackage.Dao.SurveyDao;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Person;
import entitiesJPA.Question;
import entitiesJPA.Survey;

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
public class CreateFormController implements Serializable {

    @Inject
    private QuestionDao questionDAO;

    @Inject
    private SurveyDao surveyDAO;

    @Inject
    private OfferedAnswerDao offeredAnswerDao;

    private List<Question> questionList = new ArrayList<>();

    private List<OfferedAnswer> offeredAnswerList = new ArrayList<>();

    public List<OfferedAnswer> getOfferedAnswers(){
        return offeredAnswerList;
    }

    public CreateFormController() {

    }

    public List<Question> getQuestions() {
        return questionList;
    }

    public void onButtonRemoveQuestionClick(final Question question) {
        questionList.remove(question);
    }

    public void onButtonAddQuestionClick(AjaxBehaviorEvent p_oEvent) {
        questionList.add(new Question());
    }

    public void onButtonAddOfferedAnswerClick(final Question question) {
        OfferedAnswer answer = new OfferedAnswer();
        answer.setQuestionID(question);
        offeredAnswerList.add(new OfferedAnswer());
    }

    @Transactional
    public void createForm() {
        Person person = new Person("a", "a", "a", "a", "a",new Date());
        Survey survey = new Survey();
        surveyDAO.create(survey);
        /*for (Question question : questionList) {
            question.setSurveyID(survey);
            questionDAO.create(question);
        }
        for (OfferedAnswer offeredAnswer : offeredAnswerList) {
            offeredAnswerDao.create(offeredAnswer);
        }
*/
    }
}
