package com.ruoyi.manage.platform.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.manage.platform.domain.InspectionItem;
import com.ruoyi.manage.platform.domain.InspectionItemField;
import com.ruoyi.manage.platform.mapper.InspectionItemFieldMapper;
import com.ruoyi.manage.platform.mapper.InspectionItemMapper;
import com.ruoyi.manage.platform.service.IInspectionItemService;

@Service
public class InspectionItemServiceImpl extends ServiceImpl<InspectionItemMapper, InspectionItem> implements IInspectionItemService
{
    @Autowired
    private InspectionItemFieldMapper fieldMapper;

    @Override
    public List<InspectionItem> selectInspectionItemList(InspectionItem inspectionItem)
    {
        LambdaQueryWrapper<InspectionItem> queryWrapper = new LambdaQueryWrapper<InspectionItem>()
            .like(StringUtils.isNotEmpty(inspectionItem.getItemName()), InspectionItem::getItemName, inspectionItem.getItemName())
            .eq(StringUtils.isNotEmpty(inspectionItem.getItemType()), InspectionItem::getItemType, inspectionItem.getItemType())
            .eq(StringUtils.isNotEmpty(inspectionItem.getStatus()), InspectionItem::getStatus, inspectionItem.getStatus())
            .orderByAsc(InspectionItem::getParentId)
            .orderByAsc(InspectionItem::getOrderNum)
            .orderByAsc(InspectionItem::getItemId);
        List<InspectionItem> list = list(queryWrapper);
        fillFields(list);
        return list;
    }

    @Override
    public List<InspectionItem> selectInspectionItemTree()
    {
        List<InspectionItem> list = selectInspectionItemList(new InspectionItem());
        return buildTree(list, 0L);
    }

    @Override
    public InspectionItem selectInspectionItemByItemId(Long itemId)
    {
        InspectionItem item = getById(itemId);
        if (StringUtils.isNotNull(item))
        {
            item.setFields(selectFieldsByItemId(itemId));
        }
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInspectionItem(InspectionItem inspectionItem)
    {
        InspectionItem parent = getById(inspectionItem.getParentId());
        inspectionItem.setAncestors(StringUtils.isNotNull(parent) ? parent.getAncestors() + "," + parent.getItemId() : "0");
        inspectionItem.setCreateTime(DateUtils.getNowDate());
        boolean result = save(inspectionItem);
        saveFields(inspectionItem);
        return result ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInspectionItem(InspectionItem inspectionItem)
    {
        InspectionItem parent = getById(inspectionItem.getParentId());
        inspectionItem.setAncestors(StringUtils.isNotNull(parent) ? parent.getAncestors() + "," + parent.getItemId() : "0");
        inspectionItem.setUpdateTime(DateUtils.getNowDate());
        boolean result = updateById(inspectionItem);
        fieldMapper.delete(new LambdaQueryWrapper<InspectionItemField>().eq(InspectionItemField::getItemId, inspectionItem.getItemId()));
        saveFields(inspectionItem);
        return result ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInspectionItemByItemId(Long itemId)
    {
        List<InspectionItem> children = list(new LambdaQueryWrapper<InspectionItem>().eq(InspectionItem::getParentId, itemId));
        if (StringUtils.isNotEmpty(children))
        {
            for (InspectionItem child : children)
            {
                deleteInspectionItemByItemId(child.getItemId());
            }
        }
        fieldMapper.delete(new LambdaQueryWrapper<InspectionItemField>().eq(InspectionItemField::getItemId, itemId));
        return removeById(itemId) ? 1 : 0;
    }

    private void fillFields(List<InspectionItem> list)
    {
        if (StringUtils.isEmpty(list))
        {
            return;
        }
        List<Long> itemIds = list.stream().map(InspectionItem::getItemId).collect(Collectors.toList());
        List<InspectionItemField> fields = fieldMapper.selectList(new LambdaQueryWrapper<InspectionItemField>()
            .in(InspectionItemField::getItemId, itemIds)
            .orderByAsc(InspectionItemField::getOrderNum)
            .orderByAsc(InspectionItemField::getFieldId));
        Map<Long, List<InspectionItemField>> fieldMap = fields.stream().collect(Collectors.groupingBy(InspectionItemField::getItemId));
        for (InspectionItem item : list)
        {
            item.setFields(fieldMap.get(item.getItemId()));
        }
    }

    private List<InspectionItemField> selectFieldsByItemId(Long itemId)
    {
        return fieldMapper.selectList(new LambdaQueryWrapper<InspectionItemField>()
            .eq(InspectionItemField::getItemId, itemId)
            .orderByAsc(InspectionItemField::getOrderNum)
            .orderByAsc(InspectionItemField::getFieldId));
    }

    private void saveFields(InspectionItem inspectionItem)
    {
        if (StringUtils.isEmpty(inspectionItem.getFields()))
        {
            return;
        }
        for (InspectionItemField field : inspectionItem.getFields())
        {
            field.setItemId(inspectionItem.getItemId());
            field.setCreateTime(DateUtils.getNowDate());
            fieldMapper.insert(field);
        }
    }

    private List<InspectionItem> buildTree(List<InspectionItem> list, Long parentId)
    {
        List<InspectionItem> tree = new ArrayList<InspectionItem>();
        for (InspectionItem item : list)
        {
            if (parentId.equals(item.getParentId()))
            {
                item.setChildren(buildTree(list, item.getItemId()));
                tree.add(item);
            }
        }
        return tree;
    }
}
