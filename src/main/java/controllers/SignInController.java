package controllers;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.log.Log;
import services.PasswordHash;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
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
    private Person person=  new Person();

    @Getter @Setter String expectedPassword = null;

    @Inject
    private PersonDAO personDAO;

    @Inject
    PasswordHash ph;

    public String signIn(){
        if (person.getEmail() == "null" || expectedPassword == null) {
            FacesContext.getCurrentInstance().addMessage("signin-form:password", new FacesMessage("Blogas el.paštas arba slaptažodis"));
            return null;
        }
        try {

            person = personDAO.FindPersonByEmail(person.getEmail());
            byte[] byteHashedPasswordAndSalt = ph.base64Decode(person.getPassword());
            byte[] byteHashedSalt = Arrays.copyOfRange(byteHashedPasswordAndSalt,0, 32);
            byte[] byteHashedPassword = Arrays.copyOfRange(byteHashedPasswordAndSalt, 32, byteHashedPasswordAndSalt.length);

            if(ph.checkPasswordHashWithSalt(expectedPassword,byteHashedSalt,byteHashedPassword))
            {
                return "../index.xhtml?faces-redirect=true";
            }
            else
            {
                FacesContext.getCurrentInstance().addMessage("signin-form:password", new FacesMessage("Blogas el.paštas arba slaptažodis"));
                return null;
            }

        }
        catch (NoResultException nre) {
            FacesContext.getCurrentInstance().addMessage("signin-form:password", new FacesMessage("Blogas el.paštas arba slaptažodis"));
            return null;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String signOut() {
        return "/signin/signin.xhtml";
    }


    public String getPersonFullName(){
        return person.getFirstName() + " " + person.getLastName();
    }

    public String isSigned() {
        if (person.getPersonID() == null)
            return "/signin/signin.xhtml";
        return null;
    }
    public void validate(FacesContext context, UIComponent component, Object object) {
        if(person.getFirstName() != "null" )
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
}
