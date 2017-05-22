package DAO.Interfaces;

import entitiesJPA.Person;

import java.util.List;

/**
 * Created by vdeiv on 2017-05-22.
 */
public interface IPersonDAO {

    void CreateUser(Person person);

    void UpdateUser(Person person);

    Person findById(Long id);

    Person FindPersonByEmail(String email);

    Person FindPersonByInviteUrl(String url);

    void DeleteUser(Person person);

    List<Person> findPersons();

    void updateAndFlush(Person person);
}
