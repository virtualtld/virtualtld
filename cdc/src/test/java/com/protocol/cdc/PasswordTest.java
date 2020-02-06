package com.protocol.cdc;

import org.junit.Test;

import static com.protocol.cdc.Digest.base64;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PasswordTest {
    @Test
    public void encrypt_decrypt() {
        Password password = new Password("p@55word", new byte[8]);
        byte[] encoded = password.encrypt("hello".getBytes());
        assertThat(base64(encoded), equalTo("PvYmTUMG/8Q="));
        assertThat(password.decrypt(encoded), equalTo("hello".getBytes()));
    }
}
