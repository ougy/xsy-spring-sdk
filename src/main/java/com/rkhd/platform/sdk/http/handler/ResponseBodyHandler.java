package com.rkhd.platform.sdk.http.handler;

import java.io.IOException;

public interface ResponseBodyHandler<T> {
    T handle(String paramString) throws IOException;
}