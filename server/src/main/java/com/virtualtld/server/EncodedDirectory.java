package com.virtualtld.server;

import com.protocol.cdc.Block;
import com.protocol.cdc.EncodedFile;
import com.protocol.cdc.VirtualtldSite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EncodedDirectory {

    private final Path webRoot;
    private final VirtualtldSite site;
    private final ScanOptions options;

    public EncodedDirectory(VirtualtldSite site, Path webRoot, ScanOptions options) {
        this.webRoot = webRoot;
        this.site = site;
        this.options = options;
    }

    public Map<String, Path> files() {
        return new ScanFiles(webRoot, options).files();
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
        Map<String, Path> files = files();
        for (Map.Entry<String, Path> entry : files.entrySet()) {
            byte[] content = Files.readAllBytes(entry.getValue());
            EncodedFile file = new EncodedFile(site, entry.getKey(), content);
            for (Block block : file.blocks()) {
                blocks.put(block.digest(), block);
            }
        }
        return blocks;
    }
}
