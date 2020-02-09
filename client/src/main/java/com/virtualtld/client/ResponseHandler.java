package com.virtualtld.client;

import org.xbill.DNS.Message;

public interface ResponseHandler {
    void onResponse(Message resp);
}
