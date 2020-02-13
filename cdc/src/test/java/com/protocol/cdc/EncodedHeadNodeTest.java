package com.protocol.cdc;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.protocol.cdc.Digest.hex;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class EncodedHeadNodeTest {

    @Test
    public void without_next() {
        byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
        List<EncodedBodyChunk> chunks = newChunks("hello");
        assertThat(Digest.hex(chunks.get(0).digestBytes()), equalTo("384c28b6b423f6cb0c99d75045423d91daf4c837"));
        EncodedHeadNode encoded = new EncodedHeadNode(
                chunks, salt, null);
        DecodedHeadNode decoded = new DecodedHeadNode(encoded.data());
        assertThat(decoded.salt(), equalTo(salt));
        assertThat(decoded.chunkDigests(), equalTo(Collections.singletonList(
                "384c28b6b423f6cb0c99d75045423d91daf4c837")));
        assertThat(encoded.digest(), equalTo(
                "419543ab9cfb2a63086d09250dc82c3387bf926c"));
    }

    @Test
    public void with_next() {
        byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
        EncodedHeadNode encoded2 = new EncodedHeadNode(
                newChunks("world"), salt, null);
        EncodedHeadNode encoded1 = new EncodedHeadNode(
                newChunks("hello"), salt, encoded2);
        DecodedHeadNode decoded1 = new DecodedHeadNode(encoded1.data());
        assertThat(decoded1.salt(), equalTo(salt));
        assertThat(decoded1.chunkDigests(), equalTo(Collections.singletonList(
                "384c28b6b423f6cb0c99d75045423d91daf4c837")));
        assertThat(decoded1.nextDigest(), equalTo("9b9c0299fe11c7e7b7fa22440c66cbaa1ac30ab1"));
    }

    private static List<EncodedBodyChunk> newChunks(String chunk1) {
        Password password = new Password("p@55word", new byte[8]);
        EncodedBodyChunk chunk = new EncodedBodyChunk(password, chunk1.getBytes());
        return Collections.singletonList(chunk);
    }
}
