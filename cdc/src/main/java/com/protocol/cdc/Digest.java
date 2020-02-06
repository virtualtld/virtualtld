package com.protocol.cdc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Digest {

    public static String sha1(byte[] chunk1, byte[] chunk2) {
        MessageDigest crypt = sha1();
        crypt.reset();
        crypt.update(chunk1);
        crypt.update(chunk2);
        return base64(crypt.digest());
    }

    public static byte[] sha1Bytes(byte[] chunk) {
        MessageDigest crypt = sha1();
        crypt.reset();
        crypt.update(chunk);
        return crypt.digest();
    }

    public static String base64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static MessageDigest sha1() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
