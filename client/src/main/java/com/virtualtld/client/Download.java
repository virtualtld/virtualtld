package com.virtualtld.client;

import java.net.URI;
import java.util.concurrent.Exchanger;

public class Download {
    public static void main(String[] args) {
        CdcClient cdcClient = new CdcClient();
        cdcClient.start();
        Exchanger<String> exchanger = new Exchanger<>();
        cdcClient.download(URI.create(args[0]), resp -> {
            try {
                exchanger.exchange(new String(resp));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            System.out.println(exchanger.exchange(null));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
