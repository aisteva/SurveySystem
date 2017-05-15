package importExportModule;

import dao.SurveyDAO;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import services.SaltGenerator;
import services.excel.ExcelSurveyImport;
import services.excel.Importable;
import userModule.SignInController;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.time.DateTimeException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by arturas on 2017-05-04.
 */
@Named
@ViewScoped
public class ExcelImportController implements Serializable
{
    @Inject
    Importable excelSurveyImport;

    @Inject
    SaltGenerator sg;

    @Inject
    SurveyDAO surveyDao;

    @Inject
    SignInController signInController;

    private File excelFile;

    @Getter
    private Future<Survey> asyncSurveyResult;

    @Getter
    private Survey importedSurvey = null;

    @Getter
    boolean pollResult;

    /*
        Metodas, kuris konvertuoja PrimeFaces UploadedFile į File ir paduoda metodui, kuris importuoja apklausą
     */
    public void handleFileUpload(FileUploadEvent event) throws IOException
    {
        importedSurvey = null;
        File tempFile = File.createTempFile("temp","");
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        InputStream in = event.getFile().getInputstream();
        IOUtils.copy(in, out);
        excelFile = tempFile;
        pollResult = true;
    }


    public void importSurvey()
    {
        if(asyncSurveyResult == null)
        {
            try
            {
                importedSurvey = null;
                asyncSurveyResult = excelSurveyImport.importSurveyIntoEntity(excelFile);
            }
            catch (IOException | InvalidFormatException e)
            {
                pollResult = false;
                Messages.addGlobalError(e.getMessage());
            }
        }
        else
        {
            if (asyncSurveyResult.isDone())
            {
                try
                {
                    importedSurvey = asyncSurveyResult.get();
                    asyncSurveyResult = null;
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (ExecutionException e)
                {
                    if(e.getCause() instanceof InvalidFormatException)
                    {
                        FacesContext.getCurrentInstance().addMessage("messages",
                                new FacesMessage(e.getCause().getMessage()));
                        System.out.println(e.getCause().getMessage());
                    }
                    else
                    {
                        e.printStackTrace();
                    }

                }
                finally
                {
                    pollResult = false;
                }
            }
        }
    }

    @Transactional
    public String finishImport()
    {
        importedSurvey.setPersonID(signInController.getLoggedInPerson());
        importedSurvey.setStartDate(new Date());
        importedSurvey.setCreated(true);
        importedSurvey.setSurveyURL(sg.getRandomString(8));
        surveyDao.create(importedSurvey);
        return "index.html"; //TODO kur nukreipiam po importavimo?
    }
}
