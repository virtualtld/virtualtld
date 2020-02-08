package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.net.IDN;

public class ChunkSizeLimitTest {

    @Test
    public void test() throws TextParseException {
        Name privateDomain = Name.fromString(IDN.toASCII("最新版本.xyz."));
        Assert.assertEquals(431, new ChunkSizeLimit(privateDomain).limit());
    }
}
