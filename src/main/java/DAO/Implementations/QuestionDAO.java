package DAO.Implementations;

import DAO.Interfaces.IQuestionDAO;
import entitiesJPA.Question;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by vdeiv on 2017-04-07.
 */
@ApplicationScoped
public class QuestionDAO implements IQuestionDAO {
    @Inject
    protected EntityManager em;

    public void create(Question question) {
        em.persist(question);
    }

    public List<Question> getAllQuestions() {
        return em.createNamedQuery("Question.findAll", Question.class).getResultList();
    }
}
