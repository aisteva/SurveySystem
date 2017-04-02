package dao;

import entitiesJPA.Person;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Created by arturas on 2017-04-02.
 */
@ApplicationScoped
public class PersonDAO
{
    @Inject
    private EntityManager entityManager;

    public void CreateUser(Person person)
    {
        entityManager.persist(person);
    }

    public Person FindPersonByEmail(String email)
    {
        Query q = entityManager.createQuery("SELECT p FROM Person p WHERE p.email = :emailValue").setParameter("emailValue", email);
        return (Person) q.getSingleResult();
    }

}
