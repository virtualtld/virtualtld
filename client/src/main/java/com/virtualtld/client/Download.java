package com.virtualtld.client;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Download {
    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.asList(args));
        CdcClient cdcClient = new CdcClient();
        cdcClient.start();
        byte[] result = cdcClient.download(URI.create(args[0]));
        if (args.length > 1) {
            Files.write(Paths.get(args[1]), result);
        } else {
            System.out.println(new String(result));
        }
    }
}
