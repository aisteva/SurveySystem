package controllers;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.log.Log;

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

/**
 * Created by Lenovo on 2017-04-06.
 */
@Named
@SessionScoped
public class SignInController implements Serializable {
    @Getter
    @Setter
    private Person person=  new Person();

    @Inject
    private PersonDAO personDAO;

    public String signIn(){
        if (person.getEmail() == "null" || person.getPassword() == "null") {
            FacesContext.getCurrentInstance().addMessage("signin-form:password", new FacesMessage("Blogas el.paštas arba slaptažodis"));
            return null;
        }
        try {
            person = personDAO.FindPersonByEmailAndPassword(person.getEmail(), person.getPassword());
            return "../index.xhtml?faces-redirect=true";
        }
        catch (NoResultException nre) {
            FacesContext.getCurrentInstance().addMessage("signin-form:password", new FacesMessage("Blogas el.paštas arba slaptažodis"));
            return null;
        }

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
