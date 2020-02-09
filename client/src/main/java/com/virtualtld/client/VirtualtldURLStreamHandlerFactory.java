package com.virtualtld.client;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class VirtualtldURLStreamHandlerFactory implements URLStreamHandlerFactory {

    public static void register() {
        URL.setURLStreamHandlerFactory(new VirtualtldURLStreamHandlerFactory());
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("virtualtld".equals(protocol)) {
            return new VirtualtldURLStreamHandler();
        }
        return null;
    }
}
