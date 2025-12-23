package com.example.demo.common;

import lombok.Data;

@Data
public class Result {
    private int code; // 编码 200/400
    private String msg; // 成功/失败
    private Long total; // 总记录数（分页用）
    private Object data; // 数据

    public static Result fail() {
        return result(400, "失败", null, null);
    }

    public static Result fail(String msg) {
        return result(400, msg, null, null);
    }

    public static Result success(Object data, Long total) {
        return result(200, "成功", total, data);
    }

    public static Result success(Object data) {
        return result(200, "成功", null, data);
    }

    public static Result success(String msg) {
        return result(200, msg, null, null);
    }

    private static Result result(int code, String msg, Long total, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setTotal(total);
        result.setData(data);
        return result;
    }


}