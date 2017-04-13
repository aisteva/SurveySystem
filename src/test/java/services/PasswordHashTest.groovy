package services

import org.junit.Before

import javax.inject.Inject

/**
 * Created by arturas on 2017-04-13.
 */
class PasswordHashTest extends GroovyTestCase {

    PasswordHash ph;
    SaltGenerator sg;

    @Before
    void setUp()
    {
        ph = new PasswordHash();
        sg = new SaltGenerator();
    }

    void testPasswordHashing() {
        byte[] salt = sg.generateSalt(12);
        String password = "password";
        byte[] hashedPassword = ph.generatePasswordHashWithSalt(password, salt);
        assertTrue(ph.checkPasswordHashWithSalt(password, salt, hashedPassword));

    }

    void testBase64EncodingAndDecoding() {
        byte[] salt = sg.generateSalt(12);
        String password = "password";
        byte[] hashedPassword = ph.generatePasswordHashWithSalt(password, salt);
        String encoded = ph.base64Encode(hashedPassword);
        assertEquals(hashedPassword, ph.base64Decode(encoded));
    }

}
