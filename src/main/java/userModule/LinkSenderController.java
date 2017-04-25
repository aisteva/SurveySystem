package userModule;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import services.EmailService;
import services.SaltGenerator;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.util.Date;

/**
 * Created by arturas on 2017-04-03.
 */
@Named
@RequestScoped
public class LinkSenderController
{

    private final String registrationText = "Laba diena, jus buvote pakviesti prisijungti prie apklausu sistemos." +
            " Noredami uzbaigti registracija spauskite sia nuoroda: " +
            "http://localhost:8080/signup/completeRegistration.html?id=%s";

    private final String passwordResetText = " Noredami pasikeisti slaptazodi, spauskite sia nuoroda: " +
            "http://localhost:8080/signin/resetPassword.html?id=%s";


    @Getter
    @Setter
    private Person person = new Person();

    @Inject
    private PersonDAO personDAO;

    @Inject
    private EmailService es;

    @Inject
    SaltGenerator sg;

    public void checkEmailForNewRegistration()
    {
        person = personDAO.FindPersonByEmail(person.getEmail());
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
        }

    }

    public void ResetPassword()
    {
        person = personDAO.FindPersonByEmail(person.getEmail());
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
            }
        }
        else
        {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Šis vartotojas nėra užsiregistravęs"));
        }
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
