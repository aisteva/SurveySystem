package services.interfaces;

import java.io.IOException;

/**
 * Created by arturas on 2017-05-28.
 * Password hashing interface
 */
public interface PasswordHasher
{
    byte[] generatePasswordHashWithSalt(String password, byte[] salt);
    boolean checkPasswordHashWithSalt(String password, byte[] salt, byte[] expectedHash);
    String hashPassword(String unhashedPassword);
    String base64Encode(byte[] bytes);
    byte[] base64Decode(String s) throws IOException;
}
