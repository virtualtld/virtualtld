package com.protocol.cdc;

import java.net.IDN;

public class CdcSite {
    public final String publicDomain;
    public final String privateDomain;

    public CdcSite(String publicDomain, String privateDomain) {
        this.publicDomain = IDN.toASCII(publicDomain);
        this.privateDomain = IDN.toASCII(privateDomain);
    }
}
