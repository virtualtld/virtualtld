package com.virtualtld.server;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebFiles {

    private final Path webRoot;
    private Options options;

    public WebFiles(Path webRoot, Options options) {
        this.webRoot = webRoot;
        this.options = options;
    }

    public Map<String, Path> files() {
        HashMap<String, Path> fileMap = new HashMap<>();
        try {
            Files.walkFileTree(webRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    fileMap.put(webRoot.relativize(file).toString(), file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileMap;
    }

    public static class Options {
        public List<PathMatcher> fileBlacklist = new ArrayList<>();
        public List<PathMatcher> fileWhitelist = new ArrayList<>();
        public List<PathMatcher> directoryBlacklist = new ArrayList<>();
        public List<PathMatcher> directoryWhitelist = new ArrayList<>();
    }
}
