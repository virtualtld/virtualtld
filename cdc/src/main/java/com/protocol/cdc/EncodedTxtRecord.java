package com.protocol.cdc;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import java.util.List;

import static java.util.Arrays.copyOfRange;

public class EncodedTxtRecord {

    private final Name name;
    private final byte[] block;

    public EncodedTxtRecord(Name name, byte[] block) {
        this.name = name;
        this.block = block;
    }

    public TXTRecord txtRecord() {
        TXTRecord record = (TXTRecord) Record.newRecord(
                name, Type.TXT, DClass.IN, 172800, new byte[]{0});
        List<byte[]> strings = record.getStringsAsByteArrays();
        strings.clear();
        if (block.length < 256) {
            strings.add(block);
        } else {
            strings.add(copyOfRange(block, 0, 255));
            strings.add(copyOfRange(block, 255, block.length));
        }
        return record;
    }
}
