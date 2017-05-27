package DAO.Implementations;

/**
 * Created by vdeiv on 2017-04-07.
 */

import DAO.Interfaces.ISurveyDAO;
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
public class SurveyDAO implements ISurveyDAO {
    @Inject
    protected EntityManager em;

    public void create(Survey survey) {
        em.persist(survey);
    }

    public void update(Survey survey) {
        em.merge(survey);
        em.flush();
    }

    public void delete(Survey survey){
        em.remove(survey);
    }

    public List<Survey> getAllSurveys() {
        return em.createNamedQuery("Survey.findAll", Survey.class).getResultList();
    }

    public List<Survey> getAllSurveysByPrivate(boolean isPrivate){
        return em.createNamedQuery("Survey.findByIsPrivate").setParameter("isPrivate", isPrivate).getResultList();
    }

    public Survey getSurveyByUrl(String surveyURL){

        Query q = em.createNamedQuery("Survey.findBySurveyURL").setParameter("surveyURL", surveyURL);
        try {
            return (Survey) q.getSingleResult();
        }catch(Exception ex){
            return null;
        }
    }

    public Survey getSurveyById(Long id){

        Query q = em.createNamedQuery("Survey.findBySurveyID").setParameter("surveyID", id);
        try {
            return (Survey) q.getSingleResult();
        }catch(Exception ex){
            return null;
        }
    }
}
