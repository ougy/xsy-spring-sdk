package com.rkhd.platform.sdk.http;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.exception.XsyHttpException;
import com.rkhd.platform.sdk.http.handler.ResponseBodyHandler;
import com.rkhd.platform.sdk.util.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class CommonHttpClient {
    private CloseableHttpClient client;
    private String contentEncoding = "UTF-8";
    private String contentType = "application/json";
    private int errorMsgLength = 1000;//接口错误信息字节长度
    private int socketTimeout = 120000;
    private int connectionTimeout = 120000;
    private int readTimedOutRetry = 3;
    private RequestConfig config;

    private static final String RETRY_TASK_THREAD_NAME_FORMAT = "retry-thread-%d";
    private static final AtomicLong COUNT = new AtomicLong(0L);
    private static ExecutorService executorService;

    static {
        executorService = Executors.newFixedThreadPool(50);
    }

    private static ThreadFactory threadFactory() {
        return (runnable) -> {
            return new Thread(runnable, String.format(RETRY_TASK_THREAD_NAME_FORMAT, COUNT.getAndIncrement()));
        };
    }

    private static RejectedExecutionHandler rejectedExecutionHandler() {
        return (runnable, executor) -> {
            log.info(String.format("Thread {%s} has been rejected from {%s}", runnable.toString(), executor.toString()));
        };
    }

    public CommonHttpClient() {
        createClientWithoutSSL();
    }

    public CommonHttpClient(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        createClientWithoutSSL();
    }

    public CommonHttpClient(int socketTimeout, int connectionTimeout) {
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        createClientWithoutSSL();
    }

    public CommonHttpClient(int socketTimeout, int connectionTimeout, int readTimedOutRetry) {
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.readTimedOutRetry = readTimedOutRetry;
        createClientWithoutSSL();
    }

    public CommonHttpClient(int socketTimeout, int connectionTimeout, String contentType) {
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.contentType = contentType;
        createClientWithoutSSL();
    }

    public CommonHttpClient(int socketTimeout, int connectionTimeout, String contentEncoding, String contentType) {
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.contentEncoding = contentEncoding;
        this.contentType = contentType;
        createClientWithoutSSL();
    }

    public CommonHttpClient(int socketTimeout, int connectionTimeout, int readTimedOutRetry, String contentEncoding, String contentType) {
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.readTimedOutRetry = readTimedOutRetry;
        this.contentEncoding = contentEncoding;
        this.contentType = contentType;
        createClientWithoutSSL();
    }

    public static CommonHttpClient instance() {
        return new CommonHttpClient();
    }

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例
     * 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class CommonHttpClientHolder {
        // 静态初始化器，由JVM来保证线程安全
        private static CommonHttpClient instance = new CommonHttpClient();
    }

    //单例模式
    public static CommonHttpClient getInstance() {
        return CommonHttpClientHolder.instance;
    }

    public String performRequest(CommonData data) {
        HttpResult httpResult = execute(readTimedOutRetry, data);
        if (httpResult != null) {
            return httpResult.getResult();
        }
        return null;
    }

    public HttpResult execute(CommonData data) {
        return execute(readTimedOutRetry, data);
    }

    public HttpResult execute(int readTimedOutRetry, CommonData data) {
        HttpResult httpResult = new HttpResult();

        HttpResponse httpResponse = executeBefore(data);
        try {
            for (int i = 0; i < readTimedOutRetry; ++i) {
                if (httpResponse != null) {
                    httpResult.setHeaders(getHeaders(httpResponse.getAllHeaders()));
                    HttpEntity entity = httpResponse.getEntity();
                    if (entity != null) {
                        long contentLength = entity.getContentLength();
                        String result = EntityUtils.toString(entity, StandardCharsets.UTF_8.displayName());
                        httpResult.setResult(result);
                        httpResponse = executeAfter(contentLength, result, data);
                        if (httpResponse == null) {
                            return httpResult;
                        }
                    }
                } else {
                    //{"code":"1010002","msg":"中间件接口调用失败！","data":null,"errorInfo":null}
                    JSONObject obj = new JSONObject();
                    obj.put("code", 1010002);
                    obj.put("msg", "中间件接口调用失败！");
                    obj.put("data", null);
                    obj.put("errorInfo", null);
                    httpResult.setResult(obj.toJSONString());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("execute-Problem performing request: " + e.getMessage(), e);
            httpResult.setResult(e.getMessage());
        }
        return httpResult;
    }

    public HttpResponse executeBefore(final CommonData data) {
        Future<HttpResponse> future = executorService.submit(() -> {
            RateLimiter.acquire();

            try {
                HttpResponse httpResponse = executeRequest(data);
                return httpResponse;
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("request: [").append(data.toString()).append("]")
                        .append(", response: ").append(e.getMessage());
                //GET请求超时立即重试一次
                if (e.getMessage().equals("Read timed out") && (Optional.of(data).get()).getCall_type().toUpperCase().equals("GET")) {
                    sb.append(",超时重试...");
                    TimeUnit.MILLISECONDS.sleep(1L);
                    HttpResponse httpResponse = executeRequest(data);
                    return httpResponse;
                }

                log.error("method executeBefore Problem performing request: " + sb.toString(), e);
                return null;
            }
        });

        HttpResponse httpResponse = null;
        try {
            httpResponse = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
        return httpResponse;
    }

    private HttpResponse executeAfter(long contentLength, String result, CommonData data) {
        HttpResponse httpResponse = null;
        //增加errorMsgLength判断，为了避免某文本域中包括【用户访问频率超出限制】此内容
        //"msg":"用户访问频率超出限制"
        if (contentLength < errorMsgLength && result.contains("\"msg\":\"用户访问频率超出限制\"")) {
            JSONObject obj = JSONObject.parseObject(result);
            if ("1020025".equals(obj.getString("code")) || "1020024".equals(obj.getString("code"))) {
                StringBuilder sb = new StringBuilder();
                sb.append("request: [").append(data.toString()).append("]")
                        .append(", response: ").append(result)
                        .append(",访问超频率重试...");
                log.error("executeAfter判断超频重试," + sb.toString());
                try {
                    TimeUnit.MILLISECONDS.sleep(1L);
                    RateLimiter.acquire();
                    httpResponse = executeBefore(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return httpResponse;
            }
        }
        return httpResponse;
    }

    private List<HttpHeader> getHeaders(Header[] headers) {
        List<HttpHeader> httpHeaders = new ArrayList<>();
        if (headers != null && headers.length > 0) {
            for (Header header : headers) {
                HttpHeader httpHeader = new HttpHeader();
                httpHeader.setName(header.getName());
                httpHeader.setValue(header.getValue());
                httpHeaders.add(httpHeader);
            }
        }
        return httpHeaders;
    }

    public <T> CommonResponse<T> execute(CommonData commonData, ResponseBodyHandler<T> handler) throws XsyHttpException {
        try {
            HttpResponse httpResponse = executeRequest(commonData);
            CommonResponse<T> response = new CommonResponse<>();
            if (httpResponse != null) {
                response.setCode(httpResponse.getStatusLine().getStatusCode());
                response.setHeaders(getHeaders(httpResponse.getAllHeaders()));
                HttpEntity entity = httpResponse.getEntity();
                String data = null;
                if (entity != null) {
                    data = EntityUtils.toString(entity, StandardCharsets.UTF_8.displayName());
                }
                response.setData((T) handler.handle(data));
            }
            return response;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new XsyHttpException(e.getMessage(), Long.valueOf(100000L), e);
        }
    }

    private HttpResponse executeRequest(CommonData data) throws IOException {
        CloseableHttpResponse closeableHttpResponse;
        HttpResponse httpResponse1;
        String urlStr;
        HttpGet get;
        HttpPost post;
        HttpPatch patch;
        HttpPut put;
        HttpDeleteWithEntity delete;
        HttpResponse response = null;
        switch (((CommonData) Optional.<CommonData>of(data).get()).getCall_type().toUpperCase()) {
            case "GET":
                urlStr = data.getCallString();
                get = new HttpGet(urlStr);
                for (HttpHeader httpHeader : data.getHeaderList()) {
                    if ("Authorization".equals(httpHeader.getName())) {
                        get.setHeader(httpHeader.getName(), httpHeader.getValue());
                        continue;
                    }
                    get.addHeader(httpHeader.getName(), httpHeader.getValue());
                }

                return (HttpResponse) this.client.execute((HttpUriRequest) get);
            case "POST":
                urlStr = data.getCallString();
                post = new HttpPost(urlStr);
                httpResponse1 = executeHttpEntityEnclosingRequestBase((HttpEntityEnclosingRequestBase) post, data);

                return httpResponse1;
            case "PATCH":
                urlStr = data.getCallString();
                patch = new HttpPatch(urlStr);
                httpResponse1 = executeHttpEntityEnclosingRequestBase((HttpEntityEnclosingRequestBase) patch, data);
                return httpResponse1;
            case "PUT":
                urlStr = data.getCallString();
                put = new HttpPut(urlStr);
                httpResponse1 = executeHttpEntityEnclosingRequestBase((HttpEntityEnclosingRequestBase) put, data);
                return httpResponse1;
            case "DELETE":
                urlStr = data.getCallString();
                delete = new HttpDeleteWithEntity(urlStr);
                httpResponse1 = executeHttpEntityEnclosingRequestBase(delete, data);
                return httpResponse1;
        }
        String msg = "Unknown call type: [" + data.getCall_type() + "]";
        log.error(msg);
        throw new IOException(msg);
    }

    private HttpResponse executeHttpEntityEnclosingRequestBase(HttpEntityEnclosingRequestBase request, CommonData data) throws IOException {
        for (Map.Entry<String, String> entry : data.getHeaders().entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
        if (data.getFormData().size() != 0) {
            if ("urlEncoded".equals(data.getFormType())) {

                UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(getParam(data.getFormData()), this.contentEncoding);
                request.setEntity((HttpEntity) postEntity);
            } else {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                for (Map.Entry<String, Object> entry : data.getFormData().entrySet()) {
                    if (entry.getValue() instanceof RkhdFile) {
                        RkhdFile file = (RkhdFile) entry.getValue();
                        if (file.getFileName() == null) {
                            throw new IOException("RkhdFile name can not be null");
                        }
                        builder.addBinaryBody(entry.getKey(), file.getFileContent().getBytes("UTF-8"), ContentType.create("multipart/form-data"), file.getFileName());
                        continue;
                    }
                    builder.addTextBody(entry.getKey(), entry.getValue().toString());
                }

                request.setEntity(builder.build());
            }
        } else {
            StringEntity se = new StringEntity(data.getBody(), this.contentEncoding);
            se.setContentType(this.contentType);
            se.setContentEncoding((Header) new BasicHeader("Content-Encoding", this.contentEncoding));
            request.setEntity((HttpEntity) se);
        }
        data.setCallString(request.getURI().toString());
        return (HttpResponse) this.client.execute((HttpUriRequest) request);
    }

    private List<NameValuePair> getParam(Map parameterMap) {
        List<NameValuePair> param = new ArrayList<>();
        Iterator<Map.Entry> it = parameterMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry parmEntry = it.next();
            Object value = parmEntry.getValue();
            if (value == null) {
                value = "";
            }
            param.add(new BasicNameValuePair((String) parmEntry.getKey(), value
                    .toString()));
        }
        return param;
    }

    public void close() {
        try {
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createClientWithoutSSL() {
        try {
            this.config = RequestConfig.custom().setConnectTimeout(this.connectionTimeout).setSocketTimeout(this.socketTimeout).build();

            SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            //设置重试策略,最多重试3次
            DefaultServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy = new DefaultServiceUnavailableRetryStrategy(3, 1000);
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            this.client = HttpClients.custom().setSSLSocketFactory(sslsf)
                    .setDefaultRequestConfig(this.config).setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy).build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public void createSSLClient() {
        this.config = RequestConfig.custom().setConnectTimeout(this.connectionTimeout).setSocketTimeout(this.socketTimeout).build();
        this.client = HttpClients.custom().setDefaultRequestConfig(this.config).build();
    }

    public String getContentEncoding() {
        return this.contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
