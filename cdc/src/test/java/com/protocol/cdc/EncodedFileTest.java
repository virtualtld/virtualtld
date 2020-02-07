package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.protocol.cdc.EncodedBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.Digest.base64;
import static com.protocol.cdc.Password.SALT_SIZE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class EncodedFileTest {
    @Test
    public void body_has_one_chunk() {
        EncodedFile encodedFile = new EncodedFile("最新版本.xyz", "hello".getBytes());
        List<EncodedBodyChunk> chunks = encodedFile.body();
        Assert.assertThat(chunks.size(), is(1));
        Assert.assertThat(chunks.get(0).decodedData(), equalTo("hello".getBytes()));
    }

    @Test
    public void body_has_two_chunks() {
        EncodedFile encodedFile = new EncodedFile("最新版本.xyz", new byte[512]);
        List<EncodedBodyChunk> chunks = encodedFile.body();
        Assert.assertThat(chunks.size(), is(2));
        EncodedBodyChunk chunk1 = chunks.get(0);
        Assert.assertThat(chunk1.decodedData().length, equalTo(431));
        Assert.assertThat(base64(chunk1.digest()), equalTo(
                "JqXHYQIbjEt0XU1AZhpGVdDDQKM="));
        EncodedBodyChunk chunk2 = chunks.get(1);
        Assert.assertThat(chunk2.decodedData().length, equalTo(81));
        Assert.assertThat(base64(chunk2.digest()), equalTo(
                "ho/OUhau/V2fJz+VMlM+nB5/JLo="));
    }

    @Test
    public void head_has_one_node() {
        EncodedFile encodedFile = new EncodedFile("最新版本.xyz", new byte[512]);
        List<EncodedHeadNode> nodes = encodedFile.head();
        Assert.assertThat(nodes.size(), is(1));
        byte[] data = nodes.get(0).data();
        Assert.assertThat(data[0], equalTo(0));
        Assert.assertThat(base64(data, 1 + SALT_SIZE, DIGEST_SIZE), equalTo(
                "mDWJU14FxJX/6uSwsx3c+v6Sp2M="));
        Assert.assertThat(base64(data, 1 + SALT_SIZE + DIGEST_SIZE, DIGEST_SIZE), equalTo(
                "+Ou7461qjP0TYH/Tp/rXo6elAVg="));
    }
}
