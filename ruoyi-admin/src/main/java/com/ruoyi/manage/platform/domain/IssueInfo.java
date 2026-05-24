package com.ruoyi.manage.platform.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

@TableName(value = "issue_info", excludeProperty = {"searchValue", "params"})
public class IssueInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "issue_id", type = IdType.AUTO)
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

    private Long applicantDeptId;

    @TableField(exist = false)
    @Excel(name = "申请部门")
    private String applicantDeptName;

    @Excel(name = "问题标题")
    private String issueTitle;

    private String issueContent;

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

    private String processDetail;

    @Excel(name = "是否自己解决", dictType = "issue_self_resolved")
    private String selfResolved;

    public Long getIssueId()
    {
        return issueId;
    }

    public void setIssueId(Long issueId)
    {
        this.issueId = issueId;
    }

    public String getIssueNo()
    {
        return issueNo;
    }

    public void setIssueNo(String issueNo)
    {
        this.issueNo = issueNo;
    }

    public String getAssetNo()
    {
        return assetNo;
    }

    public void setAssetNo(String assetNo)
    {
        this.assetNo = assetNo;
    }

    public String getSystemType()
    {
        return systemType;
    }

    public void setSystemType(String systemType)
    {
        this.systemType = systemType;
    }

    public Date getIssueSubmitTime()
    {
        return issueSubmitTime;
    }

    public void setIssueSubmitTime(Date issueSubmitTime)
    {
        this.issueSubmitTime = issueSubmitTime;
    }

    public Integer getResponseMinutes()
    {
        return responseMinutes;
    }

    public void setResponseMinutes(Integer responseMinutes)
    {
        this.responseMinutes = responseMinutes;
    }

    public String getBusinessEntity()
    {
        return businessEntity;
    }

    public void setBusinessEntity(String businessEntity)
    {
        this.businessEntity = businessEntity;
    }

    public String getApplicant()
    {
        return applicant;
    }

    public void setApplicant(String applicant)
    {
        this.applicant = applicant;
    }

    public Long getApplicantDeptId()
    {
        return applicantDeptId;
    }

    public void setApplicantDeptId(Long applicantDeptId)
    {
        this.applicantDeptId = applicantDeptId;
    }

    public String getApplicantDeptName()
    {
        return applicantDeptName;
    }

    public void setApplicantDeptName(String applicantDeptName)
    {
        this.applicantDeptName = applicantDeptName;
    }

    public String getIssueTitle()
    {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle)
    {
        this.issueTitle = issueTitle;
    }

    public String getIssueContent()
    {
        return issueContent;
    }

    public void setIssueContent(String issueContent)
    {
        this.issueContent = issueContent;
    }

    public String getUrgencyLevel()
    {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel)
    {
        this.urgencyLevel = urgencyLevel;
    }

    public String getIssueCategory()
    {
        return issueCategory;
    }

    public void setIssueCategory(String issueCategory)
    {
        this.issueCategory = issueCategory;
    }

    public String getProcessStatus()
    {
        return processStatus;
    }

    public void setProcessStatus(String processStatus)
    {
        this.processStatus = processStatus;
    }

    public String getKpiOwner()
    {
        return kpiOwner;
    }

    public void setKpiOwner(String kpiOwner)
    {
        this.kpiOwner = kpiOwner;
    }

    public String getCurrentSupportEngineer()
    {
        return currentSupportEngineer;
    }

    public void setCurrentSupportEngineer(String currentSupportEngineer)
    {
        this.currentSupportEngineer = currentSupportEngineer;
    }

    public String getProcessDetail()
    {
        return processDetail;
    }

    public void setProcessDetail(String processDetail)
    {
        this.processDetail = processDetail;
    }

    public String getSelfResolved()
    {
        return selfResolved;
    }

    public void setSelfResolved(String selfResolved)
    {
        this.selfResolved = selfResolved;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("issueId", getIssueId())
            .append("issueNo", getIssueNo())
            .append("assetNo", getAssetNo())
            .append("systemType", getSystemType())
            .append("issueSubmitTime", getIssueSubmitTime())
            .append("responseMinutes", getResponseMinutes())
            .append("businessEntity", getBusinessEntity())
            .append("applicant", getApplicant())
            .append("applicantDeptId", getApplicantDeptId())
            .append("applicantDeptName", getApplicantDeptName())
            .append("issueTitle", getIssueTitle())
            .append("issueContent", getIssueContent())
            .append("urgencyLevel", getUrgencyLevel())
            .append("issueCategory", getIssueCategory())
            .append("processStatus", getProcessStatus())
            .append("kpiOwner", getKpiOwner())
            .append("currentSupportEngineer", getCurrentSupportEngineer())
            .append("processDetail", getProcessDetail())
            .append("selfResolved", getSelfResolved())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
