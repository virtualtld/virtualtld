package com.protocol.cdc;

import org.junit.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.util.Collections;
import java.util.List;

import static com.protocol.cdc.Digest.base64;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HeadNodeTest {

    @Test
    public void without_next() throws TextParseException {
        byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
        List<EncodedBodyChunk> chunks = newChunks("hello");
        assertThat(base64(chunks.get(0).digest()), equalTo("OEwotrQj9ssMmddQRUI9kdr0yDc="));
        EncodedHeadNode encoded = new EncodedHeadNode(
                Name.fromString("abc.com."), chunks, salt, null);
        DecodedHeadNode decoded = new DecodedHeadNode(encoded.data());
        assertThat(decoded.salt(), equalTo(salt));
        assertThat(decoded.chunkDigests(), equalTo(Collections.singletonList(
                "OEwotrQj9ssMmddQRUI9kdr0yDc=")));
        assertThat(encoded.dnsName().toString(), equalTo(
                "QZVDq5z7KmMIbQklDcgsM4e/kmw=.abc.com."));
    }

    @Test
    public void with_next() throws TextParseException {
        byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
        EncodedHeadNode encoded2 = new EncodedHeadNode(
                Name.fromString("abc.com."), newChunks("world"), salt, null);
        EncodedHeadNode encoded1 = new EncodedHeadNode(
                Name.fromString("abc.com."), newChunks("hello"), salt, encoded2);
        DecodedHeadNode decoded1 = new DecodedHeadNode(encoded1.data());
        assertThat(decoded1.salt(), equalTo(salt));
        assertThat(decoded1.chunkDigests(), equalTo(Collections.singletonList(
                "OEwotrQj9ssMmddQRUI9kdr0yDc=")));
        assertThat(decoded1.nextDigest(), equalTo("m5wCmf4Rx+e3+iJEDGbLqhrDCrE="));
    }

    private static List<EncodedBodyChunk> newChunks(String chunk1) {
        Password password = new Password("p@55word", new byte[8]);
        try {
            EncodedBodyChunk chunk = new EncodedBodyChunk(
                    Name.fromString("abc.com."), password, chunk1.getBytes());
            return Collections.singletonList(chunk);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }
}
