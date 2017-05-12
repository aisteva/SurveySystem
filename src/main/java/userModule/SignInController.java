package userModule;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import services.PasswordHash;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Lenovo on 2017-04-06.
 */
@Named
@SessionScoped
public class SignInController implements Serializable {
    @Getter
    @Setter
    private Person loggedInPerson =  null;

    @Getter @Setter String expectedEmail = null;
    @Getter @Setter String expectedPassword = null;

    @Inject
    private PersonDAO personDAO;

    @Inject
    PasswordHash ph;

    public String signIn(){
        //tikrinam, ar į abu laukus kas nors įrašyta
        if (expectedEmail == "" || expectedPassword == "") {
            FacesContext.getCurrentInstance().addMessage("signin-form:signin-error-message",
                    new FacesMessage("Įveskite el. paštą ir slaptažodį"));
            loggedInPerson = null;
            return null;
        }
        //tikrinam, ar toks email yra duomenų bazėj ir ar teisingas password
        else if (!isEmailInDatabase() || !isPasswordCorrect())
        {
            FacesContext.getCurrentInstance().addMessage("signin-form:signin-error-message",
                    new FacesMessage("Neteisingas el.paštas arba slaptažodis"));
            loggedInPerson = null;
            return null;
        }
        //tikrinam, ar vartotojas nėra užblokuotas
        else if (loggedInPerson.isBlocked())
        {
            FacesContext.getCurrentInstance().addMessage("signin-form:signin-error-message",
                    new FacesMessage("Vartotojas užblokuotas"));
            loggedInPerson = null;
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
            loggedInPerson = p;
            return true;
        }
    }

    private boolean isPasswordCorrect()
    {
        byte[] byteHashedPasswordAndSalt = new byte[0];
        try
        {
            byteHashedPasswordAndSalt = ph.base64Decode(loggedInPerson.getPassword());
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
            loggedInPerson = null;
            return false;
        }
    }

    public String signOut() {
        loggedInPerson = null;
        return "/signin/signin.xhtml";
    }


    public String getPersonFullName(){
        return loggedInPerson.getFirstName() + " " + loggedInPerson.getLastName();
    }

    public void reload() {
        loggedInPerson = personDAO.findById(loggedInPerson.getPersonID());
    }
    public String isSigned() {
        if (loggedInPerson == null)
            return "/signin/signin.xhtml";
        reload();
        return null;
    }

    public void validate(FacesContext context, UIComponent component, Object object) {
        if(loggedInPerson.getFirstName() != "null" )
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
        if (loggedInPerson == null) return false;
        if (loggedInPerson.getUserType().equals(Person.USER_TYPE.ADMIN.toString())){
            return true;
        }
        return false;
    }

    public String onlyAdmin() {
        if (isAdmin())
            return null;
        return "/index.xhtml";
    }

}
