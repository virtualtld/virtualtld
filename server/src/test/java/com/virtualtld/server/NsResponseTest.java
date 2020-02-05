package com.virtualtld.server;

import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.Message;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;

public class NsResponseTest {

    @Test
    public void test() {
        Message nsResp = new NsResponse(VirtualTldConf.parse(Arrays.asList(
                "Version=1.1",
                "PublicDomain=最新版本.com",
                "PrivateDomain=最新版本.xyz",
                "PrivateResolver=1.1.1.1:53"
        ))).nsResponse();
        Assert.assertThat(nsResp.toString(), containsString("xn--efv12a2dz86b.com.\t4090\tIN\tNS\tver.1.1.virtualtld.com."));
    }
}
