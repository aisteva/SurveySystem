package controllers;

import dao.SurveyDAO;
import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Tuple;
import java.io.Serializable;
import java.util.*;

/**
 * Created by vdeiv on 2017-04-29.
 */
@Named
@Model
@Slf4j
public class SurveyInfoController implements Serializable{

    @Getter
    @Setter
    private String surveyId="";

    @Getter
    private Survey survey;

    @Inject
    private SurveyDAO surveyDao;

    public class AnswerCounter {
        public AnswerCounter(String answerText, int countAnswers){
            this.answerText = answerText;
            this.countAnswers = countAnswers;
        }
        @Getter private String answerText;
        @Getter private int countAnswers;
    }

    public List<AnswerCounter> getAnswerCounterList(Long questionId){
        List<AnswerCounter> answerCounterList = new ArrayList<>();
        Question question = survey.getQuestionList().stream().filter(x -> x.getQuestionID().equals(questionId)).findFirst().get();
        List<OfferedAnswer> offeredAnswers = question.getOfferedAnswerList();
        for (OfferedAnswer o : offeredAnswers){
            answerCounterList.add(new AnswerCounter(o.getText(), o.getAnswerList().size()));
        }
        if (question.getType().equals(Question.QUESTION_TYPE.SCALE.toString())){ // Only for scale
            answerCounterList.sort((x,y) -> x.answerText.compareTo(y.answerText));
        }
        return answerCounterList;
    }
    @PostConstruct
    public void init(){
    }

    public void load(){
        Long ind = Long.parseLong(surveyId);
        survey = surveyDao.getSurveyById(ind);
    }

}
