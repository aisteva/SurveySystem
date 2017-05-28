package userModule;

import DAO.Implementations.PersonDAO;
import entitiesJPA.Person;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import services.EmailService;
import services.interfaces.MessageGenerator;
import services.SaltGenerator;
import userModule.interfaces.LinkSenderInterface;

import javax.faces.application.FacesMessage;
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
@SurveySystemLog
public class LinkSenderController implements Serializable, LinkSenderInterface
{

    private final String registrationText = "Laba diena, jus buvote pakviesti prisijungti prie apklausu sistemos." +
            " Noredami uzbaigti registracija spauskite sia nuoroda: " +
            "http://localhost:8080/signup/completeRegistration.html?id=%s";

    private final String registrationSubject = "Survey System: Registracijos nuoroda";

    private final String passwordResetText = " Noredami pasikeisti slaptazodi, spauskite sia nuoroda: " +
            "http://localhost:8080/signin/resetPassword.html?id=%s";

    private final String passwordResetSubject ="Survey System: Slaptažodžio keitimas";

    private static final String REGISTRATION_SUCCESSFUL = "Registracijos laiškas išsiųstas. " +
            "Tęskite registraciją paspaudę nuorodą el. laiške.";

    private static final String PASSWORD_REMINDER_SUCCESSFUL = "Slaptažodžio priminimo laiškas išsiųstas." +
            " Tęskite paspaudę nuorodą el. laiške";


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

    @Inject
    MessageGenerator message;

    public String checkEmailForNewRegistration()
    {
        person = personDAO.FindPersonByEmail(email);
        //jei nerandam vartotojo pagal email, vadinasi jo nera leistinu sarase
        if( person == null)
        {
            message.sendMessage(FacesMessage.SEVERITY_ERROR, "Šio el. pašto nėra leistinų sąraše");

        }
        //jei vartotojas tokiu emailu jau turi nustatyta slaptazodi, reiskia jis jau prisiregistraves
        else if (person.getPassword() != null)
        {
            message.sendMessage(FacesMessage.SEVERITY_ERROR, "Vartotojas su tokiu el. pašto adresu jau registruotas");
        }
        else
        {
            person.setInviteExpiration(new Date());
            person.setInviteUrl(sg.getRandomString(8));
            personDAO.UpdateUser(person);
            sendEmailWithText(registrationSubject, String.format(registrationText, person.getInviteUrl()));
            message.sendRedirectableMessage(FacesMessage.SEVERITY_INFO, REGISTRATION_SUCCESSFUL);
            return "/signin/signin?faces-redirect=true";
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
                message.sendMessage(FacesMessage.SEVERITY_ERROR, "Šis vartotojas nėra užsiregistravęs");
            }
            else
            {
                person.setInviteUrl(sg.getRandomString(8));
                person.setInviteExpiration(new Date());
                personDAO.UpdateUser(person);
                sendEmailWithText(passwordResetSubject, String.format(passwordResetText, person.getInviteUrl()));
                message.sendRedirectableMessage(FacesMessage.SEVERITY_INFO, PASSWORD_REMINDER_SUCCESSFUL);
                return "/signin/signin?faces-redirect=true";
            }
        }
        else
        {
            message.sendMessage(FacesMessage.SEVERITY_ERROR, "Šis vartotojas nėra užsiregistravęs");
        }
        return null;
    }

    private void sendEmailWithText(String subject, String text)
    {
        try
        {
            es.sendEmail(person.getEmail(), subject, text);
        }
        catch(RuntimeException re)
        {
            if (re.getCause() instanceof MessagingException)
            {
                message.sendMessage(FacesMessage.SEVERITY_ERROR, "Nepavyko išsiųsti laiško");
            }
            else
            {
                throw re;
            }
        }
    }


}
