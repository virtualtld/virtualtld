package com.protocol.cdc;

public class Password {

    public final static int SALT_SIZE = 4;
    public final byte[] salt;
    private final byte[] dict;

    public Password(String password, byte[] salt) {
        try {
            long seed = ((long) (password.hashCode())) * byteArrayToLong(salt);
            dict = new byte[512];
            for (int i = 0; i < dict.length; i++) {
                dict[i] = (byte) (31 * seed);
            }
            this.salt = salt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encrypt(byte[] decoded) {
        byte[] encrypted = new byte[decoded.length];
        for (int i = 0; i < decoded.length; i++) {
            byte b = decoded[i];
            encrypted[i] = (byte) (b ^ dict[i]);
        }
        return encrypted;
    }

    public byte[] decrypt(byte[] encoded) {
        return this.encrypt(encoded);
    }

    private static long byteArrayToLong(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
}
