package services;

import javax.enterprise.inject.Specializes;

/**
 * Created by vdeiv on 2017-05-22.
 */
@Specializes
public class SurveyEmailService extends EmailService {

    public SurveyEmailService(){
        super();
        port = "587";
        host = "mail.inbox.lt";
        from = "surveysystem@inbox.lt";
        username = "surveysystem";
        password = "apklausa";
    }
}
