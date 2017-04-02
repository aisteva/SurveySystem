package controllers;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

/**
 * Created by arturas on 2017-04-02.
 */
@Model
public class CompleteUserRegistrationController
{
    @Inject
    private PersonDAO personDAO;

    @Getter
    Person person = new Person();

    public void getUser()
    {
        person = personDAO.FindPersonByEmail(person.getEmail());
    }
}
