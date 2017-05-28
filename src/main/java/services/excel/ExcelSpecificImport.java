package services.excel;

import entitiesJPA.Survey;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by vdeiv on 2017-05-28.
 */
@SpecificExcelFormat
public class ExcelSpecificImport implements IExcelSurveyImport {
    @Override
    public Future<Survey> importSurveyIntoEntity(File excelFile) throws IOException, InvalidFormatException {
        return null; // For future implementations.
    }
}
