package userModule;

import DAO.Implementations.PersonDAO;
import entitiesJPA.Person;
import interceptor.LogInterceptor;
import lombok.Getter;
import lombok.Setter;
import services.EmailService;
import services.SaltGenerator;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by arturas on 2017-04-03.
 */
@Named
@ViewScoped
@LogInterceptor
public class LinkSenderController implements Serializable
{

    private final String registrationText = "Laba diena, jus buvote pakviesti prisijungti prie apklausu sistemos." +
            " Noredami uzbaigti registracija spauskite sia nuoroda: " +
            "http://localhost:8080/signup/completeRegistration.html?id=%s";

    private final String passwordResetText = " Noredami pasikeisti slaptazodi, spauskite sia nuoroda: " +
            "http://localhost:8080/signin/resetPassword.html?id=%s";


    @Getter
    @Setter
    private Person person = new Person();

    @Getter @Setter String email = null;

    @Inject
    private PersonDAO personDAO;

    @Inject
    private EmailService es;

    @Inject
    SaltGenerator sg;

    public String checkEmailForNewRegistration()
    {
        person = personDAO.FindPersonByEmail(email);
        //jei nerandam vartotojo pagal email, vadinasi jo nera leistinu sarase
        if( person == null)
        {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Šio el. pašto nėra leistinų sąraše"));

        }
        //jei vartotojas tokiu emailu jau turi nustatyta slaptazodi, reiskia jis jau prisiregistraves
        else if (person.getPassword() != null)
        {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Vartotojas su tokiu el. pašto adresu jau registruotas"));
        }
        else
        {
            person.setInviteExpiration(new Date());
            person.setInviteUrl(sg.getRandomString(8));
            personDAO.UpdateUser(person);
            sendEmailWithText(String.format(registrationText, person.getInviteUrl()));
            return "/signin/signin.xhtml";
        }
        return null;
    }

    public String ResetPassword()
    {
        person = personDAO.FindPersonByEmail(email);
        if (person != null)
        {
            if(person.getPassword() == null)
            {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage("Šis vartotojas nėra užsiregistravęs"));
            }
            else
            {
                person.setInviteUrl(sg.getRandomString(8));
                person.setInviteExpiration(new Date());
                personDAO.UpdateUser(person);
                sendEmailWithText(String.format(passwordResetText, person.getInviteUrl()));
                return "/signin/signin.xhtml";
            }
        }
        else
        {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Šis vartotojas nėra užsiregistravęs"));
        }
        return null;
    }

    private void sendEmailWithText(String text)
    {
        try
        {
            es.sendEmail(person.getEmail(), text);
        }
        catch(RuntimeException re)
        {
            if (re.getCause() instanceof MessagingException)
            {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage("Nepavyko išsiųsti laiško"));
            }
            else
            {
                throw re;
            }

        }
    }

}
