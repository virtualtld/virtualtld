package com.protocol.cdc;

import org.xbill.DNS.TXTRecord;

import java.util.List;

public class DecodedTxtRecord {
    private final TXTRecord record;

    public DecodedTxtRecord(TXTRecord record) {
        this.record = record;
    }

    public String digest() {
        return record.getName().getLabelString(0);
    }

    public byte[] data() {
        List<byte[]> list = record.getStringsAsByteArrays();
        if (list.size() == 2) {
            byte[] merged = new byte[list.get(0).length + list.get(1).length];
            System.arraycopy(list.get(0), 0, merged, 0, list.get(0).length);
            System.arraycopy(list.get(1), 0, merged, list.get(0).length, list.get(1).length);
            return merged;
        }
        return list.get(0);
    }
}
