package controllers;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.log.Log;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.io.Serializable;

/**
 * Created by Lenovo on 2017-04-06.
 */
@Named
@SessionScoped
public class SignInController implements Serializable {
    @Getter
    @Setter
    private Person person = new Person();

    @Inject
    private PersonDAO personDAO;

    public String signIn(){
        try {
            person = personDAO.FindPersonByEmailAndPassword(person.getEmail(), person.getPassword());
            return "index";
        }
        catch (NoResultException nre) {
            return "alert('Blogas el. pašto adresas arba slaptažodis')";
        }

    }
}
