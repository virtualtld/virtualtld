package com.virtualtld.server;

import com.protocol.cdc.Block;
import com.protocol.cdc.VirtualtldSite;

import org.xbill.DNS.Message;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Serve {
    public static void main(String[] args) {
        Path webRoot = Paths.get(args[0]);
        System.out.println("serve webroot: " + webRoot);
        VirtualTldConf conf = VirtualTldConf.parse(webRoot);
        Message nsResp = new NsResponse(conf).nsResponse();
        VirtualtldSite site = new VirtualtldSite(conf.publicDomain, conf.privateDomain);
        Map<String, Block> blocks = new EncodedDirectory(site, webRoot, conf.webFilesOptions()).blocks();
        System.out.println("loaded blocks: " + blocks.keySet());
        CdcRequestHandler cdcRequestHandler = new CdcRequestHandler(nsResp, blocks);
        ConcurrentHandler concurrentHandler = new ConcurrentHandler(cdcRequestHandler, 8);
        System.out.println("list at " + conf.listenAt);
        new DnsServer(conf.listenAt, concurrentHandler).start();
    }
}
