package com.virtualtld.server;

import com.protocol.cdc.Block;
import com.protocol.cdc.CdcSite;
import com.protocol.cdc.EncodedFile;
import com.protocol.cdc.EncodedHeadNode;
import com.protocol.cdc.EncodedPath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EncodedDirectory {

    private final Path webRoot;
    private final CdcSite site;
    private final WebFiles.Options options;

    public EncodedDirectory(CdcSite site, Path webRoot, WebFiles.Options options) {
        this.webRoot = webRoot;
        this.site = site;
        this.options = options;
    }

    public Map<String, Path> files() {
        return new WebFiles(webRoot, options).files();
    }

    public List<Block> blocks() {
        try {
            return _blocks();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Block> _blocks() throws Exception {
        ArrayList<Block> blocks = new ArrayList<>();
        for (Map.Entry<String, Path> entry : files().entrySet()) {
            EncodedFile encodedFile = new EncodedFile(site, Files.readAllBytes(entry.getValue()));
            List<EncodedHeadNode> head = encodedFile.head();
            blocks.addAll(head);
            blocks.addAll(encodedFile.body());
            blocks.add(new EncodedPath(site.publicDomain, entry.getKey(), head.get(0)));
        }
        return blocks;
    }
}
