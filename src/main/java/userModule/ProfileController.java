package userModule;

import DAO.Implementations.PersonDAO;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import services.MessageCreator;
import services.PasswordHash;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
public class ProfileController implements Serializable
{
    @Inject
    SignInController signInController;
    @Inject @Getter
    SignInPerson signInPerson;
    @Inject
    PasswordHash ph;
    @Getter @Setter
    String unhashedPassword = null, confirmPassword = null;
    @Inject
    PersonDAO personDAO;

    @Inject
    MessageCreator mc;

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
            mc.sendMessage(FacesMessage.SEVERITY_INFO, "Paskyros redagavimas sėkmingas");
        }
        catch(OptimisticLockException ole)
        {
            mc.sendMessage(FacesMessage.SEVERITY_ERROR, "Profilis buvo pakoreguotas prieš šiuos pakeitimus");
        }
    }

}
