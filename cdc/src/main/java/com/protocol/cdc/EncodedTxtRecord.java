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
    private final Block block;

    public EncodedTxtRecord(Name name, Block block) {
        this.name = name;
        this.block = block;
    }

    public TXTRecord txtRecord() {
        TXTRecord record = (TXTRecord) Record.newRecord(
                name, Type.TXT, DClass.IN, block.ttl(), new byte[]{0});
        List<byte[]> strings = record.getStringsAsByteArrays();
        strings.clear();
        byte[] blockData = block.data();
        if (blockData.length < 256) {
            strings.add(blockData);
        } else {
            strings.add(copyOfRange(blockData, 0, 255));
            strings.add(copyOfRange(blockData, 255, blockData.length));
        }
        return record;
    }
}
