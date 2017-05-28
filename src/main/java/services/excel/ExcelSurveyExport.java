package services.excel;

import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import org.apache.deltaspike.core.api.future.Futureable;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.ejb.AsyncResult;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import java.io.*;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Created by arturas on 2017-05-11.
 */
@RequestScoped
@CustomerExcelFormat
public class ExcelSurveyExport implements IExcelSurveyExport, Serializable
{
    private static final String[] surveyColumns = new String[]{"$questionNumber", "$question", "$questionType", "$optionsList"};
    private static final String[] answerColumns = new String[]{"$answerID", "$questionNumber", "$answer"};

    //skaitliukai, į kelintą eilutę šiuo metu įrašinėjami duomenys
    private int currentSurveyRowNumber = 0;
    private int currentAnswerRowNumber = 0;

    XSSFCellStyle style;


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

        style = (XSSFCellStyle) wb.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

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
        int questionNumberExcludingPages = 0;
        for(Question question: survey.getQuestionList())
        {
            //sukuriama nauja eilutė, padidinamas skaitliukas
            Row questionRow = surveySheet.createRow(currentSurveyRowNumber++);

            //įrašomi privalomi klausimo atributai: klausimo nr, tekstas ir tipas
            question.setQuestionNumberExcludingPage(++questionNumberExcludingPages); //workaround: skaičiuojamas bendras klausimo nr, be puslapių
            questionRow.createCell(0).setCellValue(question.getQuestionNumberExcludingPage());
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
                    }
                    break;
                case "SCALE":
                    //išsiparsinamas offeredanswer ir įrašomos dvi celes su min ir max
                    String scale = question.getOfferedAnswerList().get(0).getText();
                    String[] answers = scale.split(";");
                    createNumericCell(questionRow, 3).setCellValue(Integer.parseInt(answers[0]));
                    createNumericCell(questionRow, 4).setCellValue(Integer.parseInt(answers[1]));
                    break;
                case "TEXT":
                    questionRow.createCell(3).setCellType(CellType.BLANK);
                    //nerašomas joks option pagal reikalavimus
                    break;
            }
        }
        parseAnswers(survey, answerSheet);

        //Sudedami borderiai survey lentelėje
        Iterator<Row> surveyRowIterator = surveySheet.rowIterator();
        while(surveyRowIterator.hasNext())
        {
            Row row = surveyRowIterator.next();
            for(int i=0; i<surveyColumns.length; i++)
            {
                row.getCell(i).setCellStyle(style);
            }
        }

        //sudedami borderiai answer lentelėje
        Iterator<Row> answerRowIterator = answerSheet.rowIterator();
        while(answerRowIterator.hasNext())
        {
            Row row = answerRowIterator.next();
            for(int i=0; i<answerColumns.length; i++)
            {
                row.getCell(i).setCellStyle(style);
            }
        }

        //auto size columns survey lentelėje
        for(int i=0; i<=surveyColumns.length; i++)
        {
            surveySheet.autoSizeColumn(i);
        }

        //auto size columns answer lentelėje
        for(int i=0; i<=answerColumns.length; i++)
        {
            answerSheet.autoSizeColumn(i);
        }

        return new AsyncResult<>(wb);
    }

    private void parseAnswers(Survey survey, Sheet answerSheet)
    {
        //Visi atsakymai surenkami į vieną sąrašą
        List<Answer> allAnswers = new ArrayList<>();
        for(Question q: survey.getQuestionList())
        {
            int answerNumber = 1;
            for(OfferedAnswer oa: q.getOfferedAnswerList())
            {
                oa.setAnswerNumber(answerNumber++);
                for(Answer a: oa.getAnswerList())
                {
                    if(a.isFinished())
                    {
                        allAnswers.add(a);
                    }
                }
            }
        }
        //atsakymai išrūšiuojami pagal session id
        allAnswers.sort(sessionComparator);

        int answerId = 0; //kadangi keičiam session id į int reikšmes, tam sukuriamas skaitliukas
        String previousSessionId = null; //su šitu tikrinama, ar pasikeitė session id, t.y. ar reikia padidinti skaitliuką
        String previousQuestionType = null;
        int previousQuestionNumber = 0;
        int multipleChoiceCellNumber = 2; //multiplechoice atsakymai rašomi į tą pačią eilutę, tad tai tam skirtas skaitliukas

        Row answerRow = null;
        for(Answer a: allAnswers)
        {

            boolean matchesPreviousType = a.getOfferedAnswerID().getQuestionID().getType().equals(previousQuestionType);
            boolean matchesPreviousSessionId = a.getSessionID().equals(previousSessionId);
            boolean matchesPreviousQuestionNumber = a.getOfferedAnswerID().getQuestionID().getQuestionNumberExcludingPage() == previousQuestionNumber;

            if(!(matchesPreviousType && matchesPreviousSessionId && matchesPreviousQuestionNumber))
            {
                answerRow = answerSheet.createRow(currentAnswerRowNumber++);
            }

            if(!matchesPreviousType)
            {
                previousQuestionType = a.getOfferedAnswerID().getQuestionID().getType();
                multipleChoiceCellNumber = 2;
            }
            //jei sessionID pasikeitė, pakeičiam answer id skaitliuką
            if(!matchesPreviousSessionId)
            {
                previousSessionId = a.getSessionID();
                answerId++;
                multipleChoiceCellNumber = 2;
            }
            if(!matchesPreviousQuestionNumber)
            {
                previousQuestionNumber = a.getOfferedAnswerID().getQuestionID().getQuestionNumberExcludingPage();
                multipleChoiceCellNumber = 2;
            }

            //išsaugojam į excel atsakymo id ir klausimo id
            answerRow.createCell(0).setCellValue(answerId);
            answerRow.createCell(1).setCellValue(a.getOfferedAnswerID().getQuestionID().getQuestionNumberExcludingPage());

            //toliau atsakymai surašomi pagal klausimo tipą
            switch(a.getOfferedAnswerID().getQuestionID().getType())
            {
                //rašomi į sekančią laisvą celę
                case "CHECKBOX":
                case "MULTIPLECHOICE":
                    answerRow.createCell(multipleChoiceCellNumber++).setCellValue(a.getOfferedAnswerID().getAnswerNumber());
                    break;
                //rašomi į answer celę
                case "SCALE":
                    answerRow.createCell(2).setCellValue(Integer.parseInt(a.getText()));
                    multipleChoiceCellNumber = 2;
                    break;
                case "TEXT":
                    answerRow.createCell(2).setCellValue(a.getText());
                    multipleChoiceCellNumber = 2;
                    break;
            }
        }
    }

    private Cell createNumericCell(Row row, int index)
    {
        Cell c = row.createCell(index);
        c.setCellType(CellType.NUMERIC);
        return c;
    }

    //Comparator, kuris išrūšiuoja sąrašą pagal session ID
    private static Comparator<Answer> sessionComparator = (a1, a2) ->
    {
        int res = String.CASE_INSENSITIVE_ORDER.compare(a1.getSessionID(), a2.getSessionID());
        if (res == 0) {
            res = a1.getSessionID().compareTo(a2.getSessionID());
        }
        return res;
    };
}
