package dao;

/**
 * Created by vdeiv on 2017-04-07.
 */

import entitiesJPA.Person;
import entitiesJPA.Survey;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by vdeiv on 2017-04-07.
 */
@ApplicationScoped
public class SurveyDAO {
    @Inject
    private EntityManager em;

    public void create(Survey survey) {
        em.persist(survey);
    }

    public List<Survey> getAllSurveys() {
        return em.createNamedQuery("Survey.findAll", Survey.class).getResultList();
    }

    public Survey getSurveyByUrl(String surveyURL){

        Query q = em.createNamedQuery("Survey.findBySurveyURL").setParameter("surveyURL", surveyURL);
        try {
            return (Survey) q.getSingleResult();
        }catch(Exception ex){
            return null;
        }
    }
}
