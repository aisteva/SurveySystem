package controller;

import dao.AnswerDAO;
import entitiesJPA.Answer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by Aiste on 2017-04-06.
 */

@Model
@Slf4j
public class SaveAnswersController {


    @Getter
    private Answer answer = new Answer();


    @Inject
    private AnswerDAO answerDAO;


    @Transactional
    public void saveAnswer() {

        answerDAO.save(answer);
    }


    public List<Answer> getAllAnswers() { return answerDAO.getAllAnswers();
    }
}



