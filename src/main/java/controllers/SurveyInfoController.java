package controllers;

import dao.SurveyDAO;
import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import javafx.scene.chart.PieChart;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Hibernate;
import org.omnifaces.util.Faces;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;
import services.excel.ExcelSurveyExport;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by vdeiv on 2017-04-29.
 */
@Named
@ViewScoped
@Slf4j
public class SurveyInfoController implements Serializable{

    @Getter
    @Setter
    String surveyId="";

    @Getter
    Survey survey;

    @Inject
    SurveyDAO surveyDao;

    @Getter
    Map<Long, PieChartModel> pieCharts = new HashMap<>();

    @Getter
    Map<Long, BarChartModel> barCharts = new HashMap<>();

    @Inject
    ExcelSurveyExport excelSurveyExport;

    @PostConstruct
    public void init(){
    }


    public void load(){
        Long ind = Long.parseLong(surveyId);
        survey = surveyDao.getSurveyById(ind);
        for(Question q : survey.getQuestionList()){
            if (q.getType().equals("CHECKBOX") || q.getType().equals("MULTIPLE_CHOICE")) {
                pieCharts.put(q.getQuestionID(),getPieChartModel(q));
            }
           // if (q.getType().equals("SCALE"))
            //    barCharts.put(q.getQuestionID(), getBarChartModel(q));
        }
    }

    public List<Answer> getFreeTextAnswers(Question question){
        OfferedAnswer offeredAnswer = question.getOfferedAnswerList().get(0);
        //TODO: delete at the end.
        offeredAnswer.getAnswerList().add(new Answer(1l, "1", "atsakymasmano", offeredAnswer));
        return offeredAnswer.getAnswerList();
    }

    public PieChartModel getPieChartModel(Question question){

        List<OfferedAnswer> offeredAnswers = question.getOfferedAnswerList();

        int i =1;
        for (OfferedAnswer o: offeredAnswers) {
            o.getAnswerList().add(new Answer(1l, "1", "atsakymasmano"+i, o));
            i++;
        }

        PieChartModel pieModel1 = new PieChartModel();

        for (OfferedAnswer o: offeredAnswers) {
            pieModel1.set(o.getText(), o.getAnswerList().size());
        }

        pieModel1.setTitle(question.getQuestionText());
        pieModel1.setLegendPosition("e");
        return pieModel1;
    }

    public BarChartModel getBarChartModel(Question question){

        List<OfferedAnswer> offeredAnswers = question.getOfferedAnswerList();

        int min = Integer.parseInt(offeredAnswers.get(0).getText());
        int max = Integer.parseInt(offeredAnswers.get(1).getText());

        BarChartModel model = new BarChartModel();

        ChartSeries series = new ChartSeries();
        series.setLabel(question.getQuestionText());

        Map<String, Integer> occurrences = new HashMap<>();
        for (int i=min;i<=max;i++){
            occurrences.put(Integer.toString(i), 0);
        }
        for ( Answer a : offeredAnswers.get(0).getAnswerList() ) {
            Integer oldCount = occurrences.get(a.getText());
            if ( oldCount == null ) {
                oldCount = 0;
            }
            occurrences.put(a.getText(), oldCount + 1);
        }

        for (String s : occurrences.keySet()){
            series.set(s, occurrences.get(s));
        }
        model.addSeries(series);
        return model;
    }

    public int getQuestionAnswersNumber(Question question){
        List<OfferedAnswer> offeredAnswers = question.getOfferedAnswerList();
        int sk = 0;
        for (OfferedAnswer o: offeredAnswers) {
            sk += o.getAnswerList().size();
        }
        return sk;
    }

    public TagCloudModel getTagCloudModel(Question question){
        OfferedAnswer offeredAnswer = question.getOfferedAnswerList().get(0);
        List<Answer> answers = offeredAnswer.getAnswerList();
        String txt="";
        for(Answer answer : answers){
           txt += answer.getText()+" ";
        }

        String[] splitWords = txt.split("\\s*(=>|,|\\s)\\s*");
        Map<String, Integer> occurrences = new HashMap<>();

        for ( String word : splitWords ) {
            Integer oldCount = occurrences.get(word);
            if ( oldCount == null ) {
                oldCount = 0;
            }
            occurrences.put(word, oldCount + 1);
        }

        TagCloudModel model = new DefaultTagCloudModel();

        for(String o : occurrences.keySet()){
            model.addTag(new DefaultTagCloudItem(o, occurrences.get(o)));
        }
        return model;
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
