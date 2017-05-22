package DAO.Interfaces;

import entitiesJPA.OfferedAnswer;

import java.util.List;

/**
 * Created by vdeiv on 2017-05-22.
 */
public interface IOfferedAnswerDAO {

    void create(OfferedAnswer answer);

    List<OfferedAnswer> getAllOfferedAnswers();

}
