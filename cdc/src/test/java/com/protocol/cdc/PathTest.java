package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.util.ArrayList;

public class PathTest {
    @Test
    public void test_encode() throws TextParseException {
        EncodedPath path = new EncodedPath(Name.fromString("abc.com."), "", new EncodedHeadNode(
                new ArrayList<>(), new byte[8], null));
        Assert.assertEquals("r8eMbUUdjj2cPfqeMwBJG8amEPQ=",
                path.digest());
    }
}
