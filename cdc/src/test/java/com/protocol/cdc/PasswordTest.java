package com.protocol.cdc;

import org.junit.Test;

import static com.protocol.cdc.Password.SALT_SIZE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PasswordTest {
    @Test
    public void hello() {
        Password password = new Password("p@55word", new byte[SALT_SIZE]);
        byte[] encoded = password.encrypt("hello".getBytes());
        assertThat(Digest.hex(encoded), equalTo("68656c6c6f"));
        assertThat(password.decrypt(encoded), equalTo("hello".getBytes()));
    }
    @Test
    public void long_text() {
        Password password = new Password("p@55word", new byte[SALT_SIZE]);
        String text = "You should learn about bytes, numeral systems, characters and their encoding in bits, called 'character encoding'";
        byte[] encoded = password.encrypt(text.getBytes());
        assertThat(password.decrypt(encoded), equalTo(text.getBytes()));
    }
}
