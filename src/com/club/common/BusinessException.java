package com.club.common;

/**
 * 业务异常（公共件，正式版同样由晏思汗维护）。
 *
 * <p>【为什么要单独一类】"库存不足""名称重复"这类问题不是程序坏了，
 * 而是用户的操作本身不合法。用 BusinessException 抛出，
 * Servlet 接住之后把 message 原样显示给用户；而 SQLException 属于真故障，
 * 提示语要收敛，不能把数据库报错直接甩给用户看。</p>
 *
 * <p>它继承 RuntimeException，好处是方法签名不用到处写 throws，
 * 拦截统一放在 Servlet 最外层。</p>
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
