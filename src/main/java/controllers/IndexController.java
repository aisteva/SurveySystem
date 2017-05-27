package controllers;

import DAO.Implementations.SurveyDAO;
import entitiesJPA.Survey;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import userModule.SignInController;
import userModule.SignInPerson;

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
@SurveySystemLog
public class IndexController implements Serializable {

    @Inject
    SurveyDAO surveyDAO;

    @Inject
    private SignInPerson signInPerson;

    @Inject
    private SignInController signInController;

    @Getter
    List<Survey> publicSurveys = new ArrayList<>();

    @Getter
    List<Survey> privateSurveys = new ArrayList<>(); // Only if admin

    @Getter
    List<Survey> personSurveys = new ArrayList<>();

    @Setter
    @Getter
    private Survey selectedSurvey;

    @PostConstruct
    public void load() {
        personSurveys = signInPerson.getLoggedInPerson().getSurveyList();
        publicSurveys = surveyDAO.getAllSurveysByPrivate(false);
        publicSurveys.stream().filter(p -> !personSurveys.contains(p));
        if (signInController.isAdmin()) {
            privateSurveys = surveyDAO.getAllSurveysByPrivate(true);
            privateSurveys.stream().filter(p -> !personSurveys.contains(p));
        }
    }



}
