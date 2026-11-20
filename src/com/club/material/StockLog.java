package com.club.material;

import java.sql.Timestamp;

/**
 * 库存流水（对应 stock_logs 表的一行）。
 *
 * <p>【它像什么】像银行的对账单：每一次库存数字变化，都记一笔"什么时候、谁、因为什么、
 * 四个数量分别变了多少"。这张表只追加、不修改、不删除，出了问题可以顺着流水倒查。</p>
 *
 * <p>【怎么用】对账时把某个物资的所有 delta 相加，结果应该正好等于它当前的库存数字。
 * 这一条在答辩时可以现场跑一条 SQL 验证，是"数据没被改乱"最硬的证据。</p>
 */
public class StockLog {

    private long id;

    private int materialId;

    /** 关联申请单；管理员直接入库时为空 */
    private Integer applicationId;

    /** RESERVE 预留 / RELEASE 释放预留 / BORROW 借出 / RETURN 归还 / ADJUST 手工调整 */
    private String changeType;

    /** 可用数量的变化量，可正可负 */
    private int deltaAvailable;

    /** 预留数量的变化量 */
    private int deltaReserved;

    /** 借出数量的变化量 */
    private int deltaBorrowed;

    /** 总量的变化量，归还时损坏/遗失会让总量减少，所以可能为负 */
    private int deltaTotal;

    /** 操作人编号，可空 */
    private Integer operatorId;

    private String note;

    private Timestamp createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public int getDeltaAvailable() {
        return deltaAvailable;
    }

    public void setDeltaAvailable(int deltaAvailable) {
        this.deltaAvailable = deltaAvailable;
    }

    public int getDeltaReserved() {
        return deltaReserved;
    }

    public void setDeltaReserved(int deltaReserved) {
        this.deltaReserved = deltaReserved;
    }

    public int getDeltaBorrowed() {
        return deltaBorrowed;
    }

    public void setDeltaBorrowed(int deltaBorrowed) {
        this.deltaBorrowed = deltaBorrowed;
    }

    public int getDeltaTotal() {
        return deltaTotal;
    }

    public void setDeltaTotal(int deltaTotal) {
        this.deltaTotal = deltaTotal;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 流水类型的英文码转中文，页面上直接显示这一列。
     */
    public String getChangeTypeText() {
        if ("RESERVE".equals(changeType)) {
            return "预留";
        }
        if ("RELEASE".equals(changeType)) {
            return "释放预留";
        }
        if ("BORROW".equals(changeType)) {
            return "借出";
        }
        if ("RETURN".equals(changeType)) {
            return "归还";
        }
        if ("ADJUST".equals(changeType)) {
            return "手工调整";
        }
        return changeType;
    }

    /**
     * 把四个增量拼成一句话，例如 "可用-2，预留+2，借出0，总量0"。
     */
    public String getDeltaText() {
        return "可用" + signed(deltaAvailable)
                + "，预留" + signed(deltaReserved)
                + "，借出" + signed(deltaBorrowed)
                + "，总量" + signed(deltaTotal);
    }

    private String signed(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }
}
