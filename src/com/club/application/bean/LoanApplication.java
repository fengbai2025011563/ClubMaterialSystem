package com.club.application.bean;

import java.sql.Timestamp;

import com.club.common.StatusUtil;

/**
 * 申请单头。状态常量在此定义，全组统一使用这一份。
 * 对应表：loan_applications
 */
public class LoanApplication {

    //状态常量
    public static final String PENDING   = "PENDING";
    public static final String APPROVED  = "APPROVED";
    public static final String REJECTED  = "REJECTED";
    public static final String BORROWED  = "BORROWED";
    public static final String RETURNED  = "RETURNED";
    public static final String CANCELLED = "CANCELLED";

    //字段
    private Integer id;
    private Integer applicantId;
    private String activityName;
    private String purpose;
    private String expectedStart;
    private String expectedReturn;
    private String status;
    private String clientToken;
    private Timestamp applyTime;
    private Integer reviewerId;
    private Timestamp reviewTime;
    private String reviewRemark;
    private Timestamp borrowTime;
    private Timestamp returnTime;
    /** 列表页展示用：物资种类数（findByApplicant 子查询得到，非表字段） */
    private int itemTypes;
    /** 管理端列表用：申请人姓名（联表得到，非表字段） */
    private String applicantName;

    public LoanApplication() {}

    // getter / setter
    public Integer getId()           { return id; }
    public void setId(Integer id)    { this.id = id; }

    public Integer getApplicantId()            { return applicantId; }
    public void setApplicantId(Integer id)     { this.applicantId = id; }

    public String getActivityName()            { return activityName; }
    public void setActivityName(String s)      { this.activityName = s; }

    public String getPurpose()                 { return purpose; }
    public void setPurpose(String s)          { this.purpose = s; }

    public String getExpectedStart()           { return expectedStart; }
    public void setExpectedStart(String s)     { this.expectedStart = s; }

    public String getExpectedReturn()          { return expectedReturn; }
    public void setExpectedReturn(String s)    { this.expectedReturn = s; }

    public String getStatus()                  { return status; }
    public void setStatus(String s)            { this.status = s; }

    public String getClientToken()             { return clientToken; }
    public void setClientToken(String s)       { this.clientToken = s; }

    public Timestamp getApplyTime()            { return applyTime; }
    public void setApplyTime(Timestamp t)      { this.applyTime = t; }

    public Integer getReviewerId()             { return reviewerId; }
    public void setReviewerId(Integer id)      { this.reviewerId = id; }

    public Timestamp getReviewTime()           { return reviewTime; }
    public void setReviewTime(Timestamp t)     { this.reviewTime = t; }

    public String getReviewRemark()            { return reviewRemark; }
    public void setReviewRemark(String s)      { this.reviewRemark = s; }

    public Timestamp getBorrowTime()           { return borrowTime; }
    public void setBorrowTime(Timestamp t)     { this.borrowTime = t; }

    public Timestamp getReturnTime()           { return returnTime; }
    public void setReturnTime(Timestamp t)     { this.returnTime = t; }

    public int getItemTypes()                  { return itemTypes; }
    public void setItemTypes(int n)            { this.itemTypes = n; }

    public String getApplicantName()           { return applicantName; }
    public void setApplicantName(String s)     { this.applicantName = s; }

    /** 页面显示用：状态中文。 */
    public String getStatusText()              { return StatusUtil.label(status); }

    public String getApplyTimeText()           { return fmtTime(applyTime); }
    public String getReviewTimeText()          { return fmtTime(reviewTime); }
    public String getBorrowTimeText()          { return fmtTime(borrowTime); }
    public String getReturnTimeText()          { return fmtTime(returnTime); }

    private String fmtTime(Timestamp t) {
        if (t == null) {
            return "";
        }
        String s = t.toString();
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }
}
