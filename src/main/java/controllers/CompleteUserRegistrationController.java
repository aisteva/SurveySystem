package controllers;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.Calendar;
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
        //tikrinam, ar DB yra vartotojas su tokiu url
        try
        {
            person = personDAO.FindPersonByInviteUrl((String)object);
        }
        catch (NoResultException nre)
        {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
        }

        //tikrinam, ar vartotojas tikrai dar nera prisiregistraves
        //tikrinam, ar pakvietimas dar galioja
        if(person.getInviteExpiration() == null || !isDateValid())
        {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
        }
    }

    private boolean isDateValid()
    {
        Date currentDate = new Date();
        Date invitationDate = person.getInviteExpiration();

        //nustatom datą, nuo kurios registracijos pakvietimas dar galiotų
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.DATE, -2);

        //tikrinam, ar pakvietimo data yra po tos datos, nuo kurios pakvietimas dar galiotų
        if(invitationDate.after(cal.getTime()))
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    public String finishRegistration()
    {
        person.setInviteExpiration(null);
        personDAO.UpdateUser(person);
        return "index";
    }
}
