package DAO.Decorators;

import DAO.Implementations.PersonDAO;
import DAO.Interfaces.IPersonDAO;
import entitiesJPA.Person;
import entitiesJPA.Survey;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by vdeiv on 2017-05-22.
 */
@Decorator
public class PersonTestDAO implements IPersonDAO{
    @Inject
    @Delegate
    @Any
    private PersonDAO personDAO;

    @Override
    public void CreateUser(Person person) {
        person.setFirstName("Test: "+ person.getFirstName());
        personDAO.CreateUser(person);
    }

    @Override
    public void UpdateUser(Person person) {
        Survey survey = person.getSurveyList().get(person.getSurveyList().size()-1);
        survey.setTitle("Testing survey: "+survey.getTitle());
        survey.setDescription("Test survey: "+survey.getDescription());
        personDAO.UpdateUser(person);
    }

    @Override
    public Person findById(Long id) {
        return personDAO.findById(id);
    }

    @Override
    public Person FindPersonByEmail(String email) {
        return personDAO.FindPersonByEmail(email);
    }

    @Override
    public Person FindPersonByInviteUrl(String url) {
        return personDAO.FindPersonByInviteUrl(url);
    }

    @Override
    public void DeleteUser(Person person) {
        personDAO.DeleteUser(person);
    }

    @Override
    public List<Person> findPersons() {
        return personDAO.findPersons();
    }

    @Override
    public void updateAndFlush(Person person) {
        personDAO.updateAndFlush(person);
    }
}
