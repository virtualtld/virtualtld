package com.virtualtld.server;

import com.protocol.cdc.Block;
import com.protocol.cdc.VirtualtldSite;
import com.protocol.cdc.EncodedFile;
import com.protocol.cdc.EncodedHeadNode;
import com.protocol.cdc.EncodedPath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncodedDirectory {

    private final Path webRoot;
    private final VirtualtldSite site;
    private final WebFiles.Options options;

    public EncodedDirectory(VirtualtldSite site, Path webRoot, WebFiles.Options options) {
        this.webRoot = webRoot;
        this.site = site;
        this.options = options;
    }

    public Map<String, Path> files() {
        return new WebFiles(webRoot, options).files();
    }

    public Map<String, Block> blocks() {
        try {
            return _blocks();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Block> _blocks() throws Exception {
        Map<String, Block> blocks = new HashMap<>();
        for (Map.Entry<String, Path> entry : files().entrySet()) {
            EncodedFile file = new EncodedFile(site, Files.readAllBytes(entry.getValue()));
            List<EncodedHeadNode> head = file.head();
            for (Block block : head) {
                blocks.put(block.digest(), block);
            }
            for (Block block : file.body()) {
                blocks.put(block.digest(), block);
            }
            EncodedPath path = new EncodedPath(site.publicDomain, entry.getKey(), head.get(0));
            blocks.put(path.digest(), path);
        }
        return blocks;
    }
}
