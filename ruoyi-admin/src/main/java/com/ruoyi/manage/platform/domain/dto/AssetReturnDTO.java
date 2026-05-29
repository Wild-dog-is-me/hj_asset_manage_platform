package com.ruoyi.manage.platform.domain.dto;

/**
 * 资产归还入参DTO。
 */
public class AssetReturnDTO
{
    private Long assetId;
    private String remark;
    private String operator;

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
}
