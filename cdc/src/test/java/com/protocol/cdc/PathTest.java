package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

public class PathTest {
    @Test
    public void test_encode() {
        CdcSite site = new CdcSite("最新版本.com", "最新版本.xyz");
        EncodedPath path = new EncodedPath(site, "");
        Assert.assertEquals("r8eMbUUdjj2cPfqeMwBJG8amEPQ=.xn--efv12a2dz86b.xyz.",
                path.dnsName().toString());
    }
}
