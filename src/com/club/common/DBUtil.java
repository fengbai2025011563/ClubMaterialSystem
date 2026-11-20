package com.club.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接工具类（公共件）。
 *
 * <p>【归属说明】正式版本由晏思汗维护，这里放的是"最小可用版"，
 * 只提供三样东西：拿连接、关连接、驱动加载。等他提交后整体替换即可，
 * 类名与方法签名保持 {@code DBUtil.getConnection()} 和 {@code DBUtil.close(...)} 不变。</p>
 *
 * <p>【它像什么】像快递站的取件口：任何一层想访问数据库，都不自己 new 连接，
 * 而是到这里领一个 Connection；用完再还回来（close），避免连接泄漏把数据库拖垮。</p>
 */
public final class DBUtil {

    /** 数据库地址：club_material 是库名，参数里处理了中文、时区与 SSL 警告 */
    private static final String URL = "jdbc:mysql://localhost:3306/club_material"
            + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
            + "&useSSL=false&allowPublicKeyRetrieval=true";

    /** 改成自己电脑上的 MySQL 账号密码即可 */
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    private DBUtil() {
        // 工具类不需要被实例化
    }

    /*
     * 静态代码块：类第一次被用到时执行一次，负责把 MySQL 驱动注册进 DriverManager。
     * 找不到驱动时直接抛出明确提示，比运行到一半报 "No suitable driver" 好排查。
     */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("找不到 MySQL 驱动，请检查 WEB-INF/lib 下是否有 mysql-connector-j 的 jar");
        }
    }

    /**
     * 领一个数据库连接。调用方用完必须 close。
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 安静地关闭若干个资源（Connection / PreparedStatement / ResultSet 都实现了 AutoCloseable）。
     * 关闭时出的异常不往上报，因为"关不掉"通常不代表业务失败，报出来反而干扰排查。
     */
    public static void close(AutoCloseable... resources) {
        if (resources == null) {
            return;
        }
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception ignored) {
                    // 关闭失败不影响主流程
                }
            }
        }
    }
}
