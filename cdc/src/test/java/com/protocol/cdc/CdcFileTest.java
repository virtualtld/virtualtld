package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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
        Assert.assertThat(chunks.get(0).data().length, equalTo(431));
        Assert.assertThat(chunks.get(1).data().length, equalTo(81));
    }
}
