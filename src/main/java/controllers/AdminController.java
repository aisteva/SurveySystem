package controllers;

import dao.PersonDAO;
import dao.SurveyDAO;
import entitiesJPA.Person;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import org.omnifaces.cdi.ViewScoped;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vdeiv on 2017-04-15.
 */
@Named
@ViewScoped
@Slf4j
public class AdminController implements Serializable {

    @Inject
    PersonDAO personDao;
    @Inject
    SurveyDAO surveyDAO;

    @Getter private Person conflictingPerson;

    @Getter private Person newPendingPerson = new Person();

    @Getter private List<Person> registeredPersons = new ArrayList<>();

    @Getter private List<Person> pendingPersons = new ArrayList<>();

    @Getter private List<Survey> allSurveys = new ArrayList<Survey>();

    @PostConstruct
    public void init() {
        reloadAll();
    }

    public void reloadAll(){
        List<Person> allPersons = personDao.findPersons();
        registeredPersons = new ArrayList<>(allPersons);
        registeredPersons.removeIf(p -> p.getPassword() == null);
        pendingPersons = new ArrayList<>(allPersons);
        pendingPersons.removeIf(p -> p.getPassword() != null);
    }

    @Transactional
    public void updateUserType(Person p){
        updateUserType(p, false);
    }

    @Transactional
    public void updateUserType(Person p, boolean isPending){
        String title = "Įvyko išoriniai pasikeitimai";
        String text;
        try {
            if (isPending && personDao.findById(p.getPersonID()) == null){
                    text = "Būsimas vartotojas " + p.getEmail() + " buvo prieš tai ištrintas. Pakeitimai anuliuoti.";
                    reloadAll();
                    updateAndShowDialog(title, text);
                    return;
            }
            personDao.updateAndFlush(p);
            text = "Vartotojo " + p.getFirstName() +" "+p.getLastName() + " tipas sėkmingai atnaujintas į " + p.getUserType() +"!";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Successful",  text) );
            reloadAll();
        }
        catch (OptimisticLockException ole) {
            conflictingPerson = personDao.findById(p.getPersonID());
            if (conflictingPerson.getUserType().equals(p.getUserType())){
                if (isPending) text = text = "Būsimo vartotojo " + p.getEmail() + " tipas jau buvo prieš tai pakeistas į " + p.getUserType() +"!";
                          else text = "Vartotojo " + p.getFirstName() +" "+p.getLastName() + " tipas jau buvo prieš tai pakeistas į " + p.getUserType() +"!";
                updateAndShowDialog(title, text);
            }
            else {
                conflictingPerson.setUserType(p.getUserType());
                personDao.updateAndFlush(conflictingPerson);
                text = "Vartotojo " + p.getFirstName() +" "+p.getLastName() + " tipas sėkmingai atnaujintas į " + p.getUserType() +"!";
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Successful",  text) );
            }
            reloadAll();
        }
    }

    private void updateAndShowDialog(String title, String text){
        RequestContext.getCurrentInstance().execute("$('#change-me-pls0').text('"+title+"')");
        RequestContext.getCurrentInstance().execute("$('#change-me-pls').text('"+text+"')");
        RequestContext.getCurrentInstance().execute("$('#user-modal').modal()");
    }

    @Transactional
    public void deletePerson(Person p){
        try {
            conflictingPerson = personDao.findById(p.getPersonID());
            if (conflictingPerson == null){
                String title = "Įvyko išoriniai pasikeitimai";
                String text  = "Būsimas vartotojas " + p.getEmail() + " jau buvo prieš tai ištrintas!";
                reloadAll();
                updateAndShowDialog(title, text);
                return;
            }
            if (p.getOptLockVersion() < conflictingPerson.getOptLockVersion())
                throw new OptimisticLockException();
            personDao.DeleteUser(p);
            reloadAll();
        }
        catch (OptimisticLockException ole) {
            String title ="Įvyko išoriniai pasikeitimai";
            String text = "Būsimas vartotojas " + p.getEmail() + " buvo prieš tai pakeistas. Pakeitimai anuliuoti!";
            reloadAll();
            updateAndShowDialog(title, text);
        }
    }

    @Transactional
    public void addNewPendingPerson() {
        personDao.CreateUser(newPendingPerson); //TODO: add regex and shit
        newPendingPerson = new Person();
        reloadAll();
    }


}
