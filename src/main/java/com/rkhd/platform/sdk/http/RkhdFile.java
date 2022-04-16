package com.rkhd.platform.sdk.http;

public class RkhdFile {
    private String fileName;
    private String fileContent;

    RkhdFile(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContent() {
        return this.fileContent;
    }
}