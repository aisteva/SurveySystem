package CreateFormPackage.Dao;

/**
 * Created by vdeiv on 2017-04-07.
 */

import entitiesJPA.Survey;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by vdeiv on 2017-04-07.
 */
@ApplicationScoped
public class SurveyDao {
    @Inject
    private EntityManager em;

    public void create(Survey survey) {
        em.persist(survey);
    }

    public List<Survey> getAllQuestions() {
        return em.createNamedQuery("Survey.findAll", Survey.class).getResultList();
    }
}
