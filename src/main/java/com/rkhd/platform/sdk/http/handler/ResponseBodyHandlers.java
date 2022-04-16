package com.rkhd.platform.sdk.http.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class ResponseBodyHandlers {
    public static ResponseBodyHandler<String> ofString() {
        return response -> response;
    }

    public static ResponseBodyHandler<JSONObject> ofJSON() {
        return JSON::parseObject;
    }
}