package DAO.Interfaces;

import entitiesJPA.Survey;

import javax.persistence.Query;
import java.util.List;

/**
 * Created by vdeiv on 2017-05-22.
 */
public interface ISurveyDAO {

    void create(Survey survey);

    List<Survey> getAllSurveys();

    List<Survey> getAllSurveysByPrivate(boolean isPrivate);

    Survey getSurveyByUrl(String surveyURL);

    Survey getSurveyById(Long id);
}
