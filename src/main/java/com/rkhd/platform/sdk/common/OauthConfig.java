package com.rkhd.platform.sdk.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oauthconfig")
public class OauthConfig {
    private String domain;
    private String userName;
    private String password;
    private String securityCode;
    private String clientId;
    private String clientSecret;
    private Integer socketTimeout;
    private Integer connectionTimeout;
    private Integer readTimedOutRetry;
}