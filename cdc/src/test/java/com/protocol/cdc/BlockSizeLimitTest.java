package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.net.IDN;

public class BlockSizeLimitTest {

    @Test
    public void test() throws TextParseException {
        Name privateDomain = Name.fromString(IDN.toASCII("最新版本.xyz."));
        Assert.assertEquals(415, new BlockSizeLimit(privateDomain).limit());
    }
}
