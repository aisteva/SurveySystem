package services.excel;

import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import org.apache.deltaspike.core.api.future.Futureable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.ejb.AsyncResult;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import java.io.*;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by arturas on 2017-05-11.
 */
@RequestScoped
//TODO interface
public class ExcelSurveyExport implements Serializable
{
    private static final String[] surveyColumns = new String[]{"$questionNumber", "$question", "$questionType", "$optionsList"};
    private static final String[] answerColumns = new String[]{"$answerID", "$questionNumber", "$answer"};

    //skaitliukai, į kelintą eilutę šiuo metu įrašinėjami duomenys
    private int currentSurveyRowNumber = 0;
    private int currentAnswerRowNumber = 0;


    /*
        Metodas grąžina Apache POI workbook - paskui controlleris sukurs excel failą
     */
    @Futureable
    public Future<Workbook> exportSurveyIntoExcelFile(Survey survey) throws IOException
    {
        //Sukuriamas excel failas, su dviema lapais jame
        Workbook wb = new XSSFWorkbook();
        Sheet surveySheet = wb.createSheet("Survey");
        Sheet answerSheet = wb.createSheet("Answer");

        //Į pirmą eilutę įrašomi parametrų pavadinimai
        Row surveyFirstRow = surveySheet.createRow(currentSurveyRowNumber++);
        Row answerFirstRow = answerSheet.createRow(currentAnswerRowNumber++);
        for (int i=0; i<surveyColumns.length; i++)
        {
            surveyFirstRow.createCell(i).setCellValue(surveyColumns[i]);
        }
        for (int i=0; i<answerColumns.length; i++)
        {
            answerFirstRow.createCell(i).setCellValue(answerColumns[i]);
            surveySheet.autoSizeColumn(i);
        }

        //iteruojamas klausimų sąrašas ir pagal klausimo tipą surašomas į workbook
        for(Question question: survey.getQuestionList())
        {
            //sukuriama nauja eilutė, padidinamas skaitliukas
            Row questionRow = surveySheet.createRow(currentSurveyRowNumber++);

            //įrašomi privalomi klausimo atributai: klausimo nr, tekstas ir tipas
            questionRow.createCell(0).setCellValue(question.getQuestionNumber());
            questionRow.createCell(1).setCellValue(question.getQuestionText());
            questionRow.createCell(2).setCellValue(question.getType());

            switch (question.getType())
            {
                case "CHECKBOX":
                case "MULTIPLECHOICE":
                    //Pradedant nuo $optionslist stulpelio ir einant į dešinę rašomi atsakymų variantai
                    int currentCellNumber = 3;
                    for(OfferedAnswer offeredAnswer: question.getOfferedAnswerList())
                    {
                        questionRow.createCell(currentCellNumber++).setCellValue(offeredAnswer.getText());
                        //apdorojami pateikti atsakymai į klausimą
                        for(Answer answer: offeredAnswer.getAnswerList())
                        {
                            Row answerRow = answerSheet.createRow(currentAnswerRowNumber++);
                            answerRow.createCell(0).setCellValue(answer.getSessionID());
                            answerRow.createCell(1).setCellValue(question.getQuestionNumber());
                            answerRow.createCell(2).setCellValue(answer.getOfferedAnswerID().getText());
                        }
                    }
                    break;
                case "SCALE":
                    //išsiparsinamas offeredanswer ir įrašomos dvi celes su min ir max
                    String scale = question.getOfferedAnswerList().get(0).getText();
                    String[] answers = scale.split(";");
                    createNumericCell(questionRow, 3).setCellValue(Integer.parseInt(answers[0]));
                    createNumericCell(questionRow, 4).setCellValue(Integer.parseInt(answers[1]));
                    for(OfferedAnswer offeredAnswer: question.getOfferedAnswerList())
                    {
                        for(Answer answer: offeredAnswer.getAnswerList())
                        {
                            Row answerRow = answerSheet.createRow(currentAnswerRowNumber++);
                            answerRow.createCell(0).setCellValue(answer.getSessionID());
                            answerRow.createCell(1).setCellValue(question.getQuestionNumber());
                            answerRow.createCell(2).setCellValue(Integer.parseInt(answer.getText()));
                        }
                    }
                    break;
                case "TEXT":
                    //nerašomas joks option pagal reikalavimus
                    for(OfferedAnswer offeredAnswer: question.getOfferedAnswerList())
                    {
                        for(Answer answer: offeredAnswer.getAnswerList())
                        {
                            Row answerRow = answerSheet.createRow(currentAnswerRowNumber++);
                            answerRow.createCell(0).setCellValue(answer.getSessionID());
                            answerRow.createCell(1).setCellValue(question.getQuestionNumber());
                            answerRow.createCell(2).setCellValue(answer.getText());
                        }
                    }
                    break;
            }
        }
        return new AsyncResult<>(wb);
    }

    private Cell createNumericCell(Row row, int index)
    {
        Cell c = row.createCell(index);
        c.setCellType(CellType.NUMERIC);
        return c;
    }
}
