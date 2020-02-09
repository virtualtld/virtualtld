package com.virtualtld.client;

import org.junit.Test;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;

import java.net.IDN;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NsRequestTest {

    static {
        VirtualtldURLStreamHandlerFactory.register();
    }

    @Test
    public void correct_url() throws Exception {
        URL url = new URL("virtualtld://最新版本.com/a/b/Hello.txt");
        Message nsRequest = new NsRequest(url).nsRequest();
        System.out.println(nsRequest);
        assertThat(nsRequest.getQuestion().getName(), equalTo(
                new Name(IDN.toASCII("最新版本.com."))));
    }
}
