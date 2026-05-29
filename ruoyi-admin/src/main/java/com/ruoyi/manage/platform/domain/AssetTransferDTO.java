package com.ruoyi.manage.platform.domain;

/**
 * 资产调拨入参。
 */
public class AssetTransferDTO
{
    /** 资产ID */
    private Long assetId;

    /** 调入部门ID */
    private Long deptId;

    /** 调入使用人 */
    private String userName;

    /** 调入成本中心 */
    private String costCenter;

    /** 调拨原因 */
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

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getCostCenter()
    {
        return costCenter;
    }

    public void setCostCenter(String costCenter)
    {
        this.costCenter = costCenter;
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
