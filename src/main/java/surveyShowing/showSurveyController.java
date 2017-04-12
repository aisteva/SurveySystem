package surveyShowing;

import dao.SurveyDao;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Model;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by paulinaoveraite on 2017-04-12.
 */
@Model // tas pats kaip: @Named ir @RequestScoped
@Slf4j
@Getter
public class showSurveyController {

    private Survey survey = new Survey();


    @Getter
    private String kuku = "aaa";

    @Inject
    private SurveyDao surveyDao;

//    public List<OfferedAnswer> getOfferedAnswers(final int questionIndex) {
//        return survey.getQuestionList().get(questionIndex).getOfferedAnswerList();
//    }
//
//    public List<Question> getQuestions() {
//        return survey.getQuestionList();
//    }
//
    public Survey findBySurveyURL(String surveyURL){
        survey = surveyDao.getSurveyByUrl(surveyURL);
        return survey;
    }



}
