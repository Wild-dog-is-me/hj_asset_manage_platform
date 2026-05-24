package com.ruoyi.manage.platform.domain;

import java.util.List;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;

@TableName(value = "inspection_item", excludeProperty = {"searchValue", "params"})
public class InspectionItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    private Long parentId;

    private String ancestors;

    private String itemName;

    private String itemType;

    private Integer orderNum;

    private String status;

    @TableField(exist = false)
    private List<InspectionItem> children;

    @TableField(exist = false)
    private List<InspectionItemField> fields;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getAncestors()
    {
        return ancestors;
    }

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }

    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public String getItemType()
    {
        return itemType;
    }

    public void setItemType(String itemType)
    {
        this.itemType = itemType;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<InspectionItem> getChildren()
    {
        return children;
    }

    public void setChildren(List<InspectionItem> children)
    {
        this.children = children;
    }

    public List<InspectionItemField> getFields()
    {
        return fields;
    }

    public void setFields(List<InspectionItemField> fields)
    {
        this.fields = fields;
    }
}
