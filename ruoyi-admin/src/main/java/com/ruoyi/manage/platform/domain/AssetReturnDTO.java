package com.ruoyi.manage.platform.domain;

/**
 * 资产归还入参。
 */
public class AssetReturnDTO
{
    /** 资产ID */
    private Long assetId;

    /** 归还原因 */
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
