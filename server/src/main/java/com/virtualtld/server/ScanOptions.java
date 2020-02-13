package com.virtualtld.server;

import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

public class ScanOptions {
    public List<PathMatcher> fileBlacklist = new ArrayList<>();
    public List<PathMatcher> fileWhitelist = new ArrayList<>();
    public List<PathMatcher> directoryBlacklist = new ArrayList<>();
}
