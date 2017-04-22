package services;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by arturas on 2017-04-22.
 */
public class EmailServiceTest
{
    @Test
    public void sendEmail() throws Exception
    {
        EmailService s = new EmailService();
        s.sendEmail("arturasfio@gmail.com", "http://www.google.lt");
    }

}