package com.virtualtld.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class DnsServer {

    private final InetSocketAddress addr;
    private final Consumer<DnsRequest> handler;
    private DatagramSocket sock;

    public DnsServer(InetSocketAddress addr, Consumer<DnsRequest> handler) {
        this.addr = addr;
        this.handler = handler;
    }

    public void start() {
        try {
            this._start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void _start() throws Exception {
        sock = new DatagramSocket(this.addr);
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            sock.receive(packet);
            handler.accept(new DnsRequest(sock, packet));
        }
    }

    public void stop() {
        sock.close();
    }
}
