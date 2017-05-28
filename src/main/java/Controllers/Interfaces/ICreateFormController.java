package Controllers.Interfaces;

import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import org.primefaces.event.FileUploadEvent;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.*;

/**
 * Created by vdeiv on 2017-05-28.
 */
public interface ICreateFormController {

    List<OfferedAnswer> getOfferedAnswers(final int questionIndex, final int page);

    void addPage(final int currentpage);

    void removePage(final int currentPage);

    void removeQuestion(final int questionIndex, final int page);

    void addQuestion(final int prevQuestionIndex, final int page);

    void addChildQuestion(int questionIndex, int page, OfferedAnswer oa);

    void removeAnswer(int questionIndex, final int answerIndex, final int page);

    void addOfferedAnswer(final int questionIndex, final int page);

    void removeAllOfferedAnswers(final int questionIndex, final int page);

    void moveQuestionUp(final int questionIndex, final int page);

    void moveQuestionDown(final int questionIndex, final int page);

    void movePageUp(final int currentPage);

    void movePageDown(final int currentPage);

    String getQuestionParentMessage(Question question);

    void validate(FacesContext context, UIComponent component, Object object);

    void mapQuestions();

    String createForm(final String personEmail);

    void changeQuestionType(final int questionIndex, final int page);

    void importExcelFile(FileUploadEvent event) throws IOException;
}
