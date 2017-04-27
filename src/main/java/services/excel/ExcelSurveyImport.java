package services.excel;

import entitiesJPA.Question;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.xml.soap.Text;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringJoiner;

/**
 * Created by arturas on 2017-04-27.
 */
@Named
@RequestScoped
public class ExcelSurveyImport
{
    private XSSFSheet surveySheet = null;
    private XSSFSheet answerSheet = null;

    private Survey survey = new Survey();


    public void importSurvey() throws IOException, InvalidFormatException
    {
        FileInputStream file = new FileInputStream(new File("D:\\Documents\\Programos\\SurveySystem\\src\\main\\java\\services\\excel\\Importo_formatas.xlsx"));
        Workbook wb = new XSSFWorkbook(file);

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
        Iterator<Row> rowIterator = surveySheet.rowIterator();
        //patikrinama pirma eilutė, ar teisingas formatas
        if(rowIterator.hasNext())
        {
            Row firstRow = rowIterator.next();
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
        while(rowIterator.hasNext())
        {
            Row currentRow = rowIterator.next();

            Question q = new Question();
            q.setQuestionNumber((int) currentRow.getCell(0).getNumericCellValue());
            q.setQuestionText(currentRow.getCell(1).getStringCellValue());
            q.setType(checkEnum(currentRow.getCell(2)));
        }
    }

    private void checkFirstRowFormat(Cell cell, String text) throws InvalidFormatException
    {
        if(!cell.getStringCellValue().equals(text))
        {
            throw new InvalidFormatException("Survey import error: Cell " + cell.getAddress().toString() + " should contain " + text);
        }
    }

    private String checkEnum(Cell cell) throws InvalidFormatException
    {
        for(Question.QUESTION_TYPE qt: Question.QUESTION_TYPE.values())
        {
            if(qt.name().equals(cell.getStringCellValue()))
            {

                return cell.getStringCellValue();
            }
        }
        throw new InvalidFormatException("Survey import error: Cell " + cell.getAddress().toString() + " contains invalid quesion type");
    }

}
