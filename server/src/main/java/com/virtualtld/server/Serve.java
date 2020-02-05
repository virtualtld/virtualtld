package com.virtualtld.server;

import org.xbill.DNS.Message;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Serve {
    public static void main(String[] args) {
        String webrootDir = args[0];
        System.out.println("serve webroot: " + webrootDir);
        System.out.println(Files.exists(Paths.get(webrootDir, "index.html")));
        VirtualTldConf conf = VirtualTldConf.parse(webrootDir);
        System.out.println(conf.publicDomain);
        Message nsResp = new NsResponse(conf).nsResponse();
        HandleDnsRequest handler = new HandleDnsRequest(
                input -> {
                    return new DnsResponse(input, nsResp, null).dnsResponse();
                });
        new DnsServer(conf.listenAt, handler).start();
    }
}
