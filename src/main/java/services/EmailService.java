package services;

import log.SurveySystemLog;

import javax.enterprise.context.ApplicationScoped;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by arturas on 2017-04-04.
 */
@ApplicationScoped
@SurveySystemLog
public class EmailService
{
    protected String port = "";
    protected String host = "";
    protected String from = "";
    protected String username = "";
    protected String password = "";

    public void sendEmail(String to, String text)
    {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator()
                {
                    protected PasswordAuthentication getPasswordAuthentication()
                    {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try
        {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject("SurveySystem");

            // Now set the actual message
            message.setText(text);

            // Send message
            Transport.send(message);

        } catch (MessagingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
