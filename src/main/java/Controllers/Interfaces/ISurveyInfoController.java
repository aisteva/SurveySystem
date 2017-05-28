package Controllers.Interfaces;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;

/**
 * Created by vdeiv on 2017-05-28.
 */
public interface ISurveyInfoController {

    void load(FacesContext context, UIComponent component, Object object) throws IOException;

    void exportSurvey();

    //Metodas ištrinantis apklausą
    void deleteSurvey();
}
