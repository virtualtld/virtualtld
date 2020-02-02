package com.virtualtld.server;

import java.nio.file.Files;
import java.nio.file.Paths;

public class PublishLocal {
    public static void main(String[] args) {
        String webrootDir = args[0];
        System.out.println("publish local, webroot: " + webrootDir);
        System.out.println(Files.exists(Paths.get(webrootDir, "index.html")));
    }

}
