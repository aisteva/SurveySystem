package services;

/**
 * Created by arturas on 2017-05-28.
 * Email sender interface
 */
public interface EmailSenderInterface
{
    void sendEmail(String to, String subject, String text);
}
