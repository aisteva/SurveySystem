package userModule;

import dao.PersonDAO;
import entitiesJPA.Person;
import interceptor.LogInterceptor;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.context.RequestContext;
import services.PasswordHash;
import services.SaltGenerator;

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
@LogInterceptor
public class ProfileController implements Serializable
{
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
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Paskyros redagavimas sėkmingas"));
        }
        catch(OptimisticLockException ole)
        {
            FacesContext.getCurrentInstance().addMessage("profile-form:firstname",
                    new FacesMessage("Profilis buvo pakoreguotas prieš šiuos pakeitimus"));
        }
    }

}
