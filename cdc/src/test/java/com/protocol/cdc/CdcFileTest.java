package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class CdcFileTest {
    @Test
    public void one_chunk() {
        CdcFile cdcFile = new CdcFile("最新版本.xyz", "hello".getBytes());
        Assert.assertThat(cdcFile.chunks().size(), is(1));
        Assert.assertThat(cdcFile.chunks().get(0).data(), equalTo("hello".getBytes()));
    }
}
