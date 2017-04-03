package controllers;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Created by arturas on 2017-04-02.
 */
@Named
@javax.faces.view.ViewScoped
public class CompleteUserRegistrationController implements Serializable
{
    @Inject
    private PersonDAO personDAO;

    @Getter
    Person person = new Person();

    public void onLoad()
    {
        //TODO: patikrinti ar email yra DB
        //TODO: patikrinti, ar vartotojas tikrai dar neprisiregistravęs
        //TODO: patikrinti, ar nuoroda dar galioja
        person = personDAO.FindPersonByEmail(person.getEmail());
    }

    public void finishRegistration()
    {
        personDAO.UpdateUser(person);
    }
}
