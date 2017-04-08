package CreateFormPackage.Dao;

import entitiesJPA.OfferedAnswer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by vdeiv on 2017-04-07.
 */
@ApplicationScoped
public class OfferedAnswerDao {
    @Inject
    private EntityManager em;

    public void create(OfferedAnswer answer) {
        em.persist(answer);
    }

    public List<OfferedAnswer> getAllOfferedAnswers() {
        return em.createNamedQuery("OfferedAnswer.findAll", OfferedAnswer.class).getResultList();
    }
}
