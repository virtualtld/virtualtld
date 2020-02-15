package com.protocol.cdc;

import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.IDN;
import java.util.Base64;

public class BlockSizeLimitTest {

    @Test
    public void test() throws TextParseException {
        Name privateDomain = Name.fromString(IDN.toASCII("最新版本.xyz."));
        Assert.assertEquals(431, new BlockSizeLimit(privateDomain).limit());
    }

    @Test
    public void test2() throws Exception {
        byte[] bytes = Base64.getDecoder().decode("AX9EY9a3iLVTP/44uzS2/4xdty0P8FhuOvRDEMFo/nildRO1E6aCL4KrLz4L1IIdwRCABLzmaia5XMrO8I+C7PMYgCL670oDRcxXovXnYK/OiByaznqHJ+H6XjGcBYegHf/Hk5qcEDIYVn5CfEtvGJ3YD7itmYBVNytkBAaTahIzIuxnMY75DV0tq2A694Knbtvo531tmRj+8+I9n+HAxq60zNE6vbmsH/mgSSxOegImoXk5v4StnfqFgCoquj5Dl8FpNB33Sn13XkqNNZnkJFRUfc1wGgk9SZAWmZ0AGhanKorxfieD9fK+lrQhkHeNhSCGEnEMOeRIdPYMhbTpxdd1jHdsujZrUkMjpXwDKphLwBunYQttKbiCS3m2tuNp9d2PBCoazXz4nJx2ykFdTKhFMJNRRfM1MD3mUvblxPvMf+2q9v8ByqqhsJq0qJPWFuJ2LbNHwg/0qR7wAzPNRvxdDX+rkhQJL3c3+FhKCxFu64RicBnWOB1q+BWEAi065Pb5i1B5dbHTFYXLtggkdn3iRf9T0uOFJdPTYe9dmpkylS6+Lg==");
        System.out.println(bytes.length);
        TXTRecord txtRecord = new EncodedTxtRecord(new Name("6ae060a93f5f57a5e124341152dbcc4d65fdf0dc.xn--efv12a2dz86b.xyz."), bytes).txtRecord();
        Name name = new Name("6ae060a93f5f57a5e124341152dbcc4d65fdf0dc.xn--efv12a2dz86b.xyz.");
        Message message = new Message();
        message.addRecord(Record.newRecord(name, Type.TXT, DClass.IN, 1024), Section.QUESTION);
        message.addRecord(txtRecord, Section.ANSWER);
        System.out.println(message.toWire().length);
        System.out.println(new BlockSizeLimit(new Name("xn--efv12a2dz86b.xyz.")).limit());
    }
}
