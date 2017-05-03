package services.excel;

import dao.SurveyDao;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arturas on 2017-04-27.
 */
@Named
@RequestScoped
public class ExcelSurveyImport
{

    private XSSFSheet surveySheet = null;
    private XSSFSheet answerSheet = null;


    public Survey importSurveyIntoEntity(File excelFile) throws IOException, InvalidFormatException
    {
        Survey survey = new Survey();

        //Atidaromas excel failas
        FileInputStream file = new FileInputStream(excelFile);
        Workbook wb = new XSSFWorkbook(excelFile);

        //Atsidaromi abu lapai faile
        this.surveySheet = (XSSFSheet) wb.getSheet("Survey");
        //TODO ar būtina?
        this.answerSheet = (XSSFSheet) wb.getSheet("Answer");

        if(surveySheet == null || answerSheet == null)
        {
            throw new InvalidFormatException("Invalid format: file should contain Survey and Answer sheets");
        }


        List<Question> questionList = new ArrayList<>();

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
            throw new InvalidFormatException("Import error: First row must contain " +
                    "$questionNumber, $question, $questionType, $optionsList columns");
        }

        //tikrinamos visos kitos eilutes ir dedamos i entity
        int currentRowNumber = 1;
        Row currentRow = surveySheet.getRow(currentRowNumber);
        //iteruojam per eilutes, kol sutinkam tuščią (pagal reikalavimus)
        while(!isRowEmpty(currentRow))
        {
            Question question = new Question();
            question.setQuestionNumber(getQuestionNumberFromCell(currentRow.getCell(0)));
            question.setQuestionText(getQuestionTextFromText(currentRow.getCell(1)));
            Question.QUESTION_TYPE questionType = getQuestionTypeFromCell(currentRow.getCell(2));

            question.setType(questionType.name());

            //pasiruosiam offered answer sarasa, i kuri desim visus galimus atsakymus apklausai
            List<OfferedAnswer> offeredAnswers = new ArrayList<>();
            OfferedAnswer offeredAnswer;

            int currentCellNumber = 3;
            switch(questionType)
            {
                case TEXT:
                    if(!isCellEmpty(currentRow.getCell(currentCellNumber)))
                    {
                        throw new InvalidFormatException("Import error in row " + currentRow.getRowNum() +
                                ": TEXT type question should have empty cell in $optionsList column");
                    }
                    else
                    {
                        offeredAnswer = new OfferedAnswer();
                        offeredAnswer.setQuestionID(question);
                        offeredAnswer.setText("Text answer");
                        offeredAnswers.add(offeredAnswer);
                        question.setOfferedAnswerList(offeredAnswers);
                    }

                    break;
                //iteruojam per stulpelius, kol randam pirma tuscia, galimus atsakymus dedam i sarasa
                //TODO: jei tolimesnės celės tuščios, mest exception
                case CHECKBOX: case MULTIPLECHOICE:

                Cell currentCell = currentRow.getCell(currentCellNumber);
                if(isCellEmpty(currentCell))
                {
                    throw new InvalidFormatException("Import error in row " + currentRow.getRowNum() +
                            ": " + questionType.name() + " type question should not have an empty cell in $optionsList column");
                }
                else
                {
                    while(currentCell != null)
                    {
                        offeredAnswer = new OfferedAnswer();
                        offeredAnswer.setQuestionID(question);
                        offeredAnswer.setText(getStringOrNumericValueFromCell(currentCell));
                        offeredAnswers.add(offeredAnswer);
                        question.setOfferedAnswerList(offeredAnswers);

                        currentCell = currentRow.getCell(++currentCellNumber);
                    }
                }
                break;
                //pagal sutarta formata dedam min ir max reiksmes i viena offeredanswer
                case SCALE:
                    Cell cellMin = currentRow.getCell(3);
                    Cell cellMax = currentRow.getCell(4);
                    offeredAnswer = new OfferedAnswer();
                    if(cellMin.getCellTypeEnum() == CellType.BLANK ||
                            cellMax.getCellTypeEnum() == CellType.BLANK)
                    {
                        throw new InvalidFormatException("Import error in row " + currentRow.getRowNum() +
                                ": SCALE type question should have min and max values " +
                                "in cells " + cellMin.getAddress() + " and " + cellMax.getAddress());
                    }
                    String scale = (int)currentRow.getCell(3).getNumericCellValue() + ";" +
                            (int)currentRow.getCell(4).getNumericCellValue();
                    offeredAnswer.setQuestionID(question);
                    offeredAnswer.setText(scale);
                    offeredAnswers.add(offeredAnswer);
                    question.setOfferedAnswerList(offeredAnswers);
                    break;

            }
            //pabaigiam sujungti esybes rysiais
            question.setSurveyID(survey);
            questionList.add(question);

            currentRow = surveySheet.getRow(++currentRowNumber);
        }
        //baigus iteruot per eilutes, priskiriam apklausai klausimu sarasa
        survey.setQuestionList(questionList);

        return survey;
    }


    private boolean isCellEmpty(Cell cell)
    {
        if (cell != null)
        {
            if(cell.getCellTypeEnum() != CellType.BLANK)
            {
                return false;
            }
        }
        return true;
    }

    //Funkcija tikrina, ar eilutė tuščia
    public boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellTypeEnum() != CellType.BLANK)
                return false;
        }
        return true;
    }


    //Funkcija tikrina, ar langelyje yra klausimo numeris, jei ne, išmeta exception su paaiškinimu
    private int getQuestionNumberFromCell(Cell cell) throws InvalidFormatException
    {
        try
        {
            if(cell.getCellTypeEnum() == CellType.BLANK)
            {
                throw new InvalidFormatException("Survey import error: Cell " +
                        cell.getAddress().toString() + " should not be empty");
            }
            return (int) cell.getNumericCellValue();
        }
        catch (IllegalStateException ise)
        {
            throw new InvalidFormatException("Survey import error: Cell " +
                    cell.getAddress().toString() + " should contain question number");
        }
    }

    //Funkcija tikrina, ar langelyje yra tekstas, jei ne, išmeta exception su paaiškinimu
    private String getQuestionTextFromText(Cell cell) throws InvalidFormatException
    {
        try
        {
            if(cell.getCellTypeEnum() == CellType.BLANK)
            {
                throw new InvalidFormatException("Survey import error: Cell " +
                        cell.getAddress().toString() + " should not be empty");
            }
            return cell.getStringCellValue();
        }
        catch (InvalidFormatException ise)
        {
            throw new InvalidFormatException("Survey import error: Cell " +
                    cell.getAddress().toString() + " should contain question text");
        }
    }

    //tikrina pagal Question.QUESTION_TYPE, ar celeje tinkamas klausimo tipas, jei ne, išmeta exception
    private Question.QUESTION_TYPE getQuestionTypeFromCell(Cell cell) throws InvalidFormatException
    {
        for(Question.QUESTION_TYPE qt: Question.QUESTION_TYPE.values())
        {
            if(qt.name().equals(cell.getStringCellValue()))
            {
                return qt;
            }
        }
        throw new InvalidFormatException("Survey import error: Cell " +
                cell.getAddress().toString() + " contains invalid question type");
    }

    /*
        Funkcija, kuri randa arba skaiciu, arba eilute celeje
        Kadangi reikia nurodyti, kokios reiksmes ieskoma celeje, o kartais tinka abi, tad si funkcija
        ignoruoja pirma gauta exception jei neatspetas tipas ir iesko kito.
        Jei neranda nei skaitines, nei string reiksmes, ismeta exceptions
     */
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

    //tikrina pirma eilute, ar teisingai suvardinti stulpeliai
    private void checkFirstRowFormat(Cell cell, String text) throws InvalidFormatException
    {
        if(cell != null)
        {
            if(!cell.getStringCellValue().equals(text))
            {
                throw new InvalidFormatException("Survey import error: Cell " +
                        cell.getAddress().toString() + " must contain " + text);
            }
        }
        else
        {
            throw new InvalidFormatException("Survey import error: First row must contain " + text);
        }


    }
}
