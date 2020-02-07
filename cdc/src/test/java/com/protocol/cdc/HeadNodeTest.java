package com.protocol.cdc;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.protocol.cdc.Digest.base64;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HeadNodeTest {

    @Test
    public void without_next() {
        byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
        EncodedHeadNode encoded = new EncodedHeadNode(newChunks("hello"), salt, null);
        DecodedHeadNode decoded = new DecodedHeadNode(encoded.data());
        assertThat(decoded.salt(), equalTo(salt));
        assertThat(decoded.chunkDigests(), equalTo(Collections.singletonList(
                "AjhMKLa0I/bLDJnXUEVCPZHa9Mg=")));
    }

    @Test
    public void with_next() {
        byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
        EncodedHeadNode node2 = new EncodedHeadNode(newChunks("world"), salt, null);
        EncodedHeadNode node1 = new EncodedHeadNode(newChunks("hello"), salt, node2);
        assertThat(base64(node1.data()), equalTo(
                "AQIDBAUGBwgBOEwotrQj9ssMmddQRUI9kdr0yDfi9hM0D/ukfh4mGfX7cdvNvozQeA=="));
    }

    private static List<EncodedBodyChunk> newChunks(String chunk1) {
        Password password = new Password("p@55word", new byte[8]);
        return Collections.singletonList(new EncodedBodyChunk(password, chunk1.getBytes()));
    }
}
