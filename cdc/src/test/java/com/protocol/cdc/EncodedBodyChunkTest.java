package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import static com.protocol.cdc.Digest.base64;
import static org.hamcrest.CoreMatchers.equalTo;

public class EncodedBodyChunkTest {

    @Test
    public void digest() {
        Password password = new Password("p@55word", new byte[8]);
        EncodedBodyChunk chunk = new EncodedBodyChunk(password, "hello".getBytes());
        Assert.assertThat(base64(chunk.digest()), equalTo("OEwotrQj9ssMmddQRUI9kdr0yDc="));
    }
}
