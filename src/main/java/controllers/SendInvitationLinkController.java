package controllers;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import services.EmailService;
import services.SaltGenerator;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.transaction.TransactionalException;
import java.util.Date;

/**
 * Created by arturas on 2017-04-03.
 */
@Named
@RequestScoped
public class SendInvitationLinkController
{

    private String text = "Laba diena, jus buvote pakviesti prisijungti prie apklausu sistemos." +
            " Noredami uzbaigti registracija spauskite sia nuoroda: http://localhost:8080/signup/completeRegistration.html?id=%s";

    @Getter
    @Setter
    private Person person = new Person();

    @Inject
    private PersonDAO personDAO;

    @Inject
    private EmailService es;

    @Inject
    SaltGenerator sg;

    public void checkEmailInAllowedList()
    {
        person = personDAO.FindPersonByEmail(person.getEmail());
        if( person == null)
        {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Šio el. pašto adreso nėra leistinų adresų sąraše"));
        }
        else
        {
            person.setInviteExpiration(new Date());
            person.setInviteUrl(sg.getRandomString(8));
            personDAO.UpdateUser(person);
            sendConfirmationEmail();
        }

    }

    private void sendConfirmationEmail()
    {
        try
        {
            es.sendEmail(person.getEmail(), String.format(text, person.getInviteUrl()));
        }
        catch(RuntimeException re)
        {
            if (re.getCause() instanceof MessagingException)
            {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Nepavyko išsiųsti registracijos laiško"));
            }
            else
            {
                throw re;
            }

        }
    }

    public void resendConfirmationEmail()
    {
        person.setInviteExpiration(new Date());
        sendConfirmationEmail();
    }
}
