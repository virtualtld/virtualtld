package com.virtualtld.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DnsRequest {

    public final DatagramSocket socket;
    public final DatagramPacket packet;

    public DnsRequest(DatagramSocket socket, DatagramPacket packet) {
        this.socket = socket;
        this.packet = packet;
    }
}
