package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

public class ChunkSizeLimitTest {

    @Test
    public void test() {
        Assert.assertEquals(431, new ChunkSizeLimit("最新版本.xyz").limit());
    }
}
