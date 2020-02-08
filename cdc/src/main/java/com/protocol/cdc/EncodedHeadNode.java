package com.protocol.cdc;

import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import java.util.List;

import static com.protocol.cdc.Digest.base64;
import static com.protocol.cdc.EncodedBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.Password.SALT_SIZE;

class EncodedHeadNode {
    public final static byte FLAG_NEXT = (byte) 1;
    public final static byte FLAG_SALT = (byte) 2;

    private final Name privateDomain;
    private final List<EncodedBodyChunk> chunks;
    private EncodedHeadNode next;
    private final byte[] salt;

    public EncodedHeadNode(Name privateDomain, List<EncodedBodyChunk> chunks, byte[] salt) {
        this(privateDomain, chunks, salt, null);
    }

    public EncodedHeadNode(Name privateDomain, List<EncodedBodyChunk> chunks, byte[] salt, EncodedHeadNode next) {
        this.privateDomain = privateDomain;
        this.chunks = chunks;
        this.next = next;
        this.salt = salt;
    }

    public void setNext(EncodedHeadNode next) {
        this.next = next;
    }

    private byte nodeFlag() {
        byte flag = 0;
        if (salt != null) {
            flag |= FLAG_SALT;
        }
        if (next != null) {
            flag |= FLAG_NEXT;
        }
        return flag;
    }

    public byte[] data() {
        int size = 1 + chunks.size() * DIGEST_SIZE;
        if (next != null) {
            size += DIGEST_SIZE;
        }
        if (salt != null) {
            size += SALT_SIZE;
        }
        byte[] data = new byte[size];
        int pos = 0;
        data[pos++] = nodeFlag();
        if (next != null) {
            byte[] digest = next.digest();
            System.arraycopy(digest, 0, data, pos, DIGEST_SIZE);
            pos += DIGEST_SIZE;
        }
        if (salt != null) {
            System.arraycopy(salt, 0, data, pos, SALT_SIZE);
            pos += SALT_SIZE;
        }
        for (int i = 0; i < chunks.size(); i++) {
            EncodedBodyChunk chunk = chunks.get(i);
            byte[] digest = chunk.digest();
            System.arraycopy(digest, 0, data, pos, DIGEST_SIZE);
            pos += DIGEST_SIZE;
        }
        return data;
    }

    public byte[] digest() {
        return Digest.sha1Bytes(data());
    }

    public Name dnsName() {
        try {
            return new Name(base64(digest()), privateDomain);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }
}
