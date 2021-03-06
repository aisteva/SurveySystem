package services;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import services.interfaces.PasswordHasher;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Created by arturas on 2017-04-12.
 * Slaptažodžių hash + salt kūrimo klasė
 */
@ApplicationScoped
public class PasswordHash implements PasswordHasher
{
    @Inject
    SaltGenerator sg;

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    public byte[] generatePasswordHashWithSalt(String password, byte[] salt)
    {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password.toCharArray(), Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public boolean checkPasswordHashWithSalt(String password, byte[] salt, byte[] expectedHash)
    {
        byte[] pwdHash = generatePasswordHashWithSalt(password, salt);

        Arrays.fill(password.toCharArray(), Character.MIN_VALUE);
        if (pwdHash.length != expectedHash.length){
            return false;
        }
        for (int i = 0; i < pwdHash.length; i++) {
            if (pwdHash[i] != expectedHash[i]) return false;
        }
        return true;
    }

    public String hashPassword(String unhashedPassword)
    {
        byte[] salt = sg.generateSalt(32);
        byte[] hashedPass;
        hashedPass = generatePasswordHashWithSalt(unhashedPassword, salt);
        return (base64Encode(concat(salt, hashedPass)));

    }

    private byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public String base64Encode(byte[] bytes)
    {
        return new BASE64Encoder().encode(bytes);
    }

    public byte[] base64Decode(String s) throws IOException
    {
        return new BASE64Decoder().decodeBuffer(s);
    }
}

