package com.ruoyi.manage.platform.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产流转记录实体，对应 asset_transfer_record 表。
 * 用于记录资产每一次状态变更、归属变更、部门调拨、报废等业务动作的完整快照。
 */
@TableName(value = "asset_transfer_record", excludeProperty = {"searchValue", "params"})
public class AssetTransferRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流转记录主键 */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /** 关联资产ID */
    private Long assetId;

    /** 资产编号（冗余，方便查询展示） */
    private String assetNo;

    /** 流转类型（RECEIVE领用 / RETURN归还 / TRANSFER调拨 / SCRAP报废 / INFO_CHANGE信息变更） */
    private String bizType;

    /** 变更前资产状态 */
    private String beforeStatus;

    /** 变更后资产状态 */
    private String afterStatus;

    /** 变更前使用部门ID */
    private Long beforeDeptId;

    /** 变更后使用部门ID */
    private Long afterDeptId;

    /** 变更前使用人 */
    private String beforeUserName;

    /** 变更后使用人 */
    private String afterUserName;

    /** 变更前成本中心 */
    private String beforeCostCenter;

    /** 变更后成本中心 */
    private String afterCostCenter;

    /** 变更原因 */
    private String changeReason;

    /** 操作人 */
    private String operator;

    /** 操作时间 */
    private Date operateTime;

    /** 变更前部门名称（非数据库字段，列表展示用） */
    @TableField(exist = false)
    private String beforeDeptName;

    /** 变更后部门名称（非数据库字段，列表展示用） */
    @TableField(exist = false)
    private String afterDeptName;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getAssetNo()
    {
        return assetNo;
    }

    public void setAssetNo(String assetNo)
    {
        this.assetNo = assetNo;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public String getBeforeStatus()
    {
        return beforeStatus;
    }

    public void setBeforeStatus(String beforeStatus)
    {
        this.beforeStatus = beforeStatus;
    }

    public String getAfterStatus()
    {
        return afterStatus;
    }

    public void setAfterStatus(String afterStatus)
    {
        this.afterStatus = afterStatus;
    }

    public Long getBeforeDeptId()
    {
        return beforeDeptId;
    }

    public void setBeforeDeptId(Long beforeDeptId)
    {
        this.beforeDeptId = beforeDeptId;
    }

    public Long getAfterDeptId()
    {
        return afterDeptId;
    }

    public void setAfterDeptId(Long afterDeptId)
    {
        this.afterDeptId = afterDeptId;
    }

    public String getBeforeUserName()
    {
        return beforeUserName;
    }

    public void setBeforeUserName(String beforeUserName)
    {
        this.beforeUserName = beforeUserName;
    }

    public String getAfterUserName()
    {
        return afterUserName;
    }

    public void setAfterUserName(String afterUserName)
    {
        this.afterUserName = afterUserName;
    }

    public String getBeforeCostCenter()
    {
        return beforeCostCenter;
    }

    public void setBeforeCostCenter(String beforeCostCenter)
    {
        this.beforeCostCenter = beforeCostCenter;
    }

    public String getAfterCostCenter()
    {
        return afterCostCenter;
    }

    public void setAfterCostCenter(String afterCostCenter)
    {
        this.afterCostCenter = afterCostCenter;
    }

    public String getChangeReason()
    {
        return changeReason;
    }

    public void setChangeReason(String changeReason)
    {
        this.changeReason = changeReason;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }

    public String getBeforeDeptName()
    {
        return beforeDeptName;
    }

    public void setBeforeDeptName(String beforeDeptName)
    {
        this.beforeDeptName = beforeDeptName;
    }

    public String getAfterDeptName()
    {
        return afterDeptName;
    }

    public void setAfterDeptName(String afterDeptName)
    {
        this.afterDeptName = afterDeptName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("assetId", getAssetId())
            .append("assetNo", getAssetNo())
            .append("bizType", getBizType())
            .append("beforeStatus", getBeforeStatus())
            .append("afterStatus", getAfterStatus())
            .append("beforeDeptId", getBeforeDeptId())
            .append("afterDeptId", getAfterDeptId())
            .append("beforeUserName", getBeforeUserName())
            .append("afterUserName", getAfterUserName())
            .append("beforeCostCenter", getBeforeCostCenter())
            .append("afterCostCenter", getAfterCostCenter())
            .append("changeReason", getChangeReason())
            .append("operator", getOperator())
            .append("operateTime", getOperateTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
