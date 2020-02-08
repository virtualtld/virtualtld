package com.protocol.cdc;

import org.junit.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import static com.protocol.cdc.Digest.base64;
import static com.protocol.cdc.Password.SALT_SIZE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BodyChunkTest {

    @Test
    public void digest() {
        EncodedBodyChunk chunk = newChunk();
        assertThat(base64(chunk.digest()), equalTo("OEwotrQj9ssMmddQRUI9kdr0yDc="));
    }

    @Test
    public void dnsName() {
        EncodedBodyChunk chunk = newChunk();
        assertThat(chunk.dnsName().toString(),
                equalTo("OEwotrQj9ssMmddQRUI9kdr0yDc=.abc.com."));
    }

    private static EncodedBodyChunk newChunk() {
        Password password = new Password("p@55word", new byte[SALT_SIZE]);
        try {
            return new EncodedBodyChunk(new Name("abc.com."), password, "hello".getBytes());
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }
}
