package controllers;

import dao.SurveyDAO;
import entitiesJPA.Survey;
import interceptor.LogInterceptor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import userModule.SignInController;

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
@LogInterceptor
public class IndexController implements Serializable {

    @Inject
    SurveyDAO surveyDao;

    @Inject
    private SignInController signInController;

    @Getter
    List<Survey> publicSurveys = new ArrayList<>();

    @Getter
    List<Survey> privateSurveys = new ArrayList<>(); // Only if admin

    @Getter
    List<Survey> personSurveys = new ArrayList<>();

    @PostConstruct
    public void load() {
        personSurveys = signInController.getLoggedInPerson().getSurveyList();
        publicSurveys = surveyDao.getAllSurveysByPrivate(false);
        publicSurveys.stream().filter(p -> !personSurveys.contains(p));
        if (signInController.isAdmin()) {
            privateSurveys = surveyDao.getAllSurveysByPrivate(true);
            privateSurveys.stream().filter(p -> !personSurveys.contains(p));
        }
    }

}
