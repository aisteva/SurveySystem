package Controllers.Interfaces;

import Controllers.ScaleLimits;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;

/**
 * Created by vdeiv on 2017-05-28.
 */
public interface ISaveAnswersController {

    void init();

    void nextPage() throws IOException;

    void prevPage();

    void changeCheckBoxValue(Question q, OfferedAnswer o);

    void changeMultipleValue(Question q, OfferedAnswer o);

    //patikrina, ar yra atsakytą nors į vieną klausimą
    void saveAnswer(boolean isFinished);

    void saveAnswerTransaction(boolean isFinished);

    //metodas padidinantis atsakytu apklausu skaiciu + survey submits optimistic locking
    void increaseSubmits(Boolean isFinished) throws Exception;

    //metodas perraso naujai survey su konfliktuojancio submits skaiciaus survey versija
    void solveSubmits(Boolean isFinished) throws Exception;

    //isparsina gautus scale skacius
    ScaleLimits processLine(List<OfferedAnswer> list) throws IOException;

    void validate(FacesContext context, UIComponent component, Object object) throws IOException;

    //tikrina pagla sessionID, jei norima atsakyti i nebaigta apklausa
    void validateSession(FacesContext context, UIComponent component, Object object);

    void sendUnfinishedSurvey();
}
