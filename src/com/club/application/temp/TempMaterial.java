package com.club.application.temp;

/**
 * 【临时类 · 仅用于 stu1 本地自测】
 * 正式版本由 stu2 提供（com.club.application.bean.Material），合并时请删除本类。
 * 这里只保留 stu1 表单下拉需要的字段。
 */
public class TempMaterial {

    private Integer id;
    private String name;
    private String unit;
    private int available;

    public TempMaterial() {
    }

    public TempMaterial(Integer id, String name, String unit, int available) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.available = available;
    }

    public Integer getId()          { return id; }
    public void setId(Integer id)   { this.id = id; }

    public String getName()         { return name; }
    public void setName(String n)   { this.name = n; }

    public String getUnit()         { return unit; }
    public void setUnit(String u)   { this.unit = u; }

    public int getAvailable()       { return available; }
    public void setAvailable(int a) { this.available = a; }
}
