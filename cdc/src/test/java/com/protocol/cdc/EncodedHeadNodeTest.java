package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.protocol.cdc.Digest.base64;
import static org.hamcrest.CoreMatchers.equalTo;

public class EncodedHeadNodeTest {

    @Test
    public void without_next() {
        byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
        EncodedHeadNode node = new EncodedHeadNode(newChunks("hello"), null, salt);
        Assert.assertThat(base64(node.data()), equalTo("AQIDBAUGBwgAOEwotrQj9ssMmddQRUI9kdr0yDc="));
        Assert.assertThat(base64(node.digest()), equalTo("+R9C+oJgJmVqrkl1ECaylkveB3o="));
    }

    @Test
    public void with_next() {
        byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
        EncodedHeadNode node2 = new EncodedHeadNode(newChunks("world"), null);
        EncodedHeadNode node1 = new EncodedHeadNode(newChunks("hello"), node2, salt);
        Assert.assertThat(base64(node1.data()), equalTo(
                "AQIDBAUGBwgBOEwotrQj9ssMmddQRUI9kdr0yDfi9hM0D/ukfh4mGfX7cdvNvozQeA=="));
    }

    private static List<EncodedBodyChunk> newChunks(String chunk1) {
        Password password = new Password("p@55word", new byte[8]);
        return Collections.singletonList(new EncodedBodyChunk(password, chunk1.getBytes()));
    }
}
