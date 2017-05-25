package userModule;

import DAO.Implementations.PersonDAO;
import entitiesJPA.Person;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import services.PasswordHash;
import services.SaltGenerator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by arturas on 2017-04-02.
 *
 */
@Named
@javax.faces.view.ViewScoped
@SurveySystemLog
public class SetPasswordByLinkController implements Serializable
{
    @Inject private PersonDAO personDAO;
    @Getter @Setter Person person = new Person();

    @Setter @Getter private String unhashedPassword = null;
    @Getter @Setter private String confirmPassword = null;

    @Inject PasswordHash ph;
    @Inject SaltGenerator sg;

    public void validateInvitationLink(FacesContext context, UIComponent component, Object object)
    {
        String url = (String) object;
        //Tikrinam, ar toks URL yra duomenų bazėje
        if(isUrlInDatabase(url))
        {
            //tikrinam, ar vartotojas tikrai dar nera prisiregistraves
            if(person.getPassword() != null)
            {
                redirectToErrorPage(context, "Vartotojas jau užsiregistravo");
            }
            //tikrinam, ar yra invite expiration data ir kiek galioja
            if(!isDateValid())
            {
                redirectToErrorPage(context, "Nuoroda nebegalioja");
            }

        }
        else
        {
            redirectToErrorPage(context, "Neteisingas URL");
        }
    }


    public void validatePasswordResetLink(FacesContext context, UIComponent component, Object object)
    {
        String url = (String) object;
        //Tikrinam, ar toks URL yra duomenų bazėje
        if(isUrlInDatabase(url))
        {
            //tikrinam, ar vartotojas yra prisiregistraves
            if(person.getPassword() == null)
            {
                redirectToErrorPage(context, "Vartotojas dar neužsiregistravo");
            }
            //tikrinam, ar yra invite expiration data ir kiek galioja
            if(!isDateValid())
            {
                redirectToErrorPage(context, "Nuoroda nebegalioja");
            }

        }
        else
        {
            redirectToErrorPage(context, "Neteisingas URL");
        }
    }


    private boolean isUrlInDatabase(String url)
    {
        Person p = personDAO.FindPersonByInviteUrl(url);
        if(p == null)
        {
            return false;
        }
        else
        {
            this.person = p;
            return true;
        }
    }

    private boolean isDateValid()
    {
        Date currentDate = new Date();
        Date invitationDate = person.getInviteExpiration();
        if(invitationDate == null) return false;

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
        person.setPassword(ph.hashPassword(unhashedPassword));
        person.setInviteExpiration(null);
        personDAO.UpdateUser(person);
        sendMessage(FacesMessage.SEVERITY_INFO, "Operacija sėkminga. Galite prisijungti.");
        return "/signin/signin?faces-redirect=true";
    }



    //Funkcija redirectinti į klaidos langą
    private void redirectToErrorPage(FacesContext context, String message)
    {
        try
        {
            context.getExternalContext().redirect("/errorPage.html");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        context.responseComplete();
    }

    private void sendMessage(FacesMessage.Severity severity, String message)
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, message, message));
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    }
}
