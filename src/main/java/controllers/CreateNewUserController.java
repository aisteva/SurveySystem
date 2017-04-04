package controllers;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import services.EmailService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * Created by arturas on 2017-04-03.
 */
@Named
@RequestScoped
public class CreateNewUserController
{
    @Getter
    @Setter
    private Person person = new Person();

    @Inject
    private PersonDAO personDAO;

    @Inject
    private EmailService es;

    public void createNewUser()
    {
        person.setInviteExpiration(new Date());
        personDAO.CreateUser(person);
        es.sendInvitation(person.getEmail(), "http://localhost:8080/completeRegistration.html?id="+person.getEmail());
    }
}
