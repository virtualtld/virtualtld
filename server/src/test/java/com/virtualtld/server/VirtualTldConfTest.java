package com.virtualtld.server;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class VirtualTldConfTest {
    @Test
    public void test_correct_conf() {
        VirtualTldConf conf = VirtualTldConf.parse(Arrays.asList(
                "Version=1.1",
                "PublicDomain=最新版本.com",
                "PrivateDomain=最新版本.xyz",
                "PrivateResolver=1.1.1.1:53"));
        Assert.assertEquals(1, conf.majorVersion);
        Assert.assertEquals(1, conf.minorVersion);
        Assert.assertEquals("最新版本.com", conf.publicDomain);
        Assert.assertEquals("最新版本.xyz", conf.privateDomain);
        Assert.assertEquals("/1.1.1.1:53", conf.privateResolvers.get(0).toString());
    }
}
