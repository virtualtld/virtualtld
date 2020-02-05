package com.virtualtld.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VirtualTldConf {
    public int majorVersion = 1;
    public int minorVersion = 1;
    public List<InetSocketAddress> privateResolvers = new ArrayList<>();
    public String publicDomain;
    public String privateDomain;

    public static VirtualTldConf parse(String webrootDir) {
        Path path = Paths.get(webrootDir, "virtualtld.conf");
        if (!Files.exists(path)) {
            throw new RuntimeException(path + " did not found");
        }
        try {

            List<String> lines = Files.readAllLines(path);
            return parse(lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static VirtualTldConf parse(List<String> lines) {
        VirtualTldConf conf = new VirtualTldConf();
        for (String line : lines) {
            String[] kv = line.split("=", 2);
            if (kv.length == 2) {
                conf.set(kv[0], kv[1]);
            }
        }
        return conf;
    }

    void set(String key, String value) {
        switch (key) {
            case "Version":
                setVersion(value);
                return;
            case "PublicDomain":
                this.publicDomain = value;
                return;
            case "PrivateDomain":
                this.privateDomain = value;
                return;
            case "PrivateResolver":
                addPrivateResolver(value);
                return;
        }
        throw new RuntimeException("unknown config key: " + key);
    }

    private void addPrivateResolver(String value) {
        String[] ipAndPort = value.split(":", 2);

        try {
            int port = ipAndPort[1].length() > 0 ? Integer.parseInt(ipAndPort[1]) : 53;
            this.privateResolvers.add(new InetSocketAddress(InetAddress.getByName(ipAndPort[0]), port));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void setVersion(String value) {
        String[] majorAndMinor = value.split("\\.", 2);
        this.majorVersion = Integer.parseInt(majorAndMinor[0]);
        this.minorVersion = Integer.parseInt(majorAndMinor[1]);
    }
}
