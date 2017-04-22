package userModule;

import dao.PersonDAO;
import entitiesJPA.Person;
import lombok.Getter;
import lombok.Setter;
import services.PasswordHash;
import services.SaltGenerator;

import javax.el.ELException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by arturas on 2017-04-02.
 */
@Named
@javax.faces.view.ViewScoped
public class CompleteUserRegistrationController implements Serializable
{
    @Inject private PersonDAO personDAO;
    @Getter @Setter Person person = new Person();
    @Setter @Getter private String unhashedPassword = null;
    @Inject PasswordHash ph;
    @Inject SaltGenerator sg;


    public void validateInvitationLink(FacesContext context, UIComponent component, Object object)
    {
        //tikrinam, ar DB yra vartotojas su tokiu url
        try
        {
            person = personDAO.FindPersonByInviteUrl((String)object);
            //tikrinam, ar toks vartotojas is viso yra sarase
            if(person != null)
            {
                //tikrinam, ar vartotojas tikrai dar nera prisiregistraves
                //tikrinam, ar pakvietimas dar galioja
                if(person.getInviteExpiration() == null || !isDateValid())
                {
                    set404(context);
                }
            }
            else
            {
                set404(context);
            }
        }
        catch (Exception e)
        {
            set404(context);
        }
    }

    private boolean isDateValid()
    {
        Date currentDate = new Date();
        Date invitationDate = person.getInviteExpiration();

        //nustatom datą, nuo kurios registracijos pakvietimas dar galiotų
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.DATE, -2);

        //tikrinam, ar pakvietimo data yra po tos datos, nuo kurios pakvietimas dar galiotų
        if(invitationDate.after(cal.getTime()))
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    public String finishRegistration()
    {
        person.setPassword(hashPassword());
        person.setInviteExpiration(null);
        personDAO.UpdateUser(person);
        return "/signin/signin.xhtml?faces-redirect=true";
    }

    private String hashPassword()
    {
        byte[] salt = sg.generateSalt(32);
        byte[] hashedPass = ph.generatePasswordHashWithSalt(unhashedPassword, salt);
        String concatenatedSaltAndPass = ph.base64Encode(concat(salt, hashedPass));
        return (concatenatedSaltAndPass);

    }

    public byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private void set404(FacesContext context)
    {
        context.getExternalContext().setResponseStatus(404);
        context.responseComplete();
    }
}
