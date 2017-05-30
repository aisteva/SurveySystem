package userModule;

import DAO.Implementations.PersonDAO;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import services.interfaces.MessageGenerator;
import services.interfaces.PasswordHasher;
import userModule.interfaces.ProfileInterface;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import java.io.Serializable;

/**
 * Created by arturas on 2017-05-21.
 */
@Named
@ViewScoped
@SurveySystemLog
public class ProfileController implements Serializable, ProfileInterface
{
    @Inject
    SignInController signInController;
    @Inject @Getter
    SignInPerson signInPerson;
    @Inject
    PasswordHasher ph;
    @Getter @Setter
    String unhashedPassword = null, confirmPassword = null;
    @Inject
    PersonDAO personDAO;

    @Inject
    MessageGenerator message;

    public void setNewPassword()
    {
        if(!unhashedPassword.isEmpty())
        {
            String newHashedPassword = ph.hashPassword(unhashedPassword);
            signInPerson.getLoggedInPerson().setPassword(newHashedPassword);
        }

    }

    @Transactional
    public void updateProfile()
    {
        setNewPassword();
        try
        {
            personDAO.updateAndFlush(signInPerson.getLoggedInPerson());
            message.redirectToSuccessPage("Profilis sėkmingai pakeistas");
        }
        catch(OptimisticLockException ole)
        {
            message.sendMessage(FacesMessage.SEVERITY_ERROR, "Profilis buvo pakoreguotas prieš šiuos pakeitimus");
        }
    }

}
