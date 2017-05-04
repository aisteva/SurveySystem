package services.excel;

import entitiesJPA.Survey;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

/**
 * Created by arturas on 2017-04-27.
 */
public class ExcelSurveyImportTest
{
    //Good example of import file
    private String path = ".\\src\\test\\resources\\services\\excel\\";
    private File exampleImportFile = new File(path + "Importo_formatas.xlsx");
    private String importAnswerPath = ".\\src\\test\\resources\\services\\excel\\answer\\";

    /*
        Survey import paths
     */
    private String importSurveyPath = ".\\src\\test\\resources\\services\\excel\\survey\\";

    //Files for first row checks
    private File surveyNoFirstLine = new File(importSurveyPath + "survey_noFirstLine.xlsx");
    private File surveyEmptyFirstLineCell = new File(importSurveyPath + "survey_emptyFirstLineCell.xlsx");
    private File surveyWrongFirstLineVariables = new File(importSurveyPath + "survey_wrongFirstLineVariables.xlsx");

    private File answerEmptyFirstRow = new File(importAnswerPath + "answer_emptyFirstRow.xlsx");
    private File answerNoFirstLine = new File(importAnswerPath + "answer_noFirstLine.xlsx");
    private File answerWrongFirstLineVariables = new File(importSurveyPath + "answer_wrongFirstRowVariable.xlsx");


    //Files for empty cells
    private File surveyNoQuestionNumber = new File(importSurveyPath + "survey_noQuestionNumber.xlsx");
    private File surveyNoQuestionText = new File(importSurveyPath + "survey_noQuestionNumber.xlsx");
    private File surveyNoQuestionType = new File(importSurveyPath + "survey_noQuestionType.xlsx");

    private File answerNoAnswer = new File(importAnswerPath + "answer_emptyAnswer.xlsx");
    private File answerNoAnswerID = new File(importAnswerPath + "answer_emptyAnswerID.xlsx");
    private File answerNoQuestionNumber = new File(importAnswerPath + "answer_emptyQuestionNumber.xlsx");

    //Files for invalid content cells
    private File surveyWrongQuestionNumber = new File(importSurveyPath + "survey_wrongQuestionNumber.xlsx");
    private File surveyWrongQuestionType = new File(importSurveyPath + "survey_wrongQuestionType.xlsx");

    //Files for invalid question lists
    private File surveyInvalidTextQuestionList = new File(importSurveyPath + "survey_invalidTextQuestionList.xlsx");
    private File surveyInvalidScaleQuestionList = new File(importSurveyPath + "survey_invalidScaleQuestionList.xlsx");
    private File surveyInvalidCheckboxQuestionList = new File(importSurveyPath + "survey_invalidCheckboxQuestionList.xlsx");
    private File surveyInvalidMultipleChoiceQuestionList = new File(importSurveyPath + "survey_invalidMultipleChoiceQuestionList .xlsx");


    //out of bounds checks
    private File answerOutOfBoundsQuestionNumber = new File(importAnswerPath + "answer_outOfBoundsQuestionNumber.xlsx");
    private File answerOutOfBoundsAnswerNumber = new File(importAnswerPath + "answer_outOfBoundsAnswerNumber.xlsx");
    private File answerOutOfBoundsScaleAnswer = new File(importAnswerPath + "answer_outOfBoundsScaleAnswer.xlsx");


    ExcelSurveyImport esi = new ExcelSurveyImport();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void importSurvey() throws Exception
    {
        Survey s = esi.importSurveyIntoEntity(exampleImportFile);
        System.out.println(s);
    }

    @Test
    public void testFirstRow() throws Exception
    {
        expectedException.expectMessage("turi turėti");
        expectedException.expect(InvalidFormatException.class);

        esi.importSurveyIntoEntity(surveyEmptyFirstLineCell);
        esi.importSurveyIntoEntity(surveyWrongFirstLineVariables);
        esi.importSurveyIntoEntity(surveyNoFirstLine);

        esi.importSurveyIntoEntity(answerEmptyFirstRow);
        esi.importSurveyIntoEntity(answerNoFirstLine);
        esi.importSurveyIntoEntity(answerWrongFirstLineVariables);
    }

    @Test
    public void testEmptyFields() throws Exception
    {
        expectedException.expectMessage("negali būti tuščias");
        expectedException.expect(InvalidFormatException.class);

        esi.importSurveyIntoEntity(surveyNoQuestionNumber);
        esi.importSurveyIntoEntity(surveyNoQuestionText);
        esi.importSurveyIntoEntity(surveyNoQuestionType);

        esi.importSurveyIntoEntity(answerNoAnswer);
        esi.importSurveyIntoEntity(answerNoAnswerID);
        esi.importSurveyIntoEntity(answerNoQuestionNumber);
    }

    @Test
    public void testOutOfBounds() throws Exception
    {
        expectedException.expectMessage("neegzistuoja");
        expectedException.expect(InvalidFormatException.class);

        esi.importSurveyIntoEntity(answerOutOfBoundsQuestionNumber);
        esi.importSurveyIntoEntity(answerOutOfBoundsAnswerNumber);
        esi.importSurveyIntoEntity(answerOutOfBoundsScaleAnswer);
    }

    @Test
    public void testWrongFields() throws Exception
    {
        expectedException.expectMessage("turi būti");
        expectedException.expect(InvalidFormatException.class);

        esi.importSurveyIntoEntity(surveyWrongQuestionNumber);
        esi.importSurveyIntoEntity(surveyWrongQuestionType);
    }

    @Test
    public void testTextQuestionList() throws Exception
    {
        expectedException.expectMessage("TEXT tipo klausimas");
        expectedException.expect(InvalidFormatException.class);

        esi.importSurveyIntoEntity(surveyInvalidTextQuestionList);
    }

    @Test
    public void testScaleQuestionList() throws Exception
    {
        expectedException.expectMessage("SCALE tipo klausimas");
        expectedException.expect(InvalidFormatException.class);

        esi.importSurveyIntoEntity(surveyInvalidScaleQuestionList);
    }

    @Test
    public void testMultipleChoiceQuestionList() throws Exception
    {
        expectedException.expectMessage("MULTIPLECHOICE tipo klausimas");
        expectedException.expect(InvalidFormatException.class);

        esi.importSurveyIntoEntity(surveyInvalidMultipleChoiceQuestionList);
    }

    @Test
    public void testCheckboxQuestionList() throws Exception
    {
        expectedException.expectMessage("CHECKBOX tipo klausimas");
        expectedException.expect(InvalidFormatException.class);

        esi.importSurveyIntoEntity(surveyInvalidCheckboxQuestionList);
    }

}