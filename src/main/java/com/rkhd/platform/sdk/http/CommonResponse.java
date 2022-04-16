 package com.rkhd.platform.sdk.http;

 import java.util.List;

 public class CommonResponse<T> {
   int code;
   List<HttpHeader> headers;
   T data;

   public int getCode() {
     return this.code;
   }

   public void setCode(int code) {
     this.code = code;
   }

   public List<HttpHeader> getHeaders() {
     return this.headers;
   }

   public void setHeaders(List<HttpHeader> headers) {
     this.headers = headers;
   }

   public T getData() {
     return this.data;
   }

   public void setData(T data) {
     this.data = data;
   }
 }