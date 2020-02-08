package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.protocol.cdc.Digest.base64;
import static com.protocol.cdc.EncodedHeadNode.FLAG_NEXT;
import static com.protocol.cdc.EncodedHeadNode.FLAG_SALT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class EncodedFileTest {
    @Test
    public void body_has_one_chunk() {
        EncodedFile encodedFile = newEncodedFile("hello".getBytes());
        List<EncodedBodyChunk> chunks = encodedFile.body();
        Assert.assertThat(chunks.size(), is(1));
        Assert.assertThat(chunks.get(0).decodedData(), equalTo("hello".getBytes()));
    }

    @Test
    public void body_has_two_chunks() {
        EncodedFile encodedFile = newEncodedFile(new byte[512]);
        List<EncodedBodyChunk> chunks = encodedFile.body();
        Assert.assertThat(chunks.size(), is(2));
        EncodedBodyChunk chunk1 = chunks.get(0);
        Assert.assertThat(chunk1.decodedData().length, equalTo(431));
        Assert.assertThat(base64(chunk1.digestBytes()), equalTo(
                "2l2EO4S6yQhd5uiISq49DJcgP+U="));
        EncodedBodyChunk chunk2 = chunks.get(1);
        Assert.assertThat(chunk2.decodedData().length, equalTo(81));
        Assert.assertThat(base64(chunk2.digestBytes()), equalTo(
                "Aar6AtfY07ragpI3npgm1CdT0vo="));
    }

    @Test
    public void head_has_one_node() {
        EncodedFile encodedFile = newEncodedFile(new byte[512]);
        List<EncodedHeadNode> nodes = encodedFile.head();
        Assert.assertThat(nodes.size(), is(1));
        DecodedHeadNode node = new DecodedHeadNode(nodes.get(0).data());
        Assert.assertThat(node.flag(), equalTo(FLAG_SALT));
        Assert.assertThat(base64(node.salt()), equalTo(
                "XD64AGZCAAI="));
        Assert.assertThat(node.chunkDigests(), equalTo(
                Arrays.asList("2l2EO4S6yQhd5uiISq49DJcgP+U=", "Aar6AtfY07ragpI3npgm1CdT0vo=")));
    }

    @Test
    public void head_has_two_nodes() {
        EncodedFile encodedFile = newEncodedFile(new byte[9052]);
        List<EncodedHeadNode> nodes = encodedFile.head();
        Assert.assertThat(nodes.size(), is(2));
        DecodedHeadNode node1 = new DecodedHeadNode(nodes.get(0).data());
        Assert.assertThat(node1.flag(), equalTo((byte)(FLAG_SALT | FLAG_NEXT)));
        DecodedHeadNode node2 = new DecodedHeadNode(nodes.get(1).data());
        Assert.assertThat(node2.flag(), equalTo((byte) 0));
    }

    @Test
    public void head_has_three_nodes() {
        EncodedFile encodedFile = newEncodedFile(new byte[17672]);
        List<EncodedHeadNode> nodes = encodedFile.head();
        Assert.assertThat(nodes.size(), is(3));
        DecodedHeadNode node1 = new DecodedHeadNode(nodes.get(0).data());
        Assert.assertThat(node1.flag(), equalTo((byte)(FLAG_SALT | FLAG_NEXT)));
        DecodedHeadNode node2 = new DecodedHeadNode(nodes.get(1).data());
        Assert.assertThat(node2.flag(), equalTo(FLAG_NEXT));
        DecodedHeadNode node3 = new DecodedHeadNode(nodes.get(2).data());
        Assert.assertThat(node3.flag(), equalTo((byte) 0));
    }

    private static EncodedFile newEncodedFile(byte[] content) {
        CdcSite site = new CdcSite("最新版本.com", "最新版本.xyz");
        return new EncodedFile(site, content);
    }
}
