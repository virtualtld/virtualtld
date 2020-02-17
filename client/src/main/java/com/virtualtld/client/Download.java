package com.virtualtld.client;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Download {
    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.asList(args));
        CdcClient cdcClient = new CdcClient();
        cdcClient.start();
        PipedInputStream inputStream = new PipedInputStream();
        cdcClient.download(URI.create(args[0]), new PipedOutputStream(inputStream));
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        if (args.length > 1) {
            Files.write(Paths.get(args[1]), result.toByteArray());
        } else {
            System.out.println(new String(result.toByteArray()));
        }
    }
}
