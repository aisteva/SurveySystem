package userModule;

import DAO.Implementations.PersonDAO;
import interceptor.LogInterceptor;
import lombok.Getter;
import lombok.Setter;
import services.PasswordHash;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import java.io.Serializable;

/**
 * Created by arturas on 2017-05-21.
 */
@Named
@RequestScoped
@LogInterceptor
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
            signInController.reload();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Paskyros redagavimas sėkmingas"));
        }
        catch(OptimisticLockException ole)
        {
            System.out.println(ole.getMessage());
            FacesContext.getCurrentInstance().addMessage("profile-form:firstname",
                    new FacesMessage("Profilis buvo pakoreguotas prieš šiuos pakeitimus"));
        }
    }

}
