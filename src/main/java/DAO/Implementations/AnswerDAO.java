package DAO.Implementations;


import DAO.Interfaces.IAnswerDAO;
import entitiesJPA.Answer;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by Aiste on 2017-04-06.
 */

@ApplicationScoped
@Slf4j
public class AnswerDAO implements IAnswerDAO {

    @Inject
    protected EntityManager em;

    public void save(Answer answer) {
        em.persist(answer);
    }

    public List<Answer> getAllAnswers() {
        return em.createNamedQuery("Answer.findAll", Answer.class).getResultList();
    }

    public List<Answer> getSessionAnswers(String sessionID) {
        Query q = em.createNamedQuery("Answer.findBySessionID").setParameter("sessionID", sessionID);
        try {
            return q.getResultList();
        } catch (Exception ex) {
            return null;
        }
    }

    public void remove(Answer answer)
    {
        em.remove(answer);
        em.flush();
    }

}
