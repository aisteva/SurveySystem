package DAO.Interfaces;

import entitiesJPA.Question;

import java.util.List;

/**
 * Created by vdeiv on 2017-05-22.
 */
public interface IQuestionDAO {

    void create(Question question);

    List<Question> getAllQuestions();
}
