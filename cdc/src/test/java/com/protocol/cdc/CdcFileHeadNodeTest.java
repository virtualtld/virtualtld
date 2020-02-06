package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.protocol.cdc.Digest.base64;
import static org.hamcrest.CoreMatchers.equalTo;

public class CdcFileHeadNodeTest {

    @Test
    public void without_next() {
        CdcFileHeadNode node = new CdcFileHeadNode(newChunks("hello"), null);
        Assert.assertThat(base64(node.data()), equalTo("AKr0xh3cxeii2r7eDztILNmuqUNN"));
        Assert.assertThat(base64(node.digest()), equalTo("ti/p2xwyZcn9UKmU27ikEO23NyU="));
    }

    @Test
    public void with_next() {
        CdcFileHeadNode node2 = new CdcFileHeadNode(newChunks("world"), null);
        CdcFileHeadNode node1 = new CdcFileHeadNode(newChunks("hello"), node2);
        Assert.assertThat(base64(node1.data()), equalTo(
                "Aar0xh3cxeii2r7eDztILNmuqUNNXdmIbFvbhBgzZMVe8zzE76z3TAA="));
    }

    private static List<CdcFileBodyChunk> newChunks(String chunk1) {
        return Collections.singletonList(new CdcFileBodyChunk(chunk1.getBytes()));
    }
}
