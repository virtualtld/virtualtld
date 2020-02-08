package com.protocol.cdc;

import org.junit.Test;

import static com.protocol.cdc.Password.SALT_SIZE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class EncodedBodyChunkTest {

    @Test
    public void digest() {
        EncodedBodyChunk chunk = newChunk();
        assertThat(chunk.digest(), equalTo("OEwotrQj9ssMmddQRUI9kdr0yDc="));
    }

    private static EncodedBodyChunk newChunk() {
        Password password = new Password("p@55word", new byte[SALT_SIZE]);
        return new EncodedBodyChunk(password, "hello".getBytes());
    }
}
