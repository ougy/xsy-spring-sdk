package com.rkhd.platform.sdk.util;

import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
    private static final int inputStreamLimit = 5242880;

    public static String toStringWithLimit(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        byte[] bytes = new byte[2048];
        int len;
        while ((len = inputStream.read(bytes)) != -1) {
            size += len;
            if (size > 5242880) {
                throw new IOException("the file is too large, max size is 5M");
            }
            sb.append(new String(bytes, 0, len, "UTF-8"));
        }
        return sb.toString();
    }
}