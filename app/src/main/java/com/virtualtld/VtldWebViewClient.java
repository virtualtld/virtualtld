package com.virtualtld;

import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.virtualtld.client.CdcClient;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;

public class VtldWebViewClient extends WebViewClient {

    private final CdcClient cdcClient;

    public VtldWebViewClient() {
        cdcClient = new CdcClient();
//        cdcClient.rootNameServers = Collections.singletonList(
//                new InetSocketAddress("192.168.40.241", 8383));
        cdcClient.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Log.i("vtld", "load " + request.getUrl());
        PipedInputStream pipedInputStream = new PipedInputStream();
        try {
            String uri = request.getUrl().toString();
            cdcClient.download(URI.create(uri), new PipedOutputStream(pipedInputStream));
            return new WebResourceResponse("text/html", "utf8", pipedInputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
