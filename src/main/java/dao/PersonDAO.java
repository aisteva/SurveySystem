package dao;

import entitiesJPA.Person;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.transaction.Transactional;

/**
 * Created by arturas on 2017-04-02.
 */
@ApplicationScoped
public class PersonDAO
{
    @Inject
    private EntityManager entityManager;

    @Transactional
    public void CreateUser(Person person)
    {
        entityManager.persist(person);
    }

    @Transactional
    public void UpdateUser(Person person) {entityManager.merge(person);}

    public Person FindPersonByEmail(String email)
    {
        Query q = entityManager.createNamedQuery("Person.findByEmail").setParameter("email", email);
        return (Person) q.getSingleResult();
    }

    public Person FindPersonByEmailAndPassword(String email, String password)
    {
        Query q = entityManager.createQuery("SELECT p FROM Person p WHERE p.email = :emailValue AND p.password = :passwordValue")
                .setParameter("emailValue", email)
                .setParameter("passwordValue", password);
        return (Person) q.getSingleResult();

    }

}
