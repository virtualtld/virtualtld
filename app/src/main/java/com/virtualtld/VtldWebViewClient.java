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

import java.io.ByteArrayInputStream;

public class VtldWebViewClient extends WebViewClient {

    private final CdcClient cdcClient;

    public VtldWebViewClient() {
        cdcClient = new CdcClient();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Log.i("vtld", "load " + request.getUrl());
        return new WebResourceResponse("text/html", "utf8", new ByteArrayInputStream(("hello").getBytes()));
    }
}
