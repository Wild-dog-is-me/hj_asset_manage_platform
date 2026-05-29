package com.ruoyi.manage.platform.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.manage.platform.domain.AssetTransferRecord;
import com.ruoyi.manage.platform.mapper.AssetTransferRecordMapper;
import com.ruoyi.manage.platform.service.IAssetTransferRecordService;
import com.ruoyi.system.service.ISysDeptService;

/**
 * 资产流转记录 Service 实现。
 */
@Service
public class AssetTransferRecordServiceImpl extends ServiceImpl<AssetTransferRecordMapper, AssetTransferRecord> implements IAssetTransferRecordService
{
    @Autowired
    private ISysDeptService deptService;

    @Override
    public List<AssetTransferRecord> selectByAssetId(Long assetId)
    {
        LambdaQueryWrapper<AssetTransferRecord> queryWrapper = new LambdaQueryWrapper<AssetTransferRecord>();
        queryWrapper.eq(AssetTransferRecord::getAssetId, assetId)
            .orderByDesc(AssetTransferRecord::getOperateTime)
            .orderByDesc(AssetTransferRecord::getRecordId);
        List<AssetTransferRecord> list = list(queryWrapper);
        setDeptNames(list);
        return list;
    }

    @Override
    public int insertTransferRecord(AssetTransferRecord record)
    {
        if (record.getOperateTime() == null)
        {
            record.setOperateTime(new Date());
        }
        return save(record) ? 1 : 0;
    }

    @Override
    public List<AssetTransferRecord> selectTransferRecordList(AssetTransferRecord record)
    {
        LambdaQueryWrapper<AssetTransferRecord> queryWrapper = new LambdaQueryWrapper<AssetTransferRecord>();
        queryWrapper.like(StringUtils.isNotEmpty(record.getAssetNo()), AssetTransferRecord::getAssetNo, record.getAssetNo())
            .eq(StringUtils.isNotEmpty(record.getBizType()), AssetTransferRecord::getBizType, record.getBizType())
            .like(StringUtils.isNotEmpty(record.getOperator()), AssetTransferRecord::getOperator, record.getOperator())
            .orderByDesc(AssetTransferRecord::getOperateTime)
            .orderByDesc(AssetTransferRecord::getRecordId);
        List<AssetTransferRecord> list = list(queryWrapper);
        setDeptNames(list);
        return list;
    }

    /**
     * 批量设置流转记录中的部门名称。
     */
    private void setDeptNames(List<AssetTransferRecord> list)
    {
        if (StringUtils.isEmpty(list))
        {
            return;
        }
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        Map<Long, SysDept> deptMap = depts.stream().collect(Collectors.toMap(SysDept::getDeptId, Function.identity(), (a, b) -> a));
        for (AssetTransferRecord record : list)
        {
            if (StringUtils.isNotNull(record.getBeforeDeptId()) && deptMap.containsKey(record.getBeforeDeptId()))
            {
                record.setBeforeDeptName(deptMap.get(record.getBeforeDeptId()).getDeptName());
            }
            if (StringUtils.isNotNull(record.getAfterDeptId()) && deptMap.containsKey(record.getAfterDeptId()))
            {
                record.setAfterDeptName(deptMap.get(record.getAfterDeptId()).getDeptName());
            }
        }
    }
}
