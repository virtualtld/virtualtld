package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Base64;

import static org.hamcrest.CoreMatchers.equalTo;

public class CdcFileBodyChunkTest {

    @Test
    public void digest() {
        CdcFileBodyChunk chunk = new CdcFileBodyChunk("hello".getBytes());
        Assert.assertThat(Base64.getEncoder().encodeToString(chunk.digest()), equalTo("qvTGHdzF6KLavt4PO0gs2a6pQ00="));
    }
}
