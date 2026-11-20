package com.club.application.temp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 【临时类 · 仅用于 stu1 本地自测】
 * 正式版本由 stu3 提供（com.club.application.util.DBUtil），合并时请删除本类。
 *
 * 约定与正式版一致：只有 Service 层可以调用 getConnection()，DAO 一律通过参数接收 Connection。
 * 连接参数可用 JVM 参数覆盖：-Dclubloan.db.url / -Dclubloan.db.user / -Dclubloan.db.password
 */
public final class TempDBUtil {

    private static final String URL =
            System.getProperty("clubloan.db.url",
                    "jdbc:mysql://localhost:3306/clubloan?useUnicode=true&characterEncoding=utf8&useSSL=false");
    private static final String USER =
            System.getProperty("clubloan.db.user", "root");
    private static final String PASSWORD =
            System.getProperty("clubloan.db.password", "123456");

    private static volatile boolean driverLoaded = false;

    private TempDBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        loadDriver();
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** 兼容旧驱动 5.1.x 与新版 8.x，避免换驱动时改代码 */
    private static void loadDriver() {
        if (driverLoaded) {
            return;
        }
        synchronized (TempDBUtil.class) {
            if (driverLoaded) {
                return;
            }
            String[] candidates = {
                    "com.mysql.cj.jdbc.Driver",   // mysql-connector-j 8.x
                    "com.mysql.jdbc.Driver"       // mysql-connector-java 5.1.x
            };
            RuntimeException last = null;
            for (String cls : candidates) {
                try {
                    Class.forName(cls);
                    driverLoaded = true;
                    return;
                } catch (ClassNotFoundException e) {
                    last = new RuntimeException("未找到 MySQL 驱动：" + cls + "，请检查 WEB-INF/lib", e);
                }
            }
            throw last;
        }
    }
}
