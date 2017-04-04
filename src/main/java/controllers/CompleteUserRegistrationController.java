package controllers;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.io.Console;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

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
    @Setter
    Person person = new Person();


    public void validate(FacesContext context, UIComponent component, Object object)
    {
        //TODO: patikrinti, ar vartotojas tikrai dar neprisiregistravęs
        //TODO: patikrinti, ar nuoroda dar galioja

        //tikrinam, ar DB yra vartotojas su tokiu email
        try
        {
            person = personDAO.FindPersonByEmail((String)object);
        }
        catch (NoResultException nre)
        {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
        }

        //tikrinam, ar vartotojas tikrai dar nera prisiregistraves
        if(person.getInviteExpiration() == null)
        {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
        }

    }

    public String finishRegistration()
    {
        person.setInviteExpiration(null);
        personDAO.UpdateUser(person);
        return "index";
    }

}
