package services.excel;

import entitiesJPA.Survey;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.*;

/**
 * Created by arturas on 2017-05-11.
 */
public class ExcelSurveyExportTest
{
    private String path = ".\\src\\test\\resources\\services\\excel\\";
    private File exampleImportFile = new File(path + "Importo_formatas.xlsx");
    private ExcelSurveyImport esi = new ExcelSurveyImport();

    ExcelSurveyExport ese = new ExcelSurveyExport();

    @Test
    public void exportSurveyIntoExcelFile() throws Exception
    {
        Survey s = esi.importSurveyIntoEntity(exampleImportFile).get();
        Workbook wb = ese.exportSurveyIntoExcelFile(s).get();
        FileOutputStream fileOut = new FileOutputStream("survey.xlsx");
        wb.write(fileOut);
        fileOut.close();
    }

}