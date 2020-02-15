package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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
        Assert.assertThat(Digest.hex(chunk1.digestBytes()), equalTo(
                "983589535e05c495ffeae4b0b31ddcfafe92a763"));
        EncodedBodyChunk chunk2 = chunks.get(1);
        Assert.assertThat(chunk2.decodedData().length, equalTo(81));
        Assert.assertThat(Digest.hex(chunk2.digestBytes()), equalTo(
                "f8ebbbe3ad6a8cfd13607fd3a7fad7a3a7a50158"));
    }

    @Test
    public void head_has_one_node() {
        EncodedFile encodedFile = newEncodedFile(new byte[512]);
        List<EncodedHeadNode> nodes = encodedFile.head();
        Assert.assertThat(nodes.size(), is(1));
        DecodedHeadNode node = new DecodedHeadNode(nodes.get(0).data());
        Assert.assertThat(node.flag(), equalTo(FLAG_SALT));
        Assert.assertThat(Digest.hex(node.salt()), equalTo(
                "5c3eb800"));
        Assert.assertThat(node.chunkDigests(), equalTo(
                Arrays.asList("983589535e05c495ffeae4b0b31ddcfafe92a763", "f8ebbbe3ad6a8cfd13607fd3a7fad7a3a7a50158")));
    }

    @Test
    public void head_has_two_nodes() {
        EncodedFile encodedFile = newEncodedFile(new byte[9052]);
        List<EncodedHeadNode> nodes = encodedFile.head();
        Assert.assertThat(nodes.size(), is(2));
        DecodedHeadNode node1 = new DecodedHeadNode(nodes.get(0).data());
        Assert.assertThat(node1.flag(), equalTo((byte) (FLAG_SALT | FLAG_NEXT)));
        DecodedHeadNode node2 = new DecodedHeadNode(nodes.get(1).data());
        Assert.assertThat(node2.flag(), equalTo((byte) 0));
    }

    @Test
    public void head_has_three_nodes() {
        EncodedFile encodedFile = newEncodedFile(new byte[17672]);
        List<EncodedHeadNode> nodes = encodedFile.head();
        Assert.assertThat(nodes.size(), is(3));
        DecodedHeadNode node1 = new DecodedHeadNode(nodes.get(0).data());
        Assert.assertThat(node1.flag(), equalTo((byte) (FLAG_SALT | FLAG_NEXT)));
        DecodedHeadNode node2 = new DecodedHeadNode(nodes.get(1).data());
        Assert.assertThat(node2.flag(), equalTo(FLAG_NEXT));
        DecodedHeadNode node3 = new DecodedHeadNode(nodes.get(2).data());
        Assert.assertThat(node3.flag(), equalTo((byte) 0));
    }

    private static EncodedFile newEncodedFile(byte[] content) {
        VirtualtldSite site = new VirtualtldSite("最新版本.com", "最新版本.xyz");
        return new EncodedFile(site.publicDomain, "/", content, 512);
    }
}
