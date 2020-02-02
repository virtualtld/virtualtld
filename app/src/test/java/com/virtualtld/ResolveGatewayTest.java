package com.virtualtld;

import org.junit.Test;

import java.net.IDN;

public class ResolveGatewayTest {
    @Test
    public void try_resolve() {
        System.out.println(IDN.toASCII("最新版本.com"));
        Gateway gateway = new ResolveGateway().apply("hello.virtualtld");
        System.out.println(gateway);
    }
}