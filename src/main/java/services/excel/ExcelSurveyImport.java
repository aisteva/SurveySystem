package services.excel;

import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.xml.soap.Text;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by arturas on 2017-04-27.
 */
@Named
@ApplicationScoped
public class ExcelSurveyImport
{
    private XSSFSheet surveySheet = null;
    private XSSFSheet answerSheet = null;

    private Survey survey = new Survey();


    public void importSurvey(File excelFile) throws IOException, InvalidFormatException
    {
        FileInputStream file = new FileInputStream(excelFile);
        Workbook wb = new XSSFWorkbook(excelFile);

        this.surveySheet = (XSSFSheet) wb.getSheet("Survey");
        this.answerSheet = (XSSFSheet) wb.getSheet("Answer");

        if(surveySheet == null || answerSheet == null)
        {
            throw new InvalidFormatException("Invalid format: file should contain Survey and Answer sheets");
        }

        parseSurvey();
    }

    private void parseSurvey() throws InvalidFormatException
    {
        //patikrinama pirma eilutė, ar teisingas formatas
        Row firstRow = surveySheet.getRow(0);
        if(firstRow != null)
        {
            checkFirstRowFormat(firstRow.getCell(0), "$questionNumber");
            checkFirstRowFormat(firstRow.getCell(1), "$question");
            checkFirstRowFormat(firstRow.getCell(2), "$questionType");
            checkFirstRowFormat(firstRow.getCell(3), "$optionsList");
        }
        else
        {
            throw new InvalidFormatException("Survey import error: first row should contain $questionNumber, $question, $questionType, $optionsList columns");
        }

        //tikrinamos visos kitos eilutes ir dedamos i entity
        int currentRowNumber = 1;
        Row currentRow = surveySheet.getRow(currentRowNumber);
        while(currentRow != null)
        {
            List<Question> questionList = new ArrayList<>();
            Question q = new Question();

            q.setQuestionNumber((int) currentRow.getCell(0).getNumericCellValue());
            q.setQuestionText(currentRow.getCell(1).getStringCellValue());

            Question.QUESTION_TYPE questionType = checkEnum(currentRow.getCell(2));
            q.setType(questionType.name());
            List<OfferedAnswer> offeredAnswers = new ArrayList<>();

            OfferedAnswer oa;
            switch(questionType)
            {
                case TEXT:
                    oa = new OfferedAnswer();
                    oa.setQuestionID(q);
                    oa.setText("Text answer");
                    offeredAnswers.add(oa);
                    q.setOfferedAnswerList(offeredAnswers);
                    break;
                case CHECKBOX: case MULTIPLECHOICE:
                    int currentCellNumber = 3;
                    Cell currentCell = currentRow.getCell(currentCellNumber);
                    while(currentCell != null)
                    {
                        oa = new OfferedAnswer();
                        oa.setQuestionID(q);
                        oa.setText(getStringOrNumericValueFromCell(currentCell));
                        offeredAnswers.add(oa);
                        q.setOfferedAnswerList(offeredAnswers);

                        currentCell = currentRow.getCell(++currentCellNumber);
                    }
                    break;

                case SCALE:
                    oa = new OfferedAnswer();
                    String scale = (int)currentRow.getCell(3).getNumericCellValue() + ";" + (int)currentRow.getCell(4).getNumericCellValue();
                    oa.setQuestionID(q);
                    oa.setText(scale);
                    offeredAnswers.add(oa);
                    q.setOfferedAnswerList(offeredAnswers);
                    break;

            }
            questionList.add(q);
            currentRow = surveySheet.getRow(++currentRowNumber);
            System.out.println(q.toString());
        }

    }

    private String getStringOrNumericValueFromCell(Cell cell)
    {
        try
        {
            return cell.getStringCellValue();
        }
        catch (IllegalStateException ise)
        {
            return Double.toString(cell.getNumericCellValue());
        }
    }

    private void checkFirstRowFormat(Cell cell, String text) throws InvalidFormatException
    {
        if(!cell.getStringCellValue().equals(text))
        {
            throw new InvalidFormatException("Survey import error: Cell " + cell.getAddress().toString() + " should contain " + text);
        }
    }

    private Question.QUESTION_TYPE checkEnum(Cell cell) throws InvalidFormatException
    {
        for(Question.QUESTION_TYPE qt: Question.QUESTION_TYPE.values())
        {
            if(qt.name().equals(cell.getStringCellValue()))
            {
                return qt;
            }
        }
        throw new InvalidFormatException("Survey import error: Cell " + cell.getAddress().toString() + " contains invalid question type");
    }

}
