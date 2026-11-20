package com.club.material;

import java.sql.Timestamp;

/**
 * 物资实体（对应 materials 表的一行）。
 *
 * <p>【它像什么】像仓库货架上的一张标签卡：写着这个物资叫什么、哪一类、
 * 一共多少、现在能借多少、有多少已经被审批占住、有多少正在别人手里。</p>
 *
 * <p>【四个数量怎么理解】总量 = 可用 + 预留 + 借出，任何时刻都成立。
 * 预留是"审批通过了但还没来拿"的部分；借出是"已经搬走了"的部分。
 * 页面上的数字全部来自这张表，不要在 JSP 里自己加减，容易和数据库对不上。</p>
 */
public class Material {

    /** 主键，0 表示还没入库的新物资 */
    private int id;

    /** 名称，全库唯一 */
    private String name;

    /** 分类，例如户外、家具、设备 */
    private String category;

    /** 计量单位，默认"件" */
    private String unit = "件";

    /** 库存总量 = 可用 + 预留 + 借出 */
    private int totalQty;

    /** 当前可借数量 */
    private int availableQty;

    /** 已被审批占用、还没搬走 */
    private int reservedQty;

    /** 已经借出、还没归还 */
    private int borrowedQty;

    /** ACTIVE 可借 / INACTIVE 停用 */
    private String status = "ACTIVE";

    /** 备注，例如存放地点 */
    private String remark;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    public int getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(int availableQty) {
        this.availableQty = availableQty;
    }

    public int getReservedQty() {
        return reservedQty;
    }

    public void setReservedQty(int reservedQty) {
        this.reservedQty = reservedQty;
    }

    public int getBorrowedQty() {
        return borrowedQty;
    }

    public void setBorrowedQty(int borrowedQty) {
        this.borrowedQty = borrowedQty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 页面显示用的状态中文名。
     */
    public String getStatusText() {
        return isActive() ? "可借" : "停用";
    }

    /**
     * 是否处于可借状态。新增物资、调整库存、占用库存都要先过这一关。
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * 被"锁住"的数量（预留 + 借出），用在详情页解释总量去哪了。
     */
    public int getLockedQty() {
        return reservedQty + borrowedQty;
    }
}
