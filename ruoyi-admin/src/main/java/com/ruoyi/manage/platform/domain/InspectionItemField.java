package com.ruoyi.manage.platform.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;

@TableName(value = "inspection_item_field", excludeProperty = {"searchValue", "params"})
public class InspectionItemField extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "field_id", type = IdType.AUTO)
    private Long fieldId;

    private Long itemId;

    private String fieldLabel;

    private String fieldKey;

    private String fieldType;

    private String fieldOptions;

    private String required;

    private Integer orderNum;

    public Long getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(Long fieldId)
    {
        this.fieldId = fieldId;
    }

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public String getFieldLabel()
    {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel)
    {
        this.fieldLabel = fieldLabel;
    }

    public String getFieldKey()
    {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey)
    {
        this.fieldKey = fieldKey;
    }

    public String getFieldType()
    {
        return fieldType;
    }

    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

    public String getFieldOptions()
    {
        return fieldOptions;
    }

    public void setFieldOptions(String fieldOptions)
    {
        this.fieldOptions = fieldOptions;
    }

    public String getRequired()
    {
        return required;
    }

    public void setRequired(String required)
    {
        this.required = required;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }
}
