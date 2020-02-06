package com.protocol.cdc;

import com.protocol.dns.DnsName;

import org.junit.Assert;
import org.junit.Test;

public class CdcUrlTest {
    @Test
    public void test_encode() {
        CdcSite site = new CdcSite("最新版本.com", "最新版本.xyz");
        DnsName dnsName = CdcUrl.encode(site, "");
        Assert.assertEquals("n7B5G0YvpBA0elSBz5OLbBTbJnU=.xn--efv12a2dz86b.xyz", dnsName.toString());
    }
}
