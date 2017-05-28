package userModule.interfaces;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Created by arturas on 2017-05-28.
 * Interface for setting password when coming from url
 */
public interface PasswordSetterInterface
{
    void validateInvitationLink(FacesContext context, UIComponent component, Object object);
    void validatePasswordResetLink(FacesContext context, UIComponent component, Object object);
    String finishRegistration();
}
