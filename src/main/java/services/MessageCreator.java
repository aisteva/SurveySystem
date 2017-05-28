package services;

import services.interfaces.MessageGenerator;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.IOException;

/**
 * Created by arturas on 2017-05-25.
 * Message creator
 */
@RequestScoped
public class MessageCreator implements MessageGenerator
{
    //Funkcija žinutės sukūrimui ir redirectinimui į error langą
    public void redirectToErrorPage(String message)
    {
        try
        {
            sendRedirectableMessage(FacesMessage.SEVERITY_ERROR, message);
            FacesContext.getCurrentInstance().getExternalContext().redirect("/errorPage.html?faces-redirect=true");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void redirectToSuccessPage(String message)
    {
        try
        {
            sendRedirectableMessage(FacesMessage.SEVERITY_INFO, message);
            FacesContext.getCurrentInstance().getExternalContext().redirect("/successPage.html?faces-redirect=true");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //funkcija paprastos žinutės kūrimui tam pačiam lange
    public void sendMessage(FacesMessage.Severity severity, String message)
    {
        //sukuriama žinutė
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, message, message));
    }

    //funkcija, skirta sukurti žinutę, kuri bus redirectinama į bet kokį kitą langą
    public void sendRedirectableMessage(FacesMessage.Severity severity, String message)
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, message, message)); //sukuriama žinutė
        //nustatoma, kad redirectinus žinutė nedings
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        //nustatoma, kad kitas requestas bus redirect
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setRedirect(true);
    }
}
