package com.ruoyi.manage.platform.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import com.ruoyi.manage.platform.domain.InspectionRecord;
import com.ruoyi.manage.platform.domain.InspectionRecordDetail;
import com.ruoyi.manage.platform.mapper.InspectionItemFieldMapper;
import com.ruoyi.manage.platform.mapper.InspectionItemMapper;
import com.ruoyi.manage.platform.mapper.InspectionRecordDetailMapper;
import com.ruoyi.manage.platform.mapper.InspectionRecordMapper;
import com.ruoyi.manage.platform.service.IInspectionRecordService;

@Service
public class InspectionRecordServiceImpl extends ServiceImpl<InspectionRecordMapper, InspectionRecord> implements IInspectionRecordService
{
    @Autowired
    private InspectionRecordDetailMapper detailMapper;

    @Autowired
    private InspectionItemMapper itemMapper;

    @Autowired
    private InspectionItemFieldMapper fieldMapper;

    @Override
    public List<InspectionRecord> selectInspectionRecordList(InspectionRecord inspectionRecord)
    {
        LambdaQueryWrapper<InspectionRecord> queryWrapper = new LambdaQueryWrapper<InspectionRecord>()
            .like(StringUtils.isNotEmpty(inspectionRecord.getRecordTitle()), InspectionRecord::getRecordTitle, inspectionRecord.getRecordTitle())
            .like(StringUtils.isNotEmpty(inspectionRecord.getInspector()), InspectionRecord::getInspector, inspectionRecord.getInspector())
            .eq(StringUtils.isNotEmpty(inspectionRecord.getStatus()), InspectionRecord::getStatus, inspectionRecord.getStatus())
            .eq(StringUtils.isNotNull(inspectionRecord.getInspectionDate()), InspectionRecord::getInspectionDate, inspectionRecord.getInspectionDate())
            .orderByDesc(InspectionRecord::getInspectionDate)
            .orderByDesc(InspectionRecord::getRecordId);
        return list(queryWrapper);
    }

    @Override
    public InspectionRecord selectInspectionRecordByRecordId(Long recordId)
    {
        InspectionRecord record = getById(recordId);
        if (StringUtils.isNotNull(record))
        {
            record.setDetails(detailMapper.selectList(new LambdaQueryWrapper<InspectionRecordDetail>()
                .eq(InspectionRecordDetail::getRecordId, recordId)
                .orderByAsc(InspectionRecordDetail::getOrderNum)
                .orderByAsc(InspectionRecordDetail::getDetailId)));
        }
        return record;
    }

    @Override
    public InspectionRecord buildInspectionRecordTemplate(InspectionRecord inspectionRecord)
    {
        List<InspectionItem> items = itemMapper.selectList(new LambdaQueryWrapper<InspectionItem>()
            .eq(InspectionItem::getStatus, "0")
            .orderByAsc(InspectionItem::getParentId)
            .orderByAsc(InspectionItem::getOrderNum)
            .orderByAsc(InspectionItem::getItemId));
        Map<Long, InspectionItem> itemMap = items.stream().collect(Collectors.toMap(InspectionItem::getItemId, Function.identity(), (a, b) -> a));
        List<InspectionItemField> fields = fieldMapper.selectList(new LambdaQueryWrapper<InspectionItemField>()
            .orderByAsc(InspectionItemField::getOrderNum)
            .orderByAsc(InspectionItemField::getFieldId));
        List<InspectionRecordDetail> details = new ArrayList<InspectionRecordDetail>();
        int orderNum = 1;
        for (InspectionItemField field : fields)
        {
            InspectionItem item = itemMap.get(field.getItemId());
            if (StringUtils.isNull(item) || !"CHECK".equals(item.getItemType()))
            {
                continue;
            }
            InspectionRecordDetail detail = new InspectionRecordDetail();
            detail.setItemId(item.getItemId());
            detail.setItemPath(buildItemPath(item, itemMap));
            detail.setItemName(item.getItemName());
            detail.setFieldId(field.getFieldId());
            detail.setFieldLabel(field.getFieldLabel());
            detail.setFieldKey(field.getFieldKey());
            detail.setFieldType(field.getFieldType());
            detail.setOrderNum(orderNum++);
            details.add(detail);
        }
        inspectionRecord.setDetails(details);
        return inspectionRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInspectionRecord(InspectionRecord inspectionRecord)
    {
        inspectionRecord.setCreateTime(DateUtils.getNowDate());
        boolean result = save(inspectionRecord);
        saveDetails(inspectionRecord);
        return result ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInspectionRecord(InspectionRecord inspectionRecord)
    {
        inspectionRecord.setUpdateTime(DateUtils.getNowDate());
        boolean result = updateById(inspectionRecord);
        detailMapper.delete(new LambdaQueryWrapper<InspectionRecordDetail>().eq(InspectionRecordDetail::getRecordId, inspectionRecord.getRecordId()));
        saveDetails(inspectionRecord);
        return result ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInspectionRecordByRecordIds(Long[] recordIds)
    {
        detailMapper.delete(new LambdaQueryWrapper<InspectionRecordDetail>().in(InspectionRecordDetail::getRecordId, Arrays.asList(recordIds)));
        return removeByIds(Arrays.asList(recordIds)) ? recordIds.length : 0;
    }

    private void saveDetails(InspectionRecord inspectionRecord)
    {
        if (StringUtils.isEmpty(inspectionRecord.getDetails()))
        {
            return;
        }
        for (InspectionRecordDetail detail : inspectionRecord.getDetails())
        {
            detail.setRecordId(inspectionRecord.getRecordId());
            detailMapper.insert(detail);
        }
    }

    private String buildItemPath(InspectionItem item, Map<Long, InspectionItem> itemMap)
    {
        List<String> names = new ArrayList<String>();
        InspectionItem current = item;
        while (StringUtils.isNotNull(current))
        {
            names.add(0, current.getItemName());
            current = itemMap.get(current.getParentId());
        }
        return String.join(" / ", names);
    }
}
