package dcs;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Hex;

// global security configuration
public class SecurityConfiguration {
    // the number of iterations to use for the hashing of passwords
    public static final int ITERATIONS = 1000;
    // the size of the key to generate
    public static final int KEY_SIZE = 256;
    // the size of the salt to generate (bytes)
    public static final int SALT_SIZE = 16;

    // requirements for password
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MIN_LOWERCASE = 1;
    public static final int PASSWORD_MIN_UPPERCASE = 1;
    public static final int PASSWORD_MIN_DIGITS = 1;
    public static final int PASSWORD_MIN_SYMBOLS = 1;

    // create a cryptographically secure pseudo random number generator
    private static SecureRandom cprng = new SecureRandom();

    // hash a password using PBKDF2
    public static String pbkdf2(String password, String salt, int iterations, int keySize) {
        // convert the username and password to char and byte arrays
        char[] pwd = password.toCharArray();
        byte[] slt = salt.getBytes();

        try {
            // initialise the crypto classes with the desired configuration
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(pwd, slt, iterations, keySize);

            // hash the password using the configuration
            SecretKey key = skf.generateSecret(spec);
            byte[] res = key.getEncoded();

            // return the hashed password as a hexadecimal string
            return Hex.encodeHexString(res);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    // return a newly generated salt
    public static String generateSalt() {

        // generate a 16-byte salt using a cprng
        byte[] salt = new byte[SALT_SIZE];
        cprng.nextBytes(salt);
        return Hex.encodeHexString(salt);
    }

    // whether the security configuration has changed since
    // the user's configuration was last updated
    public static boolean hasChanged(DCSUser user) {
        return user.getIterations()    != ITERATIONS
            || user.getKeySize()       != KEY_SIZE
            || user.getSalt().length() != SALT_SIZE * 2;
    }

    public static boolean isPasswordStrongEnough(String password) {

        // reject if less than minimum length
        if (password.length() < PASSWORD_MIN_LENGTH) return false;

        // calculate number of each type of character
        int num_lowercase = 0;
        int num_uppercase = 0;
        int num_digits = 0;
        int num_symbols = 0;
        for (char chr: password.toCharArray()) {
            if ( Character.isLowerCase(chr))     ++num_lowercase;
            if ( Character.isUpperCase(chr))     ++num_uppercase;
            if ( Character.isDigit(chr))         ++num_digits;
            if (!Character.isLetterOrDigit(chr)) ++num_symbols;
        }

        // allow if satisfies minimum requirements
        return num_lowercase >= PASSWORD_MIN_LOWERCASE
            && num_uppercase >= PASSWORD_MIN_UPPERCASE
            && num_digits    >= PASSWORD_MIN_DIGITS
            && num_symbols   >= PASSWORD_MIN_SYMBOLS;
    }
}