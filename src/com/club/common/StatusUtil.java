package com.club.common;

/**
 * 状态中文名。数据库里存英文常量，页面统一用这里转成中文。
 */
public class StatusUtil {

    public static String label(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "PENDING":   return "待审批";
            case "APPROVED":  return "已批准";
            case "REJECTED":  return "已驳回";
            case "BORROWED":  return "已借出";
            case "RETURNED":  return "已归还";
            case "CANCELLED": return "已取消";
            default:          return status;
        }
    }
}
