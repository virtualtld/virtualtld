package com.protocol.cdc;

import java.net.IDN;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class Password {

    private final SecretKey secretKey;
    private final PBEParameterSpec pbeParameterSpec;
    public final byte[] salt;

    public Password(String password) {
        this(password, randomSalt());
    }

    public Password(String password, byte[] salt) {
        try {
            PBEKeySpec pbeKeySpec = new PBEKeySpec(IDN.toASCII(password).toCharArray());
            SecretKeyFactory secretKeyFactory = SecretKeyFactory
                    .getInstance("PBEWithMD5AndTripleDES");
            secretKey = secretKeyFactory.generateSecret(pbeKeySpec);
            this.salt = salt;
            pbeParameterSpec = new PBEParameterSpec(salt, 100);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] randomSalt() {
        byte[] salt = new byte[8];
        Random random = new Random();
        random.nextBytes(salt);
        return salt;
    }

    public byte[] encrypt(byte[] decoded) {
        Cipher cipher = cipher(Cipher.ENCRYPT_MODE);
        cipher.update(decoded);
        try {
            return cipher.doFinal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(byte[] encoded) {
        Cipher cipher = cipher(Cipher.DECRYPT_MODE);
        cipher.update(encoded);
        try {
            return cipher.doFinal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Cipher cipher(int mode) {
        try {
            Cipher cipher = Cipher.getInstance("PBEWithMD5AndTripleDES");
            cipher.init(mode, secretKey, pbeParameterSpec);
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
