package services;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by vdeiv on 2017-05-22.
 */
public interface ISaltGenerator {

    String getRandomString(int length);

    byte[] generateSalt(int byteNumber);
}
