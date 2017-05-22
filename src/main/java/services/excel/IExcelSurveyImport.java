package services.excel;

import entitiesJPA.Survey;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by vdeiv on 2017-05-22.
 */
public interface IExcelSurveyImport {
    Future<Survey> importSurveyIntoEntity(File excelFile) throws IOException, InvalidFormatException;
}
