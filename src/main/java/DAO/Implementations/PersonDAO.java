package DAO.Implementations;

import DAO.Interfaces.IPersonDAO;
import entitiesJPA.Person;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by arturas on 2017-04-02.
 */
@ApplicationScoped
public class PersonDAO implements IPersonDAO
{
    @Inject
    protected EntityManager entityManager;

    @Transactional
    public void CreateUser(Person person)
    {
        entityManager.persist(person);
    }

    @Transactional
    public void UpdateUser(Person person)
    {
        entityManager.merge(person);
        entityManager.flush();
    }

    public Person findById(Long id)
    {
        Query q = entityManager.createNamedQuery("Person.findByPersonID").setParameter("personID", id);
        try {
            return (Person) q.getSingleResult();
        }catch(Exception ex){
            return null;
        }
    }

    public Person FindPersonByEmail(String email)
    {
        Query q = entityManager.createNamedQuery("Person.findByEmail").setParameter("email", email);
        try {
            return (Person) q.getSingleResult();
        }catch(Exception ex){
            return null;
        }
    }

    public Person FindPersonByInviteUrl(String url)
    {
        Query q = entityManager.createNamedQuery("Person.findByInviteUrl").setParameter("inviteUrl", url);
        try {
            return (Person) q.getSingleResult();
        }catch(Exception ex){
            return null;
        }
    }

    @Transactional
    public void DeleteUser(Person person)
    {
        Person per = entityManager.merge(person);
        entityManager.remove(per);
    }

    public List<Person> findPersons(){
        return entityManager.createNamedQuery("Person.findAll").getResultList();
    }

    public void updateAndFlush(Person person) {
        entityManager.merge(person);
        entityManager.flush();
    }

}
