package userModule;

import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Created by Aiste on 2017-05-18.
 */
@Named
@SessionScoped
public class SignInPerson implements Serializable{

    @Setter
    @Getter
    private Person loggedInPerson =  null;
}
