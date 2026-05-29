package com.ruoyi.manage.platform.domain.dto;

/**
 * 资产领用入参DTO。
 */
public class AssetReceiveDTO
{
    private Long assetId;
    private Long deptId;
    private String userName;
    private String costCenter;
    private String remark;
    private String operator;

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getCostCenter() { return costCenter; }
    public void setCostCenter(String costCenter) { this.costCenter = costCenter; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
}
