package com.rkhd.platform.sdk.util.aes;

import com.rkhd.platform.sdk.exception.AesException;

import javax.crypto.spec.SecretKeySpec;

public interface SecretkeyCreator {
    SecretKeySpec createSecretKey(String paramString) throws AesException;
}