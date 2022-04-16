package com.rkhd.platform.sdk.common;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

@Slf4j
public class OauthConfig {
    private static final String OAUTH_CONFIG_PROPERTIES = "oauthConfig.properties";
    private static final String USERNAME = "userName";
    private static final String PASSWORD = "password";
    private static final String SECURITY_CODE = "securityCode";
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    public static final String DOMAIN = "domain";
    public static final String SOCKET_TIMEOUT = "socketTimeout";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String READ_TIMED_OUT_RETRY = "readTimedOutRetry";
    private static Properties properties = new Properties();

    static {
        try {
            InputStream inputStream = getDefaultClassLoader().getResourceAsStream("oauthConfig.properties");
            try {
                if (inputStream != null) {
                    properties.load(inputStream);
                } else {
                    log.error("please config oauthConfig.properties in the resources directory");
                    log.error("Example:\n");
                    log.error("userName=xxx");
                    log.error("password=xxx");
                    log.error("securityCode=xxx");
                    log.error("clientId=xxx");
                    log.error("clientSecret=xxx");
                    log.error("domain=xxx");
                    log.error("modelJarPath=xxx");
                    log.error("socketTimeout=xxx");
                    log.error("connectionTimeout=xxx");
                    log.error("readTimedOutRetry=xxx");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            loadJar(properties.getProperty("modelJarPath", "model.jar"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public OauthConfig() {
        InputStream inputStream = getDefaultClassLoader().getResourceAsStream("oauthConfig.properties");

        try {
            if (inputStream != null) {
                this.properties.load(inputStream);
            } else {
                log.error("please config oauthConfig.properties in the resources directory");
                log.error("Example:\n");
                log.error("userName=xxx");
                log.error("password=xxx");
                log.error("securityCode=xxx");
                log.error("clientId=xxx");
                log.error("clientSecret=xxx");
                log.error("domain=xxx");
                log.error("modelJarPath=xxx");
                log.error("socketTimeout=xxx");
                log.error("connectionTimeout=xxx");
                log.error("readTimedOutRetry=xxx");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable throwable) {
        }

        if (cl == null) {
            cl = OauthConfig.class.getClassLoader();
        }
        return cl;
    }

    public static String getUserName() {
        return properties.getProperty("userName", "");
    }

    public static String getPassword() {
        return properties.getProperty("password", "");
    }

    public static String getSecurityCode() {
        return properties.getProperty("securityCode", "");
    }

    public static String getClientId() {
        return properties.getProperty("clientId", "");
    }

    public static String getClientSecret() {
        return properties.getProperty("clientSecret", "");
    }

    public static String getOauthUrl() {
        return getDomain() + "/oauth2/token.action";
    }

    public static String getDomain() {
        return properties.getProperty("domain", "https://api-tencent.xiaoshouyi.com");
    }

    public static String getSocketTimeout() {
        return properties.getProperty("socketTimeout", "120000");
    }

    public static String getConnectionTimeout() {
        return properties.getProperty("connectionTimeout", "120000");
    }

    public static Integer getReadTimedOutRetry() {
        return Integer.parseInt(properties.getProperty("readTimedOutRetry", "3"));
    }

    public static void loadJar(String jarPath) throws MalformedURLException {
        File jarFile = new File(jarPath);

        if (jarFile.exists() == false) {
            System.out.println("model jar file not found.");
            return;
        }

        Method method = null;
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        }

        boolean accessible = method.isAccessible();
        try {
            if (accessible == false) {
                method.setAccessible(true);
            }

            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

            URL url = jarFile.toURI().toURL();

            method.invoke(classLoader, url);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            method.setAccessible(accessible);
        }
    }
}