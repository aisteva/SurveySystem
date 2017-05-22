package services.excel;

import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import lombok.Setter;
import org.apache.deltaspike.core.api.future.Futureable;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import services.SaltGenerator;

import javax.ejb.AsyncResult;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import static entitiesJPA.Question.QUESTION_TYPE.*;

/**
 * Created by arturas on 2017-04-27.
 */
@Named
@RequestScoped
@CustomerExcelFormat
public class ExcelSurveyImport implements IExcelSurveyImport, Serializable
{
    private XSSFSheet surveySheet = null;
    private String[] surveyColumns = new String[]{"$questionNumber", "$question", "$questionType", "$optionsList"};

    private XSSFSheet answerSheet = null;
    private String[] answerColumns = new String[]{"$answerID", "$questionNumber", "$answer"};

    @Setter
    @Inject
    SaltGenerator sg;

    String currentSessionId = null;

    @Futureable
    public Future<Survey> importSurveyIntoEntity(File excelFile) throws IOException, InvalidFormatException
    {
        Survey survey = new Survey();

        //Atidaromas excel failas
        FileInputStream file = new FileInputStream(excelFile);
        Workbook wb = new XSSFWorkbook(excelFile);

        //Atsidaromas survey lapas
        this.surveySheet = (XSSFSheet) wb.getSheet("Survey");
        if(surveySheet == null)
        {
            throw new InvalidFormatException("Faile turi būti \"survey\" lapas");
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
            question.setPage(1);
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
                        throw new InvalidFormatException(currentRow.getRowNum()+1 +
                                " eilutėje TEXT tipo klausimas turi turėti tuščią $optionsList stulpelį");
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
                        throw new InvalidFormatException(currentRow.getRowNum() +
                                " eilutėje "+questionType.name() +" tipo klausimas negali turėti tuščio langelio $optionsList stulpelyje");
                    }
                    while(!isCellEmpty(currentCell))
                    {
                        offeredAnswer = new OfferedAnswer();
                        offeredAnswer.setQuestionID(question);
                        offeredAnswer.setText(getStringValueFromCell(currentCell));
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
                        throw new InvalidFormatException(currentRow.getRowNum() +
                                " eilutėje SCALE tipo klausimas turi turėti min ir max reikšmes " +
                                cellMin.getAddress() + " ir " + cellMax.getAddress() + " langeliuose");
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

        //priskiriam pradžiai, kad apklausa neturi atsakymų.
        survey.setSubmits((long)0);

        //tikrinama, ar yra answer lapas, jei yra, importuojami atsakymai ir priskiriami survey failui
        this.answerSheet = (XSSFSheet) wb.getSheet("Answer");

        //tikrinama, ar yra atsakymų lapas. Jei yra, kviečiamas metodas atsakymų parsinimui
        if(this.answerSheet == null)
        {
            return new AsyncResult<>(survey);
        }
        else
        {
            return new AsyncResult<>(importAnswersIntoSurveyEntity(survey));
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

        int answerCount = 0;
        //iteruojam per eilutes, kol sutinkam tuščią (pagal reikalavimus)
        while(!isRowEmpty(currentRow))
        {

            int answerId = getNumericValueFromCell(currentRow.getCell(0));
            if(answerId != answerCount)
            {
                currentSessionId = sg.getRandomString(15);
                answerCount = answerId;
            }
            int currentQuestionNumber = getNumericValueFromCell(currentRow.getCell(1));
            Question currentQuestion = null;
            try
            {
                currentQuestion = questionList.get(currentQuestionNumber-1);

            }
            catch (IndexOutOfBoundsException ioobe)
            {
                throw new InvalidFormatException(currentRow.getCell(1).getAddress() + " langelyje pateiktas klausimo numeris neegzistuoja");
            }

            Cell currentCell = currentRow.getCell(2);
            assertEmptyCell(currentCell);
            switch (currentQuestion.getType())
            {
                case "CHECKBOX":
                    int currentCellNumber = 2;
                    while(!isCellEmpty(currentCell))
                    {
                        int answerNumber = getNumericValueFromCell(currentCell);
                        try
                        {
                            offeredAnswer = currentQuestion.getOfferedAnswerList().get(answerNumber - 1);
                        }
                        catch (IndexOutOfBoundsException ioobe)
                        {
                            throw new InvalidFormatException(currentCell.getAddress() + " langelyje pateiktas klausimo numeris neegzistuoja");
                        }
                        createAnswerAndConnectToOfferedAnswer(offeredAnswer, null);
                        currentCell = currentRow.getCell(++currentCellNumber);
                    }
                    break;
                case "MULTIPLECHOICE":
                    int answerNumber = getNumericValueFromCell(currentCell);
                    try
                    {
                        offeredAnswer = currentQuestion.getOfferedAnswerList().get(answerNumber - 1);
                    }
                    catch (IndexOutOfBoundsException ioobe)
                    {
                        throw new InvalidFormatException(currentCell.getAddress() + " langelyje pateiktas klausimo numeris neegzistuoja");
                    }
                    createAnswerAndConnectToOfferedAnswer(offeredAnswer, null);
                    break;
                case "TEXT":
                    String textAnswer = getStringValueFromCell(currentCell);
                    offeredAnswer = currentQuestion.getOfferedAnswerList().get(0);
                    createAnswerAndConnectToOfferedAnswer(offeredAnswer, textAnswer);
                    break;
                case "SCALE":
                    int scaleAnswer = getNumericValueFromCell(currentCell);
                    offeredAnswer = currentQuestion.getOfferedAnswerList().get(0);
                    if(!isScaleAnswerInBounds(scaleAnswer, offeredAnswer.getText()))
                    {
                        throw new InvalidFormatException(currentCell.getAddress() + "langelyje pateiktas atsakymas neegzistuoja");
                    }
                    createAnswerAndConnectToOfferedAnswer(offeredAnswer, Integer.toString(scaleAnswer));
                    break;
            }
            currentRow = answerSheet.getRow(++currentRowNumber);
        }

        survey.setSubmits((long) answerCount);
        return survey;
    }

    private boolean isScaleAnswerInBounds(int answer, String unparsedScale)
    {
        String[] scale = unparsedScale.split(";");
        int min = Integer.parseInt(scale[0]);
        int max = Integer.parseInt(scale[1]);
        if(answer<min || answer>max)
        {
            return false;
        }
        return true;
    }

    //Funkcija, kuri sukuria naują answer, kurį priskiria offeredAnswer
    private void createAnswerAndConnectToOfferedAnswer(OfferedAnswer offeredAnswer, String answerText)
    {
        Answer answer = new Answer();
        answer.setOfferedAnswerID(offeredAnswer);
        answer.setText(answerText);
        answer.setSessionID(currentSessionId);

        offeredAnswer.getAnswerList().add(answer);
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
    private boolean isRowEmpty(Row row) {
        if(row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellTypeEnum() != CellType.BLANK)
                return false;
        }
        return true;
    }

    //Funkcija, kuri išmeta exception, jei eilutė tuščia
    private void assertEmptyCell(Cell cell) throws InvalidFormatException
    {
        if(cell != null && isCellEmpty(cell))
        {
            throw new InvalidFormatException(cell.getAddress() + " langelis negali būti tuščias");
        }
    }


    //Funkcija tikrina, ar langelyje yra klausimo numeris, jei ne, išmeta exception su paaiškinimu
    private int getNumericValueFromCell(Cell cell) throws InvalidFormatException
    {
        try
        {
            assertEmptyCell(cell);
            return (int) cell.getNumericCellValue();
        }
        catch (IllegalStateException ise)
        {
            throw new InvalidFormatException(cell.getAddress().toString() + " langelyje turi būti skaičius");
        }
    }

    //Funkcija tikrina, ar langelyje yra tekstas, jei ne, išmeta exception su paaiškinimu
    private String getStringValueFromCell(Cell cell) throws InvalidFormatException
    {
        try
        {
            assertEmptyCell(cell);
            return cell.getStringCellValue();
        }
        catch (InvalidFormatException ise)
        {
            throw new InvalidFormatException(cell.getAddress().toString() + " langelyje turi būti tekstas");
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
        throw new InvalidFormatException(cell.getAddress().toString() + " langelyje nurodytas neteisingas klausimo tipas");
    }

    private void assertCorrectFirstRowFormat(Sheet sheet, String[] args) throws InvalidFormatException
    {
        Row firstRow = sheet.getRow(0);
        if(isRowEmpty(firstRow))
        {
            throw new InvalidFormatException(sheet.getSheetName() + " turi turėti pirmą eilutę su stulpelių pavadinimais");
        }
        for(int i=0; i<args.length; i++)
        {
            Cell cell = firstRow.getCell(i);
            if(isCellEmpty(cell))
            {
                throw new InvalidFormatException("Lentelė turi turėti " + args[i] + " stulpelį");
            }
            if(!cell.getStringCellValue().equals(args[i]))
            {
                throw new InvalidFormatException(cell.getAddress().toString() + " langelis turi turėti "
                        + args[i] + " pavadinimą");
            }
        }
    }
}
