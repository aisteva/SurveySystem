package services.excel;

import entitiesJPA.Survey;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by vdeiv on 2017-05-28.
 */
@SpecificExcelFormat
public class ExcelSpecificExport implements IExcelSurveyExport{
    @Override
    public Future<Workbook> exportSurveyIntoExcelFile(Survey survey) throws IOException {
        return null; //For future implementations.
    }
}
