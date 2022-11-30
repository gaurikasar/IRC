import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 *Class
 * Handles Encryption
 * Encryption Algorithm used is AES-SHA-256
 *
 * */



public class EncryptionHelper {
    private static String ALGORITHM = "AES";
    private static String SHA = "SHA-256";
    private static SecretKeySpec secretKey;
    private static MessageDigest messageDigest;

    public EncryptionHelper() {
        try {
            messageDigest = MessageDigest.getInstance(SHA);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void updateKeySpec(String password) {
        byte[] password_bytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] encryptionKey = messageDigest.digest(password_bytes);
        encryptionKey = Arrays.copyOf(encryptionKey,16);
        secretKey = new SecretKeySpec(encryptionKey,ALGORITHM);
    }

    public String encrypt(String password,String unencryptedString) {
        try {
            updateKeySpec(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(unencryptedString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Encryption Failed";
    }

    public String decrypt(String password, String encryptedString) {
        try {
            updateKeySpec(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedString));
            return new String(decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Decryption Failed";
    }

    public static void main(String[] args) {
        EncryptionHelper encryptionHelper = new EncryptionHelper();
        String msg = "This is a test";
        String encryptedMsg = encryptionHelper.encrypt("key",msg);
        System.out.println("Encrypted Message: "+encryptedMsg);
        String decryptedMsg = encryptionHelper.decrypt("key",encryptedMsg);
        System.out.println("Decrypted Message: "+decryptedMsg);

    }
}
