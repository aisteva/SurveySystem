package services.excel;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by arturas on 2017-04-27.
 */
public class ExcelSurveyImportTest
{
    @Test
    public void importSurvey() throws Exception
    {
        ExcelSurveyImport esi = new ExcelSurveyImport();
        esi.importSurvey();
    }

}