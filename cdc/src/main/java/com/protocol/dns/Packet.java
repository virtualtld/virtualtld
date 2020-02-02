package com.protocol.dns;

/*

 * javaos emulation -cj
 */
public class Packet {
    byte buf[];

    Packet(int len) {
        buf = new byte[len];
    }

    Packet(byte data[], int len) {
        buf = new byte[len];
        System.arraycopy(data, 0, buf, 0, len);
    }

    void putInt(int x, int off) {
        buf[off + 0] = (byte)(x >> 24);
        buf[off + 1] = (byte)(x >> 16);
        buf[off + 2] = (byte)(x >> 8);
        buf[off + 3] = (byte)x;
    }

    void putShort(int x, int off) {
        buf[off + 0] = (byte)(x >> 8);
        buf[off + 1] = (byte)x;
    }

    void putByte(int x, int off) {
        buf[off] = (byte)x;
    }

    void putBytes(byte src[], int src_offset, int dst_offset, int len) {
        System.arraycopy(src, src_offset, buf, dst_offset, len);
    }

    public int length() {
        return buf.length;
    }

    public byte[] getData() {
        return buf;
    }
}
