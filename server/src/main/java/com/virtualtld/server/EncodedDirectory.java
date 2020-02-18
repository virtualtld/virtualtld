package com.virtualtld.server;

import com.protocol.cdc.Block;
import com.protocol.cdc.BlockSizeLimit;
import com.protocol.cdc.EncodedFile;
import com.protocol.cdc.VirtualtldSite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EncodedDirectory {

    private final static Logger LOGGER = LoggerFactory.getLogger("vtld.EncodedDirectory");
    private final Path webRoot;
    private final VirtualtldSite site;
    private final ScanOptions options;
    private final int chunkSizeLimit;

    public EncodedDirectory(VirtualtldSite site, Path webRoot, ScanOptions options) {
        this.webRoot = webRoot;
        this.site = site;
        this.options = options;
        chunkSizeLimit = new BlockSizeLimit(site.privateDomain).limit();
        LOGGER.info("block size limit: " + chunkSizeLimit);
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
            EncodedFile file = new EncodedFile(site.publicDomain, entry.getKey(), content, chunkSizeLimit);
            ArrayList<String> fileBlockDigests = new ArrayList<>();
            for (Block block : file.blocks()) {
                String digest = block.digest();
                fileBlockDigests.add(digest);
                if (block.data().length > chunkSizeLimit) {
                    throw new RuntimeException("unexpected block size " + block.data().length + ": " + block);
                }
                blocks.put(digest, block);
            }

            LOGGER.info("loaded file " + entry.getKey() + " with blocks " + fileBlockDigests);
        }
        return blocks;
    }
}
