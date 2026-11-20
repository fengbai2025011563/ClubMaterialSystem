package com.club.application.bean;

/**
 * 申请明细。一份申请头（LoanApplication）对应多条明细。
 * 查出时会 join materials 取 name 和 unit，所以有 materialName/unit 字段。
 * 对应表：application_items
 */
public class ApplicationItem {

    private Integer id;
    private Integer applicationId;
    private Integer materialId;
    /** join materials 得到的物资名称 */
    private String materialName;
    /** join materials 得到的计量单位 */
    private String unit;
    private Integer quantity;
    private Integer damagedQty;
    private Integer lostQty;
    private String remark;

    public ApplicationItem() {}

    // ===== getter / setter =====
    public Integer getId()                     { return id; }
    public void setId(Integer id)              { this.id = id; }

    public Integer getApplicationId()          { return applicationId; }
    public void setApplicationId(Integer id)   { this.applicationId = id; }

    public Integer getMaterialId()             { return materialId; }
    public void setMaterialId(Integer id)      { this.materialId = id; }

    public String getMaterialName()            { return materialName; }
    public void setMaterialName(String s)      { this.materialName = s; }

    public String getUnit()                    { return unit; }
    public void setUnit(String s)              { this.unit = s; }

    public Integer getQuantity()               { return quantity; }
    public void setQuantity(Integer q)         { this.quantity = q; }

    public Integer getDamagedQty()             { return damagedQty; }
    public void setDamagedQty(Integer q)       { this.damagedQty = q; }

    public Integer getLostQty()                { return lostQty; }
    public void setLostQty(Integer q)          { this.lostQty = q; }

    public String getRemark()                  { return remark; }
    public void setRemark(String s)            { this.remark = s; }
}
