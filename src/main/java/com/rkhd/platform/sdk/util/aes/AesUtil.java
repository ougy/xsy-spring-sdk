package com.rkhd.platform.sdk.util.aes;

import com.rkhd.platform.sdk.exception.AesException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AesUtil {
    private static final String KEY_ALGORITHM = "AES";
    private String algorithm;
    private SecretkeyCreator secretkeyCreator;

    private AesUtil(String algorithm, SecretkeyCreator secretkeyCreator) {
        this.algorithm = algorithm;
        this.secretkeyCreator = secretkeyCreator;
    }

    public static AesUtil instance(String algorithm) {
        return new AesUtil(algorithm, new DefaultSecretKeyCreator());
    }

    public static AesUtil instance(String algorithm, SecretkeyCreator secretkeyCreator) {
        return new AesUtil(algorithm, secretkeyCreator);
    }

    public String encrypt(String content, String key) throws AesException {
        try {
            Cipher cipher = Cipher.getInstance(this.algorithm);

            byte[] byteContent = content.getBytes("utf-8");

            cipher.init(1, this.secretkeyCreator.createSecretKey(key));

            byte[] result = cipher.doFinal(byteContent);

            return (new Base64()).encodeToString(result);
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    public String decrypt(String content, String key) throws AesException {
        try {
            Cipher cipher = Cipher.getInstance(this.algorithm);
            cipher.init(2, this.secretkeyCreator.createSecretKey(key));

            byte[] result = cipher.doFinal((new Base64()).decode(content));

            return new String(result, "utf-8");
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    private static class DefaultSecretKeyCreator implements SecretkeyCreator {
        private DefaultSecretKeyCreator() {
        }

        public SecretKeySpec createSecretKey(String key) throws AesException {
            try {
                KeyGenerator kg = null;
                kg = KeyGenerator.getInstance("AES");

                kg.init(128, new SecureRandom(key.getBytes()));

                SecretKey secretKey = kg.generateKey();

                return new SecretKeySpec(secretKey.getEncoded(), "AES");
            } catch (Exception e) {
                throw new AesException(e);
            }
        }
    }

    public static interface Algorithm {
        public static final String PKCS5Padding = "AES/ECB/PKCS5Padding";
        public static final String PKCS7Padding = "AES/ECB/PKCS7Padding";
    }
}