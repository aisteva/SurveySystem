package services.excel;

import entitiesJPA.Survey;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

/**
 * Created by arturas on 2017-04-27.
 */
public class ExcelSurveyImportTest
{
    private String path = ".\\src\\test\\resources\\services\\excel\\";

    //Good example of import file
    private File exampleImportFile = new File(path + "Importo_formatas.xlsx");

    //Files for first row checks
    private File surveyNoFirstLine = new File(path + "survey_noFirstLine.xlsx");
    private File surveyEmptyFirstLineCell = new File(path + "survey_emptyFirstLineCell.xlsx");
    private File surveyWrongFirstLineVariables = new File(path + "survey_wrongFirstLineVariables.xlsx");

    //Files for empty cells
    private File surveyNoQuestionNumber = new File(path + "survey_noQuestionNumber.xlsx");
    private File surveyNoQuestionText = new File(path + "survey_noQuestionNumber.xlsx");
    private File surveyNoQuestionType = new File(path + "survey_noQuestionType.xlsx");

    //Files for invalid content cells
    private File surveyWrongQuestionNumber = new File(path + "survey_wrongQuestionNumber.xlsx");
    private File surveyWrongQuestionType = new File(path + "survey_wrongQuestionType.xlsx");

    //Files for invalid question lists
    private File surveyInvalidTextQuestionList = new File(path + "survey_invalidTextQuestionList.xlsx");
    private File surveyInvalidScaleQuestionList = new File(path + "survey_invalidScaleQuestionList.xlsx");
    private File surveyInvalidCheckboxQuestionList = new File(path + "survey_invalidCheckboxQuestionList.xlsx");
    private File surveyInvalidMultipleChoiceQuestionList = new File(path + "survey_invalidMultipleChoiceQuestionList .xlsx");


    ExcelSurveyImport esi = new ExcelSurveyImport();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void importSurvey() throws Exception
    {
        esi.importSurveyIntoEntity(exampleImportFile);
    }

    @Test
    public void testFirstRow() throws Exception
    {
        expectedException.expectMessage("First row must contain");
        expectedException.expect(InvalidFormatException.class);
        esi.importSurveyIntoEntity(surveyEmptyFirstLineCell);
        esi.importSurveyIntoEntity(surveyWrongFirstLineVariables);
        esi.importSurveyIntoEntity(surveyNoFirstLine);
    }

    @Test
    public void testEmptyFields() throws Exception
    {
        expectedException.expectMessage("should not be empty");
        expectedException.expect(InvalidFormatException.class);
        esi.importSurveyIntoEntity(surveyNoQuestionNumber);
        esi.importSurveyIntoEntity(surveyNoQuestionText);
        esi.importSurveyIntoEntity(surveyNoQuestionType);
    }

    @Test
    public void testWrongFields() throws Exception
    {
        expectedException.expectMessage("should contain");
        expectedException.expect(InvalidFormatException.class);
        esi.importSurveyIntoEntity(surveyWrongQuestionNumber);
        esi.importSurveyIntoEntity(surveyWrongQuestionType);
    }

    @Test
    public void testTextQuestionList() throws Exception
    {
        expectedException.expectMessage("TEXT type question should");
        expectedException.expect(InvalidFormatException.class);
        esi.importSurveyIntoEntity(surveyInvalidTextQuestionList);
    }

    @Test
    public void testScaleQuestionList() throws Exception
    {
        expectedException.expectMessage("SCALE type question should");
        expectedException.expect(InvalidFormatException.class);
        esi.importSurveyIntoEntity(surveyInvalidScaleQuestionList);
    }

    @Test
    public void testMultipleChoiceQuestionList() throws Exception
    {
        expectedException.expectMessage("MULTIPLECHOICE type question should");
        expectedException.expect(InvalidFormatException.class);
        esi.importSurveyIntoEntity(surveyInvalidMultipleChoiceQuestionList);
    }

    @Test
    public void testCheckboxQuestionList() throws Exception
    {
        expectedException.expectMessage("CHECKBOX type question should");
        expectedException.expect(InvalidFormatException.class);
        esi.importSurveyIntoEntity(surveyInvalidCheckboxQuestionList);
    }

}