package com.rkhd.platform.sdk.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.rkhd.platform.sdk.http.CommonData;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.http.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 使用Guava缓存管理token
 *
 * @author 欧桂源
 * @date 2022/5/01 12:50
 */
@Slf4j
@Component
public class TokenCache {
    private static OauthConfig oauthConfig;

    //声明一个静态的内存块,guava里面的本地缓存
    private static LoadingCache<String, String> localcache;

    @Autowired
    public TokenCache(OauthConfig oauthConfig) {
        this.oauthConfig = oauthConfig;
    }

    static {
        oauthConfig = new OauthConfig();
        localcache =
                //构建本地缓存，调用链的方式,1是设置缓存的初始化容量，maximumSize是设置缓存最大容量，当超过了最大容量，guava将使用LRU算法（最少使用算法），来移除缓存项
                //expireAfterAccess(1,TimeUnit.HOURS)设置缓存有效期为1个小时
                CacheBuilder.newBuilder().initialCapacity(1).maximumSize(10).expireAfterAccess(1, TimeUnit.HOURS)
                        //build里面要实现一个匿名抽象类
                        .build(new CacheLoader<String, String>() {
                            //这个方法是默认的数据加载实现,get的时候，如果key没有对应的值，就调用这个方法进行加载
                            @Override
                            public String load(String s) throws Exception {
                                //为什么要把return的null值写成字符串，因为到时候用null去.equal的时候，会报空指针异常
                                String accessToken = "null";
                                try {
                                    CommonHttpClient commonHttpClient = CommonHttpClient.instance();
                                    commonHttpClient.setContentEncoding("UTF-8");
                                    commonHttpClient.setContentType("application/json");

                                    String oauthUrl = oauthConfig.getDomain() + "/oauth2/token?grant_type=password" + "&client_id=" + oauthConfig.getClientId()
                                            + "&client_secret=" + oauthConfig.getClientSecret() + "&username=" + oauthConfig.getUserName() + "&password=" + oauthConfig.getPassword()
                                            + oauthConfig.getSecurityCode();
                                    CommonData commonData = new CommonData();
                                    commonData.setCall_type("GET");
                                    commonData.setCallString(oauthUrl);

                                    HttpResult result = commonHttpClient.execute(oauthConfig.getReadTimedOutRetry(), commonData);
                                    if (result != null && StringUtils.isNotBlank(result.getResult())) {
                                        JSONObject jsonObject = JSONObject.parseObject(result.getResult());
                                        if (jsonObject.containsKey("access_token")) {
                                            accessToken = jsonObject.getString("access_token");
                                            log.debug("TokenCache_Load加载token:" + accessToken);
                                            TokenCache.setKey("accessToken", accessToken);
                                        } else {
                                            if (jsonObject.containsKey("error_description")) {
                                                log.error(jsonObject.getString("error_description"));
                                            } else {
                                                log.error(jsonObject.toString());
                                            }
                                        }
                                    } else {
                                        log.error("can not get the accessToken,please check your config");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return accessToken;
                            }
                        });

        //创建任务队列
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                CommonHttpClient commonHttpClient = CommonHttpClient.instance();
                commonHttpClient.setContentEncoding("UTF-8");
                commonHttpClient.setContentType("application/json");

                String oauthUrl = oauthConfig.getDomain() + "/oauth2/token?grant_type=password" + "&client_id=" + oauthConfig.getClientId()
                        + "&client_secret=" + oauthConfig.getClientSecret() + "&username=" + oauthConfig.getUserName() + "&password=" + oauthConfig.getPassword()
                        + oauthConfig.getSecurityCode();
                CommonData commonData = new CommonData();
                commonData.setCall_type("GET");
                commonData.setCallString(oauthUrl);

                HttpResult result = commonHttpClient.execute(oauthConfig.getReadTimedOutRetry(), commonData);
                if (result != null && StringUtils.isNotBlank(result.getResult())) {
                    JSONObject jsonObject = JSONObject.parseObject(result.getResult());
                    if (jsonObject.containsKey("access_token")) {
                        String accessToken = jsonObject.getString("access_token");
                        log.debug("TokenCache定时获取token:" + accessToken);
                        TokenCache.setKey("accessToken", accessToken);
                    } else {
                        if (jsonObject.containsKey("error_description")) {
                            log.error(jsonObject.getString("error_description"));
                        } else {
                            log.error(jsonObject.toString());
                        }
                    }
                } else {
                    log.error("can not get the accessToken,please check your config");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 30, TimeUnit.MINUTES);
    }

    /**
     * 添加本地缓存
     *
     * @param key
     * @param value
     */
    public static void setKey(String key, String value) {
        localcache.put(key, value);
    }

    /**
     * 获取本地缓存
     *
     * @param key
     * @return 本地缓存token
     */
    public static String getKey(String key) {
        String value = null;
        try {
            value = localcache.get(key);
            if ("null".equals(value)) {
                return null;
            }
            log.debug("从缓存中获取token:" + value);
            return value;
        } catch (ExecutionException e) {
            log.error("获取本地缓存错误", e);
        }
        return null;
    }

    /**
     * 刷新本地缓存
     */
    public static void refreshAccessToken() {
        try {
            CommonHttpClient commonHttpClient = CommonHttpClient.instance();
            commonHttpClient.setContentEncoding("UTF-8");
            commonHttpClient.setContentType("application/json");

            String oauthUrl = oauthConfig.getDomain() + "/oauth2/token?grant_type=password" + "&client_id=" + oauthConfig.getClientId()
                    + "&client_secret=" + oauthConfig.getClientSecret() + "&username=" + oauthConfig.getUserName() + "&password=" + oauthConfig.getPassword()
                    + oauthConfig.getSecurityCode();
            CommonData commonData = new CommonData();
            commonData.setCall_type("GET");
            commonData.setCallString(oauthUrl);

            HttpResult result = commonHttpClient.execute(oauthConfig.getReadTimedOutRetry(), commonData);
            if (result != null && StringUtils.isNotBlank(result.getResult())) {
                JSONObject jsonObject = JSONObject.parseObject(result.getResult());
                if (jsonObject.containsKey("access_token")) {
                    String accessToken = jsonObject.getString("access_token");
                    log.debug("refreshAccessToken加载token:" + accessToken);
                    TokenCache.setKey("accessToken", accessToken);
                } else {
                    if (jsonObject.containsKey("error_description")) {
                        log.error(jsonObject.getString("error_description"));
                    } else {
                        log.error(jsonObject.toString());
                    }
                }
            } else {
                log.error("can not get the accessToken,please check your config");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}