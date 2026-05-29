package com.ruoyi.manage.platform.domain;

/**
 * 资产报废入参。
 */
public class AssetScrapDTO
{
    /** 资产ID */
    private Long assetId;

    /** 报废原因 */
    private String remark;

    /** 操作人 */
    private String operator;

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }
}
