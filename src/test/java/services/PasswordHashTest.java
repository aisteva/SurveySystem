package services;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by arturas on 2017-04-22.
 */
public class PasswordHashTest
{
    PasswordHash ph;
    SaltGenerator sg;

    @Before
    public void setUp() throws Exception
    {
        ph = new PasswordHash();
        sg = new SaltGenerator();
    }

    @Test
    public void testPasswordHashing() throws Exception{
        byte[] salt = sg.generateSalt(12);
        String password = "password";
        byte[] hashedPassword = ph.generatePasswordHashWithSalt(password, salt);
        assertTrue(ph.checkPasswordHashWithSalt(password, salt, hashedPassword));

    }

    @Test
    public void testBase64EncodingAndDecoding() throws Exception{
        byte[] salt = sg.generateSalt(12);
        String password = "password";
        byte[] hashedPassword = ph.generatePasswordHashWithSalt(password, salt);
        String encoded = ph.base64Encode(hashedPassword);
        try
        {
            assertArrayEquals(hashedPassword, ph.base64Decode(encoded));
        } catch (IOException e)
        {
            throw e;
        }
    }

}