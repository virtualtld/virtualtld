package com.virtualtld.server;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class VirtualTldConfTest {
    @Test
    public void test_correct_conf() {
        VirtualTldConf conf = VirtualTldConf.parse(Arrays.asList(
                "Version=1.1",
                "PublicDomain=最新版本.com",
                "PrivateDomain=最新版本.xyz",
                "PrivateResolver=1.1.1.1:53",
                "FileBlacklist=**/*.html",
                "FileWhitelist=**/*.txt",
                "DirectoryBlacklist=**/.git"));

        assertThat(conf.majorVersion, equalTo(1));
        assertThat(conf.minorVersion, equalTo(1));
        assertThat(conf.publicDomain, equalTo("最新版本.com"));
        assertThat(conf.privateDomain, equalTo("最新版本.xyz"));
        assertThat(conf.privateResolvers, equalTo(Collections.singletonList(
                new InetSocketAddress("1.1.1.1", 53))));
        assertThat(conf.fileBlacklist, hasSize(1));
        assertThat(conf.fileWhitelist, hasSize(1));
        assertThat(conf.directoryBlacklist, hasSize(1));
    }
}
