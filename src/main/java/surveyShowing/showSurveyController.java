package surveyShowing;

import DAO.Implementations.SurveyDAO;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Model;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
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
    @Inject
    private SurveyDAO surveyDAO;

//    public List<OfferedAnswer> getOfferedAnswers(final int questionIndex) {
//        return survey.getQuestionList().get(questionIndex).getOfferedAnswerList();
//    }
//
//    public List<Question> getQuestions() {
//        return survey.getQuestionList();
//    }
//
    public int getScaleMinValue(final int questionIndex) {
        List<OfferedAnswer> answers = survey.getQuestionList().get(questionIndex).getOfferedAnswerList();
        if (answers.size() != 2)
            return -100000;
        int a = Integer.parseInt(answers.get(0).getText());
        int b = Integer.parseInt(answers.get(1).getText());
        return a > b ? b : a;
    }
    public int getScaleMaxValue(final int questionIndex) {
        List<OfferedAnswer> answers = survey.getQuestionList().get(questionIndex).getOfferedAnswerList();
        if (answers.size() != 2)
            return 100000;
        int a = Integer.parseInt(answers.get(0).getText());
        int b = Integer.parseInt(answers.get(1).getText());
        return a > b ? a : b;
    }
    public Survey findBySurveyURL(String surveyURL){
        survey = surveyDAO.getSurveyByUrl(surveyURL);
        return survey;
    }

    public void validate(FacesContext context, UIComponent component, Object object) {
        //surandam apklausą pagal url
        try {
            survey = surveyDAO.getSurveyByUrl((String) object);
        } catch (Exception e) {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
        }
    }



}
