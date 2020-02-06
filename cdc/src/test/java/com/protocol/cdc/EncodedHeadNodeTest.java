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
        EncodedHeadNode node = new EncodedHeadNode(newChunks("hello"), null);
        Assert.assertThat(base64(node.data()), equalTo("AKr0xh3cxeii2r7eDztILNmuqUNN"));
        Assert.assertThat(base64(node.digest()), equalTo("ti/p2xwyZcn9UKmU27ikEO23NyU="));
    }

    @Test
    public void with_next() {
        EncodedHeadNode node2 = new EncodedHeadNode(newChunks("world"), null);
        EncodedHeadNode node1 = new EncodedHeadNode(newChunks("hello"), node2);
        Assert.assertThat(base64(node1.data()), equalTo(
                "Aar0xh3cxeii2r7eDztILNmuqUNNXdmIbFvbhBgzZMVe8zzE76z3TAA="));
    }

    private static List<EncodedBodyChunk> newChunks(String chunk1) {
        Password password = new Password("p@55word", new byte[8]);
        return Collections.singletonList(new EncodedBodyChunk(password, chunk1.getBytes()));
    }
}
