package com.rkhd.platform.sdk.util;

import java.util.concurrent.TimeUnit;

/**
 * @author 欧桂源
 */
public class RateLimiter {
    private static final long[] QUEUE = new long[50];
    private static volatile int max = 20;

    public RateLimiter() {
    }

    public static void setRate(int max) {
        RateLimiter.max = max;
    }

    public static int acquire() throws InterruptedException {
        int wait;
        for(wait = 0; !canDo(); ++wait) {
            TimeUnit.SECONDS.sleep(1L);
        }

        return wait;
    }

    private static synchronized boolean canDo() {
        long t = System.currentTimeMillis();

        for(int i = 0; i < max; ++i) {
            if (QUEUE[i] < t) {
                QUEUE[i] = t + 1000L;
                return true;
            }
        }

        return false;
    }
}