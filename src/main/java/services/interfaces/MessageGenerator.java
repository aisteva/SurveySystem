package services.interfaces;

import javax.faces.application.FacesMessage;

/**
 * Created by arturas on 2017-05-28.
 * Interface for generating messages for h:messages  components and redirecting
 */
public interface MessageGenerator
{
    void redirectToErrorPage(String message);
    void redirectToSuccessPage(String message);
    void sendMessage(FacesMessage.Severity severity, String message);
    void sendRedirectableMessage(FacesMessage.Severity severity, String message);
}
