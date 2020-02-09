package com.virtualtld.client;

import org.junit.Test;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;

import java.net.IDN;
import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NsRequestTest {

    @Test
    public void correct_uri() throws Exception {
        URI uri = new URI("virtualtld://最新版本.com/a/b/Hello.txt");
        Message nsRequest = new NsRequest(uri).nsRequest();
        assertThat(nsRequest.getQuestion().getName(), equalTo(
                new Name(IDN.toASCII("u5U4Ywx+E1CWvdiyz0xHNjsHu8o=.最新版本.com."))));
    }
}
