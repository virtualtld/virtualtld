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
        byte[] salt = {1, 2, 3, 4};
        List<EncodedBodyChunk> chunks = newChunks("hello");
        assertThat(Digest.hex(chunks.get(0).digestBytes()), equalTo("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d"));
        EncodedHeadNode encoded = new EncodedHeadNode(
                chunks, salt, null);
        DecodedHeadNode decoded = new DecodedHeadNode(encoded.data());
        assertThat(decoded.salt(), equalTo(salt));
        assertThat(decoded.chunkDigests(), equalTo(Collections.singletonList(
                "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d")));
        assertThat(encoded.digest(), equalTo(
                "a98aedbebe595b7e244186ab696965a39962a579"));
    }

    @Test
    public void with_next() {
        byte[] salt = {1, 2, 3, 4};
        EncodedHeadNode encoded2 = new EncodedHeadNode(
                newChunks("world"), salt, null);
        EncodedHeadNode encoded1 = new EncodedHeadNode(
                newChunks("hello"), salt, encoded2);
        DecodedHeadNode decoded1 = new DecodedHeadNode(encoded1.data());
        assertThat(decoded1.salt(), equalTo(salt));
        assertThat(decoded1.chunkDigests(), equalTo(Collections.singletonList(
                "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d")));
        assertThat(decoded1.nextDigest(), equalTo("4893654d9ab031db94d2020fe72c30d0f081c8db"));
    }

    private static List<EncodedBodyChunk> newChunks(String chunk1) {
        Password password = new Password("p@55word", new byte[8]);
        EncodedBodyChunk chunk = new EncodedBodyChunk(password, chunk1.getBytes());
        return Collections.singletonList(chunk);
    }
}
