package com.ruoyi.manage.platform.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.manage.platform.domain.AssetInfo;

/**
 * 资产信息导出VO。
 */
public class AssetInfoExportVO
{
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
    @Excel(name = "使用部门")
    private String deptName;
    @Excel(name = "成本中心")
    private String costCenter;
    @Excel(name = "使用人")
    private String userName;
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

    public AssetInfoExportVO() {}

    public AssetInfoExportVO(AssetInfo assetInfo)
    {
        this.assetId = assetInfo.getAssetId();
        this.accountSet = assetInfo.getAccountSet();
        this.assetCategory = assetInfo.getAssetCategory();
        this.assetStatus = assetInfo.getAssetStatus();
        this.assetNo = assetInfo.getAssetNo();
        this.deviceNo = assetInfo.getDeviceNo();
        this.financeAccountNo = assetInfo.getFinanceAccountNo();
        this.deviceType = assetInfo.getDeviceType();
        this.assetName = assetInfo.getAssetName();
        this.model = assetInfo.getModel();
        this.unit = assetInfo.getUnit();
        this.quantity = assetInfo.getQuantity();
        this.deptName = assetInfo.getDeptName();
        this.costCenter = assetInfo.getCostCenter();
        this.userName = assetInfo.getUserName();
        this.remark = assetInfo.getRemark();
        this.createTime = assetInfo.getCreateTime();
        this.updateTime = assetInfo.getUpdateTime();
        this.createBy = assetInfo.getCreateBy();
        this.updateBy = assetInfo.getUpdateBy();
    }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public String getAccountSet() { return accountSet; }
    public void setAccountSet(String accountSet) { this.accountSet = accountSet; }
    public String getAssetCategory() { return assetCategory; }
    public void setAssetCategory(String assetCategory) { this.assetCategory = assetCategory; }
    public String getAssetStatus() { return assetStatus; }
    public void setAssetStatus(String assetStatus) { this.assetStatus = assetStatus; }
    public String getAssetNo() { return assetNo; }
    public void setAssetNo(String assetNo) { this.assetNo = assetNo; }
    public String getDeviceNo() { return deviceNo; }
    public void setDeviceNo(String deviceNo) { this.deviceNo = deviceNo; }
    public String getFinanceAccountNo() { return financeAccountNo; }
    public void setFinanceAccountNo(String financeAccountNo) { this.financeAccountNo = financeAccountNo; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getCostCenter() { return costCenter; }
    public void setCostCenter(String costCenter) { this.costCenter = costCenter; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
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
