package com.club.user;

/**
 * 登录用户。
 * 字段与 users 表对应；password 只在登录校验时临时使用，登录成功后会被清空。
 */
public class User {

    private int id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public String getRoleText() {
        return isAdmin() ? "管理员" : "社团成员";
    }
}