package Controllers.Interfaces;

import entitiesJPA.Person;

/**
 * Created by vdeiv on 2017-05-28.
 */
public interface IAdminController {

    void init();

    void reloadAll();

    void updateUserType(Person p);

    void updateUserType(Person p, boolean isPending);

    void deletePerson(Person p);

    void addNewPendingPerson();

    void updateIfBlocked(Person p);

}
