package com.qiuyj.qrpc.utils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

/**
 * @author qiuyj
 * @since 2020-05-23
 */
@SuppressWarnings("unused")
public abstract class RsaUtils {

    private static final String DEFAULT_KEY_ALG = "RSA";

    private static final int DEFAULT_KEY_LEN = 1024;

    private static final String DEFAULT_CIPHER_ALG = DEFAULT_KEY_ALG;

    public static class RSAKeyPair {

        /**
         * 公钥
         */
        public final RSAPublicKey publicKey;

        /**
         * 私钥
         */
        public final RSAPrivateKey privateKey;

        private String urledPublicKeyString;

        private String publicKeyString;

        private String urledPrivateKeyString;

        private String privateKeyString;

        private RSAKeyPair(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String encodePublicKeyToString(boolean url) {
            if (url) {
                return Optional.ofNullable(urledPublicKeyString)
                        .orElseGet(() -> {
                            byte[] b = Base64.getUrlEncoder().encode(publicKey.getEncoded());
                            return urledPublicKeyString = new String(b, StandardCharsets.UTF_8);
                        });
            }
            else {
                return Optional.ofNullable(publicKeyString)
                        .orElseGet(() -> {
                            byte[] b = Base64.getEncoder().encode(publicKey.getEncoded());
                            return publicKeyString = new String(b, StandardCharsets.UTF_8);
                        });
            }
        }

        public String encodePrivateKeyToString(boolean url) {
            if (url) {
                return Optional.ofNullable(urledPrivateKeyString)
                        .orElseGet(() -> {
                            byte[] b = Base64.getUrlEncoder().encode(privateKey.getEncoded());
                            return urledPrivateKeyString = new String(b, StandardCharsets.UTF_8);
                        });
            }
            else {
                return Optional.ofNullable(privateKeyString)
                        .orElseGet(() -> {
                            byte[] b = Base64.getEncoder().encode(privateKey.getEncoded());
                            return privateKeyString = new String(b, StandardCharsets.UTF_8);
                        });
            }
        }
    }

    public static RSAKeyPair keygen() throws NoSuchAlgorithmException {
        return keygen(DEFAULT_KEY_LEN);
    }

    public static RSAKeyPair keygen(int keyLen) throws NoSuchAlgorithmException {
        checkRsaKeyLen(keyLen);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(DEFAULT_KEY_ALG);
        kpg.initialize(keyLen);
        KeyPair kp = kpg.generateKeyPair();
        return new RSAKeyPair((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate());
    }

    private static void checkRsaKeyLen(int keyLen) {
        if ((keyLen > 0 && keyLen <= 1024) || keyLen == 2048 || keyLen == 3072) {
            return;
        }
        throw new InvalidParameterException("Invalid RSA key length: " + keyLen);
    }

    public static String encryptToUrlSupportedString(String content, String publicKey) throws GeneralSecurityException {
        return encryptToString(content, publicKey, true);
    }

    public static String encryptToString(String content, String publicKey) throws GeneralSecurityException {
        return encryptToString(content, publicKey, false);
    }

    public static String encryptToString(String content, String publicKey, boolean url) throws GeneralSecurityException {
        return encryptToString(content.getBytes(StandardCharsets.UTF_8), publicKey, DEFAULT_KEY_ALG, url);
    }

    public static String encryptToString(byte[] content, String publicKey, String keyAlg, boolean url) throws GeneralSecurityException {
        byte[] publicKeyBytes = (url ? Base64.getUrlDecoder() : Base64.getDecoder()).decode(publicKey.getBytes(StandardCharsets.UTF_8));
        RSAPublicKey rpk = (RSAPublicKey) KeyFactory.getInstance(keyAlg).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        return encryptToString(content, rpk, DEFAULT_CIPHER_ALG, url);
    }

    public static String encryptToString(byte[] content, RSAPublicKey publicKey, String cipherAlg, boolean url) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(cipherAlg);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] b = (url ? Base64.getUrlEncoder() : Base64.getEncoder()).encode(cipher.doFinal(content));
        return new String(b, StandardCharsets.UTF_8);
    }

    public static String encryptToString(byte[] content, RSAKeyPair kp, String cipherAlg, boolean url) throws GeneralSecurityException {
        return encryptToString(content, kp.publicKey, cipherAlg, url);
    }

    public static String encryptToString(byte[] content, RSAKeyPair kp) throws GeneralSecurityException {
        return encryptToString(content, kp.publicKey, DEFAULT_CIPHER_ALG, false);
    }

    public static String encryptToUrlSupportedString(byte[] content, RSAKeyPair kp) throws GeneralSecurityException {
        return encryptToString(content, kp.publicKey, DEFAULT_CIPHER_ALG, true);
    }

    public static String decryptToString(String content, String privateKey) throws GeneralSecurityException {
        return decryptToString(content, privateKey, false);
    }

    public static String decryptUrlSupportedStringToString(String content, String privateKey) throws GeneralSecurityException {
        return decryptToString(content, privateKey, true);
    }

    public static String decryptToString(String content, String privateKey, boolean url) throws GeneralSecurityException {
        byte[] b = decrypt(content, privateKey, DEFAULT_KEY_ALG, url);
        return new String(b, StandardCharsets.UTF_8);
    }

    public static byte[] decrypt(String content, String privateKey, String keyAlg, boolean url) throws GeneralSecurityException {
        byte[] privateKeyBytes = (url ? Base64.getUrlDecoder() : Base64.getDecoder()).decode(privateKey.getBytes(StandardCharsets.UTF_8));
        RSAPrivateKey rpk = (RSAPrivateKey) KeyFactory.getInstance(keyAlg).generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        return decrypt(content, rpk, DEFAULT_CIPHER_ALG, url);
    }

    public static byte[] decrypt(String content, RSAPrivateKey privateKey, String cipherAlg, boolean url) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(cipherAlg);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] b = (url ? Base64.getUrlDecoder() : Base64.getDecoder()).decode(content);
        return cipher.doFinal(b);
    }

    public static byte[] decrypt(String content, RSAKeyPair kp, String cipherAlg, boolean url) throws GeneralSecurityException {
        return decrypt(content, kp.privateKey, cipherAlg, url);
    }

    public static byte[] decrypt(String content, RSAKeyPair kp) throws GeneralSecurityException {
        return decrypt(content, kp.privateKey, DEFAULT_CIPHER_ALG, false);
    }

    public static String decryptToString(String content, RSAKeyPair kp) throws GeneralSecurityException {
        byte[] b = decrypt(content, kp);
        return new String(b, StandardCharsets.UTF_8);
    }
}
