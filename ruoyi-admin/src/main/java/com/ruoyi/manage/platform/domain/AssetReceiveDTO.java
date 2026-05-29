package com.ruoyi.manage.platform.domain;

/**
 * 资产领用入参。
 */
public class AssetReceiveDTO
{
    /** 资产ID */
    private Long assetId;

    /** 领用部门ID */
    private Long deptId;

    /** 领用人 */
    private String userName;

    /** 成本中心 */
    private String costCenter;

    /** 领用原因 */
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
