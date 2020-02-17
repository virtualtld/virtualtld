package com.protocol.cdc;

public interface Block {
    String digest();
    byte[] data();
    int ttl();
}
