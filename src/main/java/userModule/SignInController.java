package userModule;

import DAO.Implementations.PersonDAO;
import entitiesJPA.Person;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import services.interfaces.PasswordHasher;
import userModule.interfaces.SignInInterface;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Lenovo on 2017-04-06.
 */
@Named
@ViewScoped
@SurveySystemLog
public class SignInController implements Serializable, SignInInterface
{

    @Inject @Getter @Setter
    SignInPerson signInPerson;

    @Getter @Setter String expectedEmail = null;
    @Getter @Setter String expectedPassword = null;

    @Inject
    private PersonDAO personDAO;

    @Inject
    PasswordHasher ph;

    @Getter @Setter
    private boolean showRegisterWindow = false;
    @Getter @Setter
    private boolean showForgotPasswordWindow = false;

    public String signIn(){
        //tikrinam, ar į abu laukus kas nors įrašyta
        if (expectedEmail == "" || expectedPassword == "") {
            sendMessage("Įveskite el. paštą ir slaptažodį");
            signInPerson.setLoggedInPerson(null);
            return null;
        }
        //tikrinam, ar toks email yra duomenų bazėj ir ar teisingas password
        else if (!isEmailInDatabase() || !isPasswordCorrect())
        {
            sendMessage("Neteisingas el.paštas arba slaptažodis");
            signInPerson.setLoggedInPerson(null);
            return null;
        }
        //tikrinam, ar vartotojas nėra užblokuotas
        else if (signInPerson.getLoggedInPerson().isBlocked())
        {
            sendMessage("Vartotojas užblokuotas");
            signInPerson.setLoggedInPerson(null);
            return null;
        }
        //jei viskas ok, išvalom laukus ir parodom index.html
        else
        {
            expectedEmail = null;
            expectedPassword = null;
            return "/index.xhtml?faces-redirect=true";
        }
    }

    private boolean isEmailInDatabase()
    {
        Person p = personDAO.FindPersonByEmail(expectedEmail);
        if(p == null)
        {
            return false;
        }
        else
        {
            signInPerson.setLoggedInPerson(p);
            return true;
        }
    }

    private boolean isPasswordCorrect()
    {
        byte[] byteHashedPasswordAndSalt = new byte[0];
        try
        {
            byteHashedPasswordAndSalt = ph.base64Decode(signInPerson.getLoggedInPerson().getPassword());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        byte[] byteHashedSalt = Arrays.copyOfRange(byteHashedPasswordAndSalt,0, 32);
        byte[] byteHashedPassword = Arrays.copyOfRange(byteHashedPasswordAndSalt,
                32, byteHashedPasswordAndSalt.length);
        if(ph.checkPasswordHashWithSalt(expectedPassword,byteHashedSalt,byteHashedPassword))
        {
            return true;
        }
        else
        {
            signInPerson.setLoggedInPerson(null);
            return false;
        }
    }

    public String signOut() {
        signInPerson.setLoggedInPerson(null);
        return "/signin/signin?faces-redirect=true";
    }


    public String getPersonFullName(){
        return signInPerson.getLoggedInPerson().getFirstName() + " " + signInPerson.getLoggedInPerson().getLastName();
    }

    public void reload() {
        signInPerson.setLoggedInPerson(personDAO.findById(signInPerson.getLoggedInPerson().getPersonID()));
    }
    public String isSigned() {
        if (signInPerson.getLoggedInPerson() == null || signInPerson.getLoggedInPerson().isBlocked())
            return "/signin/signin.xhtml";
        reload();
        return null;
    }

    public void validate(FacesContext context, UIComponent component, Object object) {
        if(signInPerson.getLoggedInPerson().getFirstName() != "null" )
        {
            context.responseComplete();
        }
        else
            try {
                context.getExternalContext().redirect("/signup/signup.xhtml");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public boolean isAdmin(){
        if (signInPerson.getLoggedInPerson() == null) return false;
        if (signInPerson.getLoggedInPerson().getUserType().equals(Person.USER_TYPE.ADMIN.toString())){
            return true;
        }
        return false;
    }

    public String onlyAdmin() {
        if (isAdmin())
            return null;
        return "/index.xhtml";
    }

    private void sendMessage(String message)
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
    }

}
