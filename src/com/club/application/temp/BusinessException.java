package com.club.application.temp;

/**
 * 【临时类 · 仅用于 stu1 本地自测】
 * 正式版本由 stu3 提供（含错误码等），合并时请删除本类并改为 stu3 的包名。
 * 业务异常：Service 层抛出，由 Servlet 捕获后转成页面提示。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
