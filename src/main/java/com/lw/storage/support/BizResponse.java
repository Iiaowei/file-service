package com.lw.storage.support;

public class BizResponse<T> {
    private String code;
    private String message;
    private T data;

    private BizResponse(String code, String message) {
        this(code, message, null);
    }


    private BizResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BizResponse<T> ok() {
        return new BizResponse<>("0000", "success");
    }

    public static <T> BizResponse<T> ok(T data) {
        return new BizResponse<>("0000", "success", data);
    }

    public static <T> BizResponse<T> error() {
        return new BizResponse<>("9999", "操作失败");
    }

    public static <T> BizResponse<T> error(String message) {
        return new BizResponse<>("9999", message);
    }

}
