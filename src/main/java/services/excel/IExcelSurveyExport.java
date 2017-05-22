package services.excel;

import entitiesJPA.Survey;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by vdeiv on 2017-05-22.
 */
public interface IExcelSurveyExport {
    Future<Workbook> exportSurveyIntoExcelFile(Survey survey) throws IOException;
}
