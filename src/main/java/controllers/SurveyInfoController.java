package controllers;

import dao.SurveyDAO;
import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.omnifaces.util.Faces;
import services.excel.ExcelSurveyExport;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Tuple;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by vdeiv on 2017-04-29.
 */
@Named
@javax.faces.view.ViewScoped
@Slf4j
public class SurveyInfoController implements Serializable{

    @Getter
    @Setter
    private String surveyId="";

    @Getter
    private Survey survey;

    @Inject
    private SurveyDAO surveyDao;

    @Inject
    private ExcelSurveyExport excelSurveyExport;

    public class AnswerCounter {
        public AnswerCounter(String answerText, int countAnswers){
            this.answerText = answerText;
            this.countAnswers = countAnswers;
        }
        @Getter private String answerText;
        @Getter private int countAnswers;

        public void addToCountAnswers(){
            countAnswers++;
        }
    }

    public List<AnswerCounter> getAnswerCounterList(Long questionId){
        Question question = survey.getQuestionList().stream().filter(x -> x.getQuestionID().equals(questionId)).findFirst().get();
        List<OfferedAnswer> offeredAnswers = question.getOfferedAnswerList();

        List<AnswerCounter> answerCounterList = new ArrayList<>();
        for (OfferedAnswer o : offeredAnswers){
            if (question.getType().equals(Question.QUESTION_TYPE.TEXT.toString())){ // Only for text
                Set<String> texts = new HashSet<>();
                for (Answer a : o.getAnswerList()) {
                    if (texts.contains(a.getText())){
                        answerCounterList.stream().filter(x -> x.getAnswerText().equals(a.getText())).findFirst().get().addToCountAnswers();
                    } else {
                        texts.add(a.getText());
                        answerCounterList.add(new AnswerCounter(a.getText(), 1));
                    }
                }
            }
            else if (question.getType().equals(Question.QUESTION_TYPE.SCALE.toString())){ // Only for scale
                for (Answer a : o.getAnswerList()) {
                    answerCounterList.add(new AnswerCounter(a.getText(), o.getAnswerList().size()));
                }
            }
            else { // Checkbox or multiple
                answerCounterList.add(new AnswerCounter(o.getText(), o.getAnswerList().size()));
            }
        }

        if (question.getType().equals(Question.QUESTION_TYPE.SCALE.toString())){ // Only for scale
            answerCounterList.sort((x,y) -> x.answerText.compareTo(y.answerText));
        }
        return answerCounterList;
    }

    public void load(){
        Long ind = Long.parseLong(surveyId);
        survey = surveyDao.getSurveyById(ind);
    }

    public void exportSurvey()
    {
        try
        {
            File file = new File("apklausa.xlsx");
            Workbook wb = excelSurveyExport.exportSurveyIntoExcelFile(survey).get();
            FileOutputStream fileOut = new FileOutputStream(file);
            wb.write(fileOut);
            fileOut.close();
            Faces.sendFile(file, true);
            file.delete();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }

    }

}
