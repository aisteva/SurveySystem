package controllers;

import dao.AnswerDAO;
import dao.SurveyDAO;
import entitiesJPA.Answer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vdeiv on 2017-04-28.
 */
@Named
@RequestScoped
@Slf4j
public class IndexController implements Serializable {

    @Inject
    SurveyDAO surveyDao;

    @Inject
    AnswerDAO answerDAO;

    @Getter
    List<Survey> allSurveys = new ArrayList<>();

    @Getter
    List<Survey> userSurveys = new ArrayList<>();

    @PostConstruct
    public void init() {
        allSurveys = surveyDao.getAllPublicSurveys();
        userSurveys = new ArrayList<>(allSurveys);
        //userSurveys.removeIf(x -> !x.getPersonID().getEmail().equals(personEmail) );
    }

}
