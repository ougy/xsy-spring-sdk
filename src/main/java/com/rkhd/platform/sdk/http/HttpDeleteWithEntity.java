package com.rkhd.platform.sdk.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class HttpDeleteWithEntity extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";

    public HttpDeleteWithEntity() {
    }

    public HttpDeleteWithEntity(URI uri) {
        setURI(uri);
    }

    public HttpDeleteWithEntity(String uri) {
        setURI(URI.create(uri));
    }

    public String getMethod() {
        return "DELETE";
    }
}