package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

public class CdcFileHeaderNode {
    public List<CdcFileBodyChunk> chunks = new ArrayList<>();
    public CdcFileHeaderNode next;
}
