package com.virtualtld.client;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Exchanger;

public class Download {
    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.asList(args));
        CdcClient cdcClient = new CdcClient();
        cdcClient.start();
        Exchanger<byte[]> exchanger = new Exchanger<>();
        cdcClient.download(URI.create(args[0]), resp -> {
            try {
                exchanger.exchange(resp);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        if (args.length > 1) {
            Files.write(Paths.get(args[1]), exchanger.exchange(null));
        } else {
            System.out.println(new String(exchanger.exchange(null)));
        }
    }
}
