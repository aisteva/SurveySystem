package CreateFormPackage.Dao;

import entitiesJPA.AnswerConnection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by vdeiv on 2017-04-07.
 */
@ApplicationScoped
public class AnswerConnectionDao {
    @Inject
    private EntityManager em;

    public void create(AnswerConnection con) {
        em.persist(con);
    }

    public List<AnswerConnection> getAllOfferedAnswers() {
        return em.createNamedQuery("AnswerConnection.findAll", AnswerConnection.class).getResultList();
    }
}
