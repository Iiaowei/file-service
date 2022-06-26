package com.lw.storage.exception;

public class FileDownlaodException extends Exception {

    public FileDownlaodException() {
        this("文件读取失败.");
    }

    public FileDownlaodException(String message) {
        super(message);
    }

    public FileDownlaodException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileDownlaodException(Throwable cause) {
        super(cause);
    }

    public FileDownlaodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
