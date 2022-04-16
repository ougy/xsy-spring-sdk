package com.rkhd.platform.sdk.http;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HttpResult {
    private List<HttpHeader> headers = new ArrayList<>();
    private String result;

    public List<HttpHeader> getAllHeaders() {
        return this.headers;
    }

    public void setHeader(String name, String value) {
        HttpHeader httpHeader = new HttpHeader();
        httpHeader.setName(name);
        httpHeader.setValue(value);
        this.headers.add(httpHeader);
    }

    void setHeaders(List<HttpHeader> headers) {
        this.headers = headers;
    }

    public List<HttpHeader> getHeaders(String name) {
        List<HttpHeader> headerList = new ArrayList<>();
        if (StringUtils.isNotBlank(name)) {
            for (HttpHeader httpHeader : this.headers) {
                if (name.equals(httpHeader.getName())) {
                    headerList.add(httpHeader);
                }
            }
        }
        return headerList;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}