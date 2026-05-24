package com.ruoyi.manage.platform.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.manage.platform.domain.AssetInfo;
import com.ruoyi.manage.platform.mapper.AssetInfoMapper;
import com.ruoyi.manage.platform.service.IAssetInfoService;
import com.ruoyi.system.service.ISysDeptService;

@Service
public class AssetInfoServiceImpl extends ServiceImpl<AssetInfoMapper, AssetInfo> implements IAssetInfoService
{
    @Autowired
    private ISysDeptService deptService;

    @Override
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo)
    {
        LambdaQueryWrapper<AssetInfo> queryWrapper = buildQueryWrapper(assetInfo);
        List<AssetInfo> list = list(queryWrapper);
        setDeptNames(list);
        return list;
    }

    @Override
    public AssetInfo selectAssetInfoByAssetId(Long assetId)
    {
        AssetInfo assetInfo = getById(assetId);
        setDeptName(assetInfo);
        return assetInfo;
    }

    @Override
    public int insertAssetInfo(AssetInfo assetInfo)
    {
        assetInfo.setCreateTime(DateUtils.getNowDate());
        return save(assetInfo) ? 1 : 0;
    }

    @Override
    public int updateAssetInfo(AssetInfo assetInfo)
    {
        assetInfo.setUpdateTime(DateUtils.getNowDate());
        return updateById(assetInfo) ? 1 : 0;
    }

    @Override
    public int deleteAssetInfoByAssetIds(Long[] assetIds)
    {
        return removeByIds(Arrays.asList(assetIds)) ? assetIds.length : 0;
    }

    private LambdaQueryWrapper<AssetInfo> buildQueryWrapper(AssetInfo assetInfo)
    {
        LambdaQueryWrapper<AssetInfo> queryWrapper = new LambdaQueryWrapper<AssetInfo>();
        queryWrapper.eq(StringUtils.isNotEmpty(assetInfo.getAccountSet()), AssetInfo::getAccountSet, assetInfo.getAccountSet())
            .eq(StringUtils.isNotEmpty(assetInfo.getAssetCategory()), AssetInfo::getAssetCategory, assetInfo.getAssetCategory())
            .eq(StringUtils.isNotEmpty(assetInfo.getAssetStatus()), AssetInfo::getAssetStatus, assetInfo.getAssetStatus())
            .like(StringUtils.isNotEmpty(assetInfo.getAssetNo()), AssetInfo::getAssetNo, assetInfo.getAssetNo())
            .like(StringUtils.isNotEmpty(assetInfo.getDeviceNo()), AssetInfo::getDeviceNo, assetInfo.getDeviceNo())
            .like(StringUtils.isNotEmpty(assetInfo.getFinanceAccountNo()), AssetInfo::getFinanceAccountNo, assetInfo.getFinanceAccountNo())
            .eq(StringUtils.isNotEmpty(assetInfo.getDeviceType()), AssetInfo::getDeviceType, assetInfo.getDeviceType())
            .like(StringUtils.isNotEmpty(assetInfo.getAssetName()), AssetInfo::getAssetName, assetInfo.getAssetName())
            .like(StringUtils.isNotEmpty(assetInfo.getModel()), AssetInfo::getModel, assetInfo.getModel())
            .eq(StringUtils.isNotNull(assetInfo.getDeptId()), AssetInfo::getDeptId, assetInfo.getDeptId())
            .like(StringUtils.isNotEmpty(assetInfo.getCostCenter()), AssetInfo::getCostCenter, assetInfo.getCostCenter())
            .like(StringUtils.isNotEmpty(assetInfo.getUserName()), AssetInfo::getUserName, assetInfo.getUserName())
            .orderByDesc(AssetInfo::getCreateTime)
            .orderByDesc(AssetInfo::getAssetId);
        return queryWrapper;
    }

    private void setDeptNames(List<AssetInfo> list)
    {
        if (StringUtils.isEmpty(list))
        {
            return;
        }
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        Map<Long, SysDept> deptMap = depts.stream().collect(Collectors.toMap(SysDept::getDeptId, Function.identity(), (a, b) -> a));
        for (AssetInfo assetInfo : list)
        {
            if (StringUtils.isNotNull(assetInfo.getDeptId()) && deptMap.containsKey(assetInfo.getDeptId()))
            {
                assetInfo.setDeptName(deptMap.get(assetInfo.getDeptId()).getDeptName());
            }
        }
    }

    private void setDeptName(AssetInfo assetInfo)
    {
        if (StringUtils.isNull(assetInfo) || StringUtils.isNull(assetInfo.getDeptId()))
        {
            return;
        }
        SysDept dept = deptService.selectDeptById(assetInfo.getDeptId());
        if (StringUtils.isNotNull(dept))
        {
            assetInfo.setDeptName(dept.getDeptName());
        }
    }
}
