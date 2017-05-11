package services.excel;

import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import org.apache.deltaspike.core.api.future.Futureable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import java.io.*;
import java.util.concurrent.Future;

/**
 * Created by arturas on 2017-05-11.
 */
@RequestScoped
public class ExcelSurveyExport implements Serializable
{
    private static final String[] surveyColumns = new String[]{"$questionNumber", "$question", "$questionType", "$optionsList"};
    private static final String[] answerColumns = new String[]{"$answerID", "$questionNumber", "$answer"};

    private int currentSurveyRowNumber = 0;
    private int currentAnswerRowNumber = 0;

    public void exportSurveyIntoExcelFile(Survey survey) throws IOException
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

        for(Question question: survey.getQuestionList())
        {
            Row questionRow = surveySheet.createRow(currentSurveyRowNumber++);
            questionRow.createCell(0).setCellValue(question.getQuestionNumber());
            questionRow.createCell(1).setCellValue(question.getQuestionText());
            questionRow.createCell(2).setCellValue(question.getType());
            switch (question.getType())
            {
                case "CHECKBOX":
                case "MULTIPLECHOICE":
                    int currentCellNumber = 3;
                    for(OfferedAnswer oa: question.getOfferedAnswerList())
                    {
                        questionRow.createCell(currentCellNumber++).setCellValue(oa.getText());
                    }
                    break;
                case "SCALE":
                    //išsiparsinam offeredanswer ir įrašom dvi celes su min ir max
                    String scale = question.getOfferedAnswerList().get(0).getText();
                    String[] answers = scale.split(";");
                    createNumericCell(questionRow, 3).setCellValue(Integer.parseInt(answers[0]));
                    createNumericCell(questionRow, 4).setCellValue(Integer.parseInt(answers[1]));
                    break;
                case "TEXT":
                    //nerašom jokio option pagal reikalavimus
                    break;
            }
        }

        FileOutputStream fileOut = new FileOutputStream("survey.xlsx");
        wb.write(fileOut);
        fileOut.close();
    }

    private Cell createNumericCell(Row row, int index)
    {
        Cell c = row.createCell(index);
        c.setCellType(CellType.NUMERIC);
        return c;
    }
}
