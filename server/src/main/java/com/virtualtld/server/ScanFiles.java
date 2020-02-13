package com.virtualtld.server;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class ScanFiles {

    private final Path webRoot;
    private ScanOptions options;

    public ScanFiles(Path webRoot, ScanOptions options) {
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
                        String path = "/" + webRoot.relativize(file).toString();
                        if (path.equals("/index.html")) {
                            fileMap.put("/", file);
                        }
                        fileMap.put(path, file);
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

}
