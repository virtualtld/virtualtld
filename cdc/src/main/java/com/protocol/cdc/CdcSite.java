package com.protocol.cdc;


import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.net.IDN;

public class CdcSite {
    public final Name publicDomain;
    public final Name privateDomain;

    public CdcSite(String publicDomain, String privateDomain) {
        try {
            this.publicDomain = Name.fromString(IDN.toASCII(publicDomain + "."));
            this.privateDomain = Name.fromString(IDN.toASCII(privateDomain + "."));
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }
}
