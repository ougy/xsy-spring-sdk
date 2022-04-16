package com.rkhd.platform.sdk.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonData {
    private String call_type;
    private String body = "";
    private String callString;
    private Map<String, String> headerMap = new HashMap<>();
    private List<HttpHeader> headerList = new ArrayList<>();
    private Map<String, Object> formData = new HashMap<>();
    private String formType;

    public CommonData() {
        this.formType = "urlEncoded";
    }

    public CommonData(String call_type, String body, String callString) {
        this.call_type = call_type;
        this.body = body;
        this.callString = callString;
        this.formType = "urlEncoded";
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getCall_type() {
        return this.call_type;
    }

    public void setCall_type(String call_type) {
        this.call_type = call_type;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCallString() {
        return this.callString;
    }

    public void setCallString(String callString) {
        this.callString = callString;
    }

    public Map<String, String> getHeaders() {
        return this.headerMap;
    }

    public void putHeader(String key, String value) {
        HttpHeader httpHeader = new HttpHeader();
        httpHeader.setName(key);
        httpHeader.setValue(value);
        this.headerList.add(httpHeader);
        this.headerMap.put(key, value);
    }

    public Map<String, Object> getFormData() {
        return this.formData;
    }

    public void putFormData(String key, Object object) {
        this.formData.put(key, object);
    }

    public void putFormDataAll(Map<String, Object> formData) {
        this.formData.putAll(formData);
    }

    public String getFormType() {
        return this.formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public void addHeader(String name, String value) {
        HttpHeader httpHeader = new HttpHeader();
        httpHeader.setName(name);
        httpHeader.setValue(value);
        this.headerList.add(httpHeader);
        this.headerMap.put(name, value);
    }

    public void addHeader(HttpHeader httpHeader) {
        if (httpHeader != null) {
            this.headerList.add(httpHeader);
            this.headerMap.put(httpHeader.getName(), httpHeader.getValue());
        }
    }

    public List<HttpHeader> getHeaderList() {
        return this.headerList;
    }


    public String toString() {
        return "call_type:" + this.call_type + ", callString:" + this.callString
                + ", body:" + this.body + ", formData:" + this.formData.toString();
    }

    public static class Builder {
        private String call_type;
        private String body = "";
        private String callString;
        private Map<String, String> headerMap = new HashMap<>();
        private List<HttpHeader> headerList = new ArrayList<>();
        private Map<String, Object> formData = new HashMap<>();
        private String formType;

        public Builder callType(String callType) {
            this.call_type = callType;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder callString(String callString) {
            this.callString = callString;
            return this;
        }

        public Builder header(String name, String value) {
            HttpHeader httpHeader = new HttpHeader();
            httpHeader.setName(name);
            httpHeader.setValue(value);
            this.headerList.add(httpHeader);
            this.headerMap.put(name, value);
            return this;
        }

        public Builder header(Map<String, String> headerMap) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                header(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder formData(String name, Object value) {
            this.formData.put(name, value);
            return this;
        }

        public Builder formData(Map<String, Object> formData) {
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                formData(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder formType(String formType) {
            this.formType = formType;
            return this;
        }

        public CommonData build() {
            return new CommonData(this.call_type, this.body, this.callString, this.headerMap, this.headerList, this.formData, this.formType);
        }
    }

    private CommonData(String call_type, String body, String callString, Map<String, String> headerMap, List<HttpHeader> headerList, Map<String, Object> formData, String formType) {
        this.call_type = call_type;
        this.body = body;
        this.callString = callString;
        this.headerMap = headerMap;
        this.headerList = headerList;
        this.formData = formData;
        this.formType = (formType == null) ? "urlEncoded" : formType;
    }

    public static interface formDataType {
        public static final String xWwwFormUrlencoded = "urlEncoded";
        public static final String formData = "formData";
    }
}