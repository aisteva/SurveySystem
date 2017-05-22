package DAO.Interfaces;

import entitiesJPA.Answer;

import java.util.List;

/**
 * Created by vdeiv on 2017-05-22.
 */
public interface IAnswerDAO {

    void save(Answer answer);

    List<Answer> getAllAnswers();
}
