package services.excel;

import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static entitiesJPA.Question.QUESTION_TYPE.*;

/**
 * Created by arturas on 2017-04-27.
 */
@Named
@RequestScoped
public class ExcelSurveyImport
{
    private XSSFSheet surveySheet = null;
    private String[] surveyColumns = new String[]{"$questionNumber", "$question", "$questionType", "$optionsList"};

    private XSSFSheet answerSheet = null;
    private String[] answerColumns = new String[]{"$answerID", "$questionNumber", "$answer"};

    public Survey importSurveyIntoEntity(File excelFile) throws IOException, InvalidFormatException
    {
        Survey survey = new Survey();

        //Atidaromas excel failas
        FileInputStream file = new FileInputStream(excelFile);
        Workbook wb = new XSSFWorkbook(excelFile);

        //Atsidaromi abu lapai faile
        this.surveySheet = (XSSFSheet) wb.getSheet("Survey");

        if(surveySheet == null)
        {
            throw new InvalidFormatException("Invalid format: file should contain Survey sheet");
        }

        List<Question> questionList = new ArrayList<>();

        //patikrinama pirma eilutė, ar teisingas formatas
        assertCorrectFirstRowFormat(surveySheet, surveyColumns);

        //tikrinamos visos kitos eilutes ir dedamos i entity
        int currentRowNumber = 1;
        Row currentRow = surveySheet.getRow(currentRowNumber);
        //iteruojam per eilutes, kol sutinkam tuščią (pagal reikalavimus)
        while(!isRowEmpty(currentRow))
        {
            Question question = new Question();
            question.setQuestionNumber(getNumericValueFromCell(currentRow.getCell(0)));
            question.setQuestionText(getStringValueFromCell(currentRow.getCell(1)));
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
                    offeredAnswer = new OfferedAnswer();
                    offeredAnswer.setQuestionID(question);
                    offeredAnswer.setText("Text answer");
                    offeredAnswers.add(offeredAnswer);
                    question.setOfferedAnswerList(offeredAnswers);

                    break;
                //iteruojam per stulpelius, kol randam pirma tuscia, galimus atsakymus dedam i sarasa
                case CHECKBOX: case MULTIPLECHOICE:

                    Cell currentCell = currentRow.getCell(currentCellNumber);
                    if(isCellEmpty(currentCell))
                    {
                        throw new InvalidFormatException("Import error in row " + currentRow.getRowNum() +
                                ": " + questionType.name() + " type question should not have an empty cell in $optionsList column");
                    }
                    while(currentCell != null)
                    {
                        offeredAnswer = new OfferedAnswer();
                        offeredAnswer.setQuestionID(question);
                        offeredAnswer.setText(getStringOrNumericValueFromCell(currentCell));
                        offeredAnswers.add(offeredAnswer);
                        question.setOfferedAnswerList(offeredAnswers);

                        currentCell = currentRow.getCell(++currentCellNumber);
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

        //tikrinama, ar yra answer lapas, jei yra, importuojami atsakymai ir priskiriami survey failui
        this.answerSheet = (XSSFSheet) wb.getSheet("Answer");

        //tikrinama, ar yra atsakymų lapas. Jei yra, kviečiamas metodas atsakymų parsinimui
        if(this.answerSheet == null)
        {
            return survey;
        }
        else
        {
            return importAnswersIntoSurveyEntity(survey);
        }

    }

    private Survey importAnswersIntoSurveyEntity(Survey survey) throws InvalidFormatException
    {
        //Tikrinama, ar atitinka pirmos eilutės formatas
        assertCorrectFirstRowFormat(answerSheet, answerColumns);

        int currentRowNumber = 1;
        Row currentRow = answerSheet.getRow(currentRowNumber);

        List<Question> questionList = survey.getQuestionList();
        Answer answer;
        OfferedAnswer offeredAnswer;
        //iteruojam per eilutes, kol sutinkam tuščią (pagal reikalavimus)
        while(!isRowEmpty(currentRow))
        {

            int currentQuestionNumber = getNumericValueFromCell(currentRow.getCell(1));
            Question currentQuestion = questionList.get(currentQuestionNumber-1);
            switch (currentQuestion.getType())
            {
                case "CHECKBOX":
                    break;

                case "MULTIPLECHOICE":
                    break;

                case "TEXT":
                    String textAnswer = getStringValueFromCell(currentRow.getCell(2));
                    offeredAnswer = currentQuestion.getOfferedAnswerList().get(0);

                    answer = new Answer();
                    answer.setOfferedAnswerID(offeredAnswer);
                    answer.setText(textAnswer);
                    answer.setSessionID(0);

                    offeredAnswer.getAnswerList().add(answer);
                    break;

                case "SCALE":
                    int scaleAnswer = getNumericValueFromCell(currentRow.getCell(2));
                    offeredAnswer = currentQuestion.getOfferedAnswerList().get(0);

                    answer = new Answer();
                    answer.setOfferedAnswerID(offeredAnswer);
                    answer.setText(Integer.toString(scaleAnswer));
                    answer.setSessionID(0);

                    offeredAnswer.getAnswerList().add(answer);
                    break;
            }

            currentRow = answerSheet.getRow(++currentRowNumber);

        }
        return survey;
    }


    //Funkcija tikrina, ar eilutė tuščia
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
    private int getNumericValueFromCell(Cell cell) throws InvalidFormatException
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
                    cell.getAddress().toString() + " should contain a number");
        }
    }

    //Funkcija tikrina, ar langelyje yra tekstas, jei ne, išmeta exception su paaiškinimu
    private String getStringValueFromCell(Cell cell) throws InvalidFormatException
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
                    cell.getAddress().toString() + " should contain text");
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

    private void assertCorrectFirstRowFormat(Sheet sheet, String[] args) throws InvalidFormatException
    {
        Row firstRow = sheet.getRow(0);
        if(firstRow == null)
        {
            throw new InvalidFormatException(sheet.getSheetName() + " sheet must contain first row with column names");
        }
        for(int i=0; i<args.length; i++)
        {
            Cell cell = firstRow.getCell(i);
            if(cell != null)
            {
                if(!cell.getStringCellValue().equals(args[i]))
                {
                    throw new InvalidFormatException("Survey import error: Cell " +
                            cell.getAddress().toString() + " must contain " + args[i]);
                }
            }
            else
            {
                throw new InvalidFormatException("Survey import error: First row must contain " + args[i]);
            }
        }
    }
}
