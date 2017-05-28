package services;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by arturas on 2017-04-22.
 */
public class EmailServiceTestJUnit
{
    @Test
    public void sendEmail() throws Exception
    {
        EmailService s = new EmailService();
        s.sendEmail("test@example.com", "This is a test", "what a test");
    }

}