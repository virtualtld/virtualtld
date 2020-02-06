package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.protocol.cdc.CdcFileBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.EncodedHeadNode.LAST_NODE;
import static com.protocol.cdc.Digest.base64;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class CdcFileTest {
    @Test
    public void body_has_one_chunk() {
        CdcFile cdcFile = new CdcFile("最新版本.xyz", "hello".getBytes());
        List<CdcFileBodyChunk> chunks = cdcFile.body();
        Assert.assertThat(chunks.size(), is(1));
        Assert.assertThat(chunks.get(0).data(), equalTo("hello".getBytes()));
    }

    @Test
    public void body_has_two_chunks() {
        CdcFile cdcFile = new CdcFile("最新版本.xyz", new byte[512]);
        List<CdcFileBodyChunk> chunks = cdcFile.body();
        Assert.assertThat(chunks.size(), is(2));
        CdcFileBodyChunk chunk1 = chunks.get(0);
        Assert.assertThat(chunk1.data().length, equalTo(431));
        Assert.assertThat(base64(chunk1.digest()), equalTo(
                "mDWJU14FxJX/6uSwsx3c+v6Sp2M="));
        CdcFileBodyChunk chunk2 = chunks.get(1);
        Assert.assertThat(chunk2.data().length, equalTo(81));
        Assert.assertThat(base64(chunk2.digest()), equalTo(
                "+Ou7461qjP0TYH/Tp/rXo6elAVg="));
    }

    @Test
    public void head_has_one_node() {
        CdcFile cdcFile = new CdcFile("最新版本.xyz", new byte[512]);
        List<EncodedHeadNode> nodes = cdcFile.head();
        Assert.assertThat(nodes.size(), is(1));
        byte[] data = nodes.get(0).data();
        Assert.assertThat(data[0], equalTo(LAST_NODE));
        Assert.assertThat(base64(data, 1, DIGEST_SIZE), equalTo(
                "mDWJU14FxJX/6uSwsx3c+v6Sp2M="));
        Assert.assertThat(base64(data, 1 + DIGEST_SIZE, DIGEST_SIZE), equalTo(
                "+Ou7461qjP0TYH/Tp/rXo6elAVg="));
    }
}
