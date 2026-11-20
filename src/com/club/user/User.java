package com.club.user;

/**
 * 登录用户（公共件，正式版由晏思汗在身份认证模块中维护）。
 *
 * <p>物资模块只需要用到"是谁在操作"和"是不是管理员"这两件事：
 * 管理员才能新增物资、入库、停用；普通成员只能浏览。</p>
 *
 * <p>字段与 users 表一一对应，这里只保留了物资模块会用到的部分，
 * 密码摘要不进这个对象，避免不小心带到页面上。</p>
 */
public class User {

    private int id;
    private String username;
    private String realName;
    /** MEMBER 普通成员 / ADMIN 管理员 */
    private String role;

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

    /**
     * 是否管理员。页面与 Servlet 都靠这一个判断决定"能不能改物资"。
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * 给页面显示的角色名。
     */
    public String getRoleText() {
        return isAdmin() ? "管理员" : "社团成员";
    }
}
