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

    @Inject PersonDAO personDao;
    @Inject
    SurveyDAO surveyDAO;

    @Getter private Person selectedPerson;
    @Getter private Person conflictingPerson;

    @Getter private Person newPendingPerson = new Person();

    @Getter private List<Person> registeredPersons = new ArrayList<>();

    @Getter private List<Person> pendingPersons = new ArrayList<>();

    @Getter private List<Survey> allSurveys = new ArrayList<Survey>();

    @PostConstruct
    public void init() {
        reloadAll();
    }

    private List<Person> loadPersons() {
        List<Person> persons = personDao.findPersons();
        persons.forEach(p -> Hibernate.initialize(p.getSurveyList()));
        return persons;
    }


    public void prepareForEditing(Person person) {
        selectedPerson = person;
        conflictingPerson = null;
    }

    public void reloadAll(){
        List<Person> allPersons = loadPersons();
        registeredPersons = new ArrayList<Person>(allPersons);
        registeredPersons.removeIf(p -> p.getPassword() == null);
        pendingPersons = allPersons;
        pendingPersons.removeIf(p -> p.getPassword() != null);
        allSurveys = surveyDAO.getAllSurveys();
    }

    @Transactional
    public void updateSelectedPerson() {
        try {
            personDao.updateAndFlush(selectedPerson);
            selectedPerson = null;
            reloadAll();
        } catch (OptimisticLockException ole) {
            //conflictingStudent = studentDAO.findById(selectedStudent.getId());
            // Pavyzdys, kaip inicializuoti LAZY ryšį, jei jo reikia HTML puslapyje:
            //Hibernate.initialize(conflictingStudent.getCourseList());
            // Pranešam PrimeFaces dialogui, kad užsidaryti dar negalima:
            RequestContext.getCurrentInstance().addCallbackParam("validationFailed", true);
        }
    }

    @Transactional
    public void deleteSelectedPerson(){
        try {
            personDao.DeleteUser(selectedPerson);
            reloadAll();
        } catch (OptimisticLockException ole) {
            //conflictingStudent = studentDAO.findById(selectedStudent.getId());
            // Pavyzdys, kaip inicializuoti LAZY ryšį, jei jo reikia HTML puslapyje:
            //Hibernate.initialize(conflictingStudent.getCourseList());
            // Pranešam PrimeFaces dialogui, kad užsidaryti dar negalima:
            RequestContext.getCurrentInstance().addCallbackParam("validationFailed", true);
        }
    }

    @Transactional
    public void overwritePerson() {
        //selectedPerson.setOptLockVersion(conflictingPerson.getOptLockVersion());
        updateSelectedPerson();
    }

    @Transactional
    public void addNewPendingPerson() {
        personDao.CreateUser(newPendingPerson);
        newPendingPerson = new Person();
        reloadAll();
    }


}
