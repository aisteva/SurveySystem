package controllers;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import services.EmailService;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceException;
import javax.transaction.TransactionalException;
import java.util.Date;

/**
 * Created by arturas on 2017-04-03.
 */
@Named
@RequestScoped
public class CreateNewUserController
{

    private String text = "Laba diena, jus buvote pakviesti prisijungti prie apklausu sistemos." +
            " Noredami uzbaigti registracija spauskite sia nuoroda: http://localhost:8080/completeRegistration.html?id=%s";

    @Getter
    @Setter
    private Person person = new Person();

    @Inject
    private PersonDAO personDAO;

    @Inject
    private EmailService es;

    public void createNewUser()
    {
        //TODO įdėti exception handling, jei toks email jau egzistuoja
        //TODO įdėti exception handling, jei nepaėjo email išsiųst

        person.setInviteExpiration(new Date());
        try
        {
            personDAO.CreateUser(person);
           // es.sendEmail(person.getEmail(), String.format(text, person.getFirstName(), person.getLastName(), person.getEmail()));
        }
        catch(TransactionalException e)
        {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Vartotojas su tokiu email jau egzistuoja"));
        }


    }
}
