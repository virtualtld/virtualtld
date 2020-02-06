package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;

import static com.protocol.cdc.Digest.base64;
import static org.hamcrest.CoreMatchers.equalTo;

public class CdcFileBodyChunkTest {

    @Test
    public void digest() {
        CdcFileBodyChunk chunk = new CdcFileBodyChunk("hello".getBytes());
        Assert.assertThat(base64(chunk.digest()), equalTo("qvTGHdzF6KLavt4PO0gs2a6pQ00="));
    }
}
