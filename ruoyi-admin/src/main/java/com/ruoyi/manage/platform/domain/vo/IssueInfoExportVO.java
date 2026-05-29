package com.ruoyi.manage.platform.domain.vo;

import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.manage.platform.domain.IssueInfo;

/**
 * 问题信息导出VO。
 */
public class IssueInfoExportVO
{
    @Excel(name = "唯一ID", cellType = ColumnType.NUMERIC)
    private Long issueId;
    @Excel(name = "问题编号")
    private String issueNo;
    @Excel(name = "资产编号")
    private String assetNo;
    @Excel(name = "系统类别", dictType = "issue_system_type")
    private String systemType;
    @Excel(name = "问题提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date issueSubmitTime;
    @Excel(name = "问题响应分钟", cellType = ColumnType.NUMERIC)
    private Integer responseMinutes;
    @Excel(name = "业务主体", dictType = "issue_business_entity")
    private String businessEntity;
    @Excel(name = "申请人")
    private String applicant;
    @Excel(name = "申请部门")
    private String applicantDeptName;
    @Excel(name = "问题标题")
    private String issueTitle;
    @Excel(name = "紧急程度", dictType = "issue_urgency_level")
    private String urgencyLevel;
    @Excel(name = "问题类别")
    private String issueCategory;
    @Excel(name = "处理状态", dictType = "issue_process_status")
    private String processStatus;
    @Excel(name = "KPI担责人")
    private String kpiOwner;
    @Excel(name = "当前支持工程师")
    private String currentSupportEngineer;
    @Excel(name = "是否自己解决", dictType = "issue_self_resolved")
    private String selfResolved;
    @Excel(name = "备注")
    private String remark;
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    @Excel(name = "创建人")
    private String createBy;
    @Excel(name = "更新人")
    private String updateBy;

    public IssueInfoExportVO() {}

    public IssueInfoExportVO(IssueInfo issueInfo)
    {
        this.issueId = issueInfo.getIssueId();
        this.issueNo = issueInfo.getIssueNo();
        this.assetNo = issueInfo.getAssetNo();
        this.systemType = issueInfo.getSystemType();
        this.issueSubmitTime = issueInfo.getIssueSubmitTime();
        this.responseMinutes = issueInfo.getResponseMinutes();
        this.businessEntity = issueInfo.getBusinessEntity();
        this.applicant = issueInfo.getApplicant();
        this.applicantDeptName = issueInfo.getApplicantDeptName();
        this.issueTitle = issueInfo.getIssueTitle();
        this.urgencyLevel = issueInfo.getUrgencyLevel();
        this.issueCategory = issueInfo.getIssueCategory();
        this.processStatus = issueInfo.getProcessStatus();
        this.kpiOwner = issueInfo.getKpiOwner();
        this.currentSupportEngineer = issueInfo.getCurrentSupportEngineer();
        this.selfResolved = issueInfo.getSelfResolved();
        this.remark = issueInfo.getRemark();
        this.createTime = issueInfo.getCreateTime();
        this.updateTime = issueInfo.getUpdateTime();
        this.createBy = issueInfo.getCreateBy();
        this.updateBy = issueInfo.getUpdateBy();
    }

    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public String getIssueNo() { return issueNo; }
    public void setIssueNo(String issueNo) { this.issueNo = issueNo; }
    public String getAssetNo() { return assetNo; }
    public void setAssetNo(String assetNo) { this.assetNo = assetNo; }
    public String getSystemType() { return systemType; }
    public void setSystemType(String systemType) { this.systemType = systemType; }
    public Date getIssueSubmitTime() { return issueSubmitTime; }
    public void setIssueSubmitTime(Date issueSubmitTime) { this.issueSubmitTime = issueSubmitTime; }
    public Integer getResponseMinutes() { return responseMinutes; }
    public void setResponseMinutes(Integer responseMinutes) { this.responseMinutes = responseMinutes; }
    public String getBusinessEntity() { return businessEntity; }
    public void setBusinessEntity(String businessEntity) { this.businessEntity = businessEntity; }
    public String getApplicant() { return applicant; }
    public void setApplicant(String applicant) { this.applicant = applicant; }
    public String getApplicantDeptName() { return applicantDeptName; }
    public void setApplicantDeptName(String applicantDeptName) { this.applicantDeptName = applicantDeptName; }
    public String getIssueTitle() { return issueTitle; }
    public void setIssueTitle(String issueTitle) { this.issueTitle = issueTitle; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public String getIssueCategory() { return issueCategory; }
    public void setIssueCategory(String issueCategory) { this.issueCategory = issueCategory; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getKpiOwner() { return kpiOwner; }
    public void setKpiOwner(String kpiOwner) { this.kpiOwner = kpiOwner; }
    public String getCurrentSupportEngineer() { return currentSupportEngineer; }
    public void setCurrentSupportEngineer(String currentSupportEngineer) { this.currentSupportEngineer = currentSupportEngineer; }
    public String getSelfResolved() { return selfResolved; }
    public void setSelfResolved(String selfResolved) { this.selfResolved = selfResolved; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
}
