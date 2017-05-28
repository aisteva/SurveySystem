package Controllers.Interfaces;

import java.util.Date;

/**
 * Created by vdeiv on 2017-05-28.
 */
public interface IIndexController {

    void load();

    boolean isSurveyEnded(final Date endDate);
}
