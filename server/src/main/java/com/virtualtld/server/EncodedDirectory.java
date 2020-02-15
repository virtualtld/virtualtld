package com.virtualtld.server;

import com.protocol.cdc.Block;
import com.protocol.cdc.EncodedFile;
import com.protocol.cdc.VirtualtldSite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EncodedDirectory {

    private final static Logger LOGGER = LoggerFactory.getLogger(EncodedDirectory.class);
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
            ArrayList<String> fileBlockDigests = new ArrayList<>();
            for (Block block : file.blocks()) {
                if ("6ae060a93f5f57a5e124341152dbcc4d65fdf0dc".equals(block.digest())) {
                    System.out.println(Base64.getEncoder().encodeToString(block.data()));
                }
                blocks.put(block.digest(), block);
            }
            LOGGER.info("loaded file " + entry.getKey() + " with blocks " + fileBlockDigests);
        }
        return blocks;
    }
}
