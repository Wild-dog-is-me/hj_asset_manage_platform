package com.ruoyi.manage.platform.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

@TableName(value = "asset_info", excludeProperty = {"searchValue", "params"})
public class AssetInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "asset_id", type = IdType.AUTO)
    @Excel(name = "唯一ID", cellType = ColumnType.NUMERIC)
    private Long assetId;

    @Excel(name = "账套", dictType = "asset_account_set")
    private String accountSet;

    @Excel(name = "资产类别", dictType = "asset_category")
    private String assetCategory;

    @Excel(name = "资产状态", dictType = "asset_status")
    private String assetStatus;

    @Excel(name = "资产编号")
    private String assetNo;

    @Excel(name = "设备编号")
    private String deviceNo;

    @Excel(name = "财务帐编号")
    private String financeAccountNo;

    @Excel(name = "设备类别", dictType = "asset_device_type")
    private String deviceType;

    @Excel(name = "资产名称")
    private String assetName;

    @Excel(name = "型号")
    private String model;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "数量", cellType = ColumnType.NUMERIC)
    private BigDecimal quantity;

    private Long deptId;

    @TableField(exist = false)
    @Excel(name = "使用部门")
    private String deptName;

    @Excel(name = "成本中心")
    private String costCenter;

    @Excel(name = "使用人")
    private String userName;

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getAccountSet()
    {
        return accountSet;
    }

    public void setAccountSet(String accountSet)
    {
        this.accountSet = accountSet;
    }

    public String getAssetCategory()
    {
        return assetCategory;
    }

    public void setAssetCategory(String assetCategory)
    {
        this.assetCategory = assetCategory;
    }

    public String getAssetStatus()
    {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus)
    {
        this.assetStatus = assetStatus;
    }

    public String getAssetNo()
    {
        return assetNo;
    }

    public void setAssetNo(String assetNo)
    {
        this.assetNo = assetNo;
    }

    public String getDeviceNo()
    {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo)
    {
        this.deviceNo = deviceNo;
    }

    public String getFinanceAccountNo()
    {
        return financeAccountNo;
    }

    public void setFinanceAccountNo(String financeAccountNo)
    {
        this.financeAccountNo = financeAccountNo;
    }

    public String getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }

    public String getAssetName()
    {
        return assetName;
    }

    public void setAssetName(String assetName)
    {
        this.assetName = assetName;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public BigDecimal getQuantity()
    {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity)
    {
        this.quantity = quantity;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getCostCenter()
    {
        return costCenter;
    }

    public void setCostCenter(String costCenter)
    {
        this.costCenter = costCenter;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("assetId", getAssetId())
            .append("accountSet", getAccountSet())
            .append("assetCategory", getAssetCategory())
            .append("assetStatus", getAssetStatus())
            .append("assetNo", getAssetNo())
            .append("deviceNo", getDeviceNo())
            .append("financeAccountNo", getFinanceAccountNo())
            .append("deviceType", getDeviceType())
            .append("assetName", getAssetName())
            .append("model", getModel())
            .append("unit", getUnit())
            .append("quantity", getQuantity())
            .append("deptId", getDeptId())
            .append("deptName", getDeptName())
            .append("costCenter", getCostCenter())
            .append("userName", getUserName())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
