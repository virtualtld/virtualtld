package com.virtualtld.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VirtualTldConf {
    public InetSocketAddress listenAt = new InetSocketAddress("127.0.0.1", 53);
    public int majorVersion = 1;
    public int minorVersion = 1;
    public List<InetSocketAddress> privateResolvers = new ArrayList<>();
    public String publicDomain;
    public String privateDomain;
    public List<PathMatcher> fileBlacklist = new ArrayList<>();
    public List<PathMatcher> fileWhitelist = new ArrayList<>();
    public List<PathMatcher> directoryBlacklist = new ArrayList<>();


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
                publicDomain = value;
                return;
            case "PrivateDomain":
                privateDomain = value;
                return;
            case "ListenAt":
                listenAt = parseIpAndPort(value);
                return;
            case "PrivateResolver":
                addPrivateResolver(value);
                return;
            case "FileBlacklist":
                fileBlacklist.add(FileSystems.getDefault().getPathMatcher(
                        "glob:" + value));
                return;
            case "FileWhitelist":
                fileWhitelist.add(FileSystems.getDefault().getPathMatcher(
                        "glob:" + value));
                return;
            case "DirectoryBlacklist":
                directoryBlacklist.add(FileSystems.getDefault().getPathMatcher(
                        "glob:" + value));
                return;
        }
        throw new RuntimeException("unknown config key: " + key);
    }

    private void addPrivateResolver(String value) {
        this.privateResolvers.add(parseIpAndPort(value));
    }

    private void setVersion(String value) {
        String[] majorAndMinor = value.split("\\.", 2);
        this.majorVersion = Integer.parseInt(majorAndMinor[0]);
        this.minorVersion = Integer.parseInt(majorAndMinor[1]);
    }

    private static InetSocketAddress parseIpAndPort(String value) {
        String[] ipAndPort = value.split(":", 2);
        int port = ipAndPort[1].length() > 0 ? Integer.parseInt(ipAndPort[1]) : 53;
        return new InetSocketAddress(ipAndPort[0], port);
    }
}
