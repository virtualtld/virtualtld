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
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return isInDirectoryBlacklist(dir)
                            ? FileVisitResult.SKIP_SUBTREE
                            : FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (shouldIncludeFile(file)) {
                        fileMap.put(webRoot.relativize(file).toString(), file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileMap;
    }

    private boolean isInDirectoryBlacklist(Path dir) {
        for (PathMatcher pathMatcher : options.directoryBlacklist) {
            if (pathMatcher.matches(dir)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldIncludeFile(Path file) {
        if (isInFileBlacklist(file)) {
            return false;
        }
        if (options.fileWhitelist.isEmpty()) {
            return true;
        }
        return isInFileWhitelist(file);
    }

    private boolean isInFileBlacklist(Path file) {
        for (PathMatcher pathMatcher : options.fileBlacklist) {
            if (pathMatcher.matches(file)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInFileWhitelist(Path file) {
        for (PathMatcher pathMatcher : options.fileWhitelist) {
            if (pathMatcher.matches(file)) {
                return true;
            }
        }
        return false;
    }

    public static class Options {
        public List<PathMatcher> fileBlacklist = new ArrayList<>();
        public List<PathMatcher> fileWhitelist = new ArrayList<>();
        public List<PathMatcher> directoryBlacklist = new ArrayList<>();
    }
}
