package com.virtualtld.client;

import org.junit.Test;

import java.net.URI;

public class CdcClientTest {

    @Test
    public void spike() throws Exception {
        CdcClient cdcClient = new CdcClient();
        cdcClient.start();
        cdcClient.download(new URI("virtualtld://最新消息.com/"), resp -> {});
        Thread.sleep(2000);
        cdcClient.stop();
    }
}
