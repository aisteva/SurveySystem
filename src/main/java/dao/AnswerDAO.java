package dao;


import entitiesJPA.Answer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by Aiste on 2017-04-06.
 */

@ApplicationScoped
public class AnswerDAO {

    @Inject
    private EntityManager em;


    public void save(Answer answer) {
        em.persist(answer);
    }

    public List<Answer> getAllAnswers() {
        return em.createNamedQuery("Answer.findAll", Answer.class).getResultList();
    }


}
