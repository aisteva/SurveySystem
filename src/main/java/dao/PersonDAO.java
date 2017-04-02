package dao;

import entitiesJPA.Person;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

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
        Person person = (Person) entityManager.createQuery("SELECT person FROM person WHERE person.email = :emailValue").setParameter("emailValue", email).getSingleResult();
        return person;
    }

}
