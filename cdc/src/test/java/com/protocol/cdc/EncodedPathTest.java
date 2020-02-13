package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.util.ArrayList;

public class EncodedPathTest {
    @Test
    public void test_encode() throws TextParseException {
        EncodedPath path = new EncodedPath(Name.fromString("abc.com."), "/");
        Assert.assertEquals("8bea061f3f71b7d4d3b39257114b713b93dd3b87",
                path.digest());
    }
}
