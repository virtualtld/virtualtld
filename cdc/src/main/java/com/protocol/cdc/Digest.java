package com.protocol.cdc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Digest {

    public static String sha1(byte[] chunk1, byte[] chunk2) {
        MessageDigest crypt = sha1();
        crypt.reset();
        crypt.update(chunk1);
        crypt.update(chunk2);
        return hex(crypt.digest());
    }

    public static String sha1(byte[] chunk) {
        return hex(sha1Bytes(chunk));
    }

    public static byte[] sha1Bytes(byte[] chunk) {
        MessageDigest crypt = sha1();
        crypt.reset();
        crypt.update(chunk);
        return crypt.digest();
    }

    public static String hex(byte[] data) {
        Formatter formatter = new Formatter();
        for (byte b : data) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String hex(byte[] data, int offset, int length) {
        Formatter formatter = new Formatter();
        for (int i = offset; i < offset + length; i++) {
            formatter.format("%02x", data[i]);
        }
        return formatter.toString();
    }

    private static MessageDigest sha1() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
