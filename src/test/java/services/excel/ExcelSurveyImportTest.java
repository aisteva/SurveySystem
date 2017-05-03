package services.excel;

import entitiesJPA.Survey;
import org.junit.Test;

import java.io.File;

/**
 * Created by arturas on 2017-04-27.
 */
public class ExcelSurveyImportTest
{
    @Test
    public void importSurvey() throws Exception
    {
        ExcelSurveyImport esi = new ExcelSurveyImport();
        File excelFile = new File("D:\\Documents\\Programos\\SurveySystem\\src\\test\\java\\services\\excel\\Importo_formatas.xlsx");
        Survey survey = esi.importSurveyIntoEntity(excelFile);
    }

}