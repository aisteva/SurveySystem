package DAO.Interfaces;

import entitiesJPA.AnswerConnection;

import java.util.List;

/**
 * Created by vdeiv on 2017-05-22.
 */
public interface IAnswerConnectionDAO {

    void create(AnswerConnection con);

    List<AnswerConnection> getAllOfferedAnswers();
}
