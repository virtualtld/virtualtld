package com.protocol.cdc;

import org.junit.Test;

import static com.protocol.cdc.Digest.hex;
import static com.protocol.cdc.Password.SALT_SIZE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PasswordTest {
    @Test
    public void encrypt_decrypt() {
        Password password = new Password("p@55word", new byte[SALT_SIZE]);
        byte[] encoded = password.encrypt("hello".getBytes());
        assertThat(Digest.hex(encoded), equalTo("PvYmTUMG/8Q="));
        assertThat(password.decrypt(encoded), equalTo("hello".getBytes()));
    }
}
