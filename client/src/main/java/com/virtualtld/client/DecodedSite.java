package com.virtualtld.client;

import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TextParseException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DecodedSite {

    private final static List<InetSocketAddress> DEFAULT_RESOLVERS = Arrays.asList(
            new InetSocketAddress("1.1.1.1", 53),
            new InetSocketAddress("208.67.222.222", 53),
            new InetSocketAddress("208.67.220.220", 53),
            new InetSocketAddress("1.0.0.1", 53)
    );
    private boolean versionVerified;
    private Name privateDomain;
    private List<InetSocketAddress> privateResolvers = new ArrayList<>();

    public DecodedSite(Message resp) {
        try {
            parse(resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void parse(Message resp) throws Exception {
        for (Record record : resp.getSectionArray(Section.ANSWER)) {
            if (!(record instanceof NSRecord)) {
                throw new RuntimeException("unexpected record type");
            }
            parseRecord((NSRecord) record);
        }
        if (!versionVerified) {
            throw new RuntimeException("missing version");
        }
        if (privateDomain == null) {
            throw new RuntimeException("missing private domain");
        }
        if (privateResolvers.isEmpty()) {
            privateResolvers.addAll(DEFAULT_RESOLVERS);
        }
    }

    private void parseRecord(NSRecord record) throws TextParseException {
        Name target = record.getTarget();
        int labelsCount = target.labels();
        switch (labelsCount) {
            case 5:
                privateDomain = Name.fromString(target.getLabelString(0) + "."
                        + target.getLabelString(1) + ".");
                break;
            case 6:
                versionVerified = true;
                if (!target.getLabelString(0).equals("ver")) {
                    throw new RuntimeException("unexpected version");
                }
                if (!target.getLabelString(1).equals("1")) {
                    throw new RuntimeException("unexpected version");
                }
                if (!target.getLabelString(2).equals("1")) {
                    throw new RuntimeException("unexpected version");
                }
                break;
            case 7:
                privateResolvers.add(new InetSocketAddress(target.getLabelString(0) + "."
                        + target.getLabelString(1) + "."
                        + target.getLabelString(2) + "."
                        + target.getLabelString(3), 53));
                break;
            case 8:
                privateResolvers.add(new InetSocketAddress(
                        target.getLabelString(0) + "."
                                + target.getLabelString(1) + "."
                                + target.getLabelString(2) + "."
                                + target.getLabelString(3),
                        Integer.parseInt(target.getLabelString(4))));
                break;
            default:
                throw new RuntimeException("unexpected record content");
        }
    }

    public Name privateDomain() {
        return privateDomain;
    }

    public List<InetSocketAddress> privateResolvers() {
        return privateResolvers;
    }
}
