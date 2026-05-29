package com.ruoyi.manage.platform.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.manage.platform.domain.AssetInfo;
import com.ruoyi.manage.platform.domain.AssetReceiveDTO;
import com.ruoyi.manage.platform.domain.AssetReturnDTO;
import com.ruoyi.manage.platform.domain.AssetScrapDTO;
import com.ruoyi.manage.platform.domain.AssetTransferDTO;
import com.ruoyi.manage.platform.domain.AssetTransferRecord;
import com.ruoyi.manage.platform.mapper.AssetInfoMapper;
import com.ruoyi.manage.platform.service.IAssetInfoService;
import com.ruoyi.manage.platform.service.IAssetTransferRecordService;
import com.ruoyi.system.service.ISysDeptService;

/**
 * 资产信息 Service 实现。
 */
@Service
public class AssetInfoServiceImpl extends ServiceImpl<AssetInfoMapper, AssetInfo> implements IAssetInfoService
{
    /** 资产状态：库存中 */
    private static final String STATUS_IN_STOCK = "IN_STOCK";

    /** 资产状态：使用中 */
    private static final String STATUS_IN_USE = "IN_USE";

    /** 资产状态：已报废 */
    private static final String STATUS_SCRAPPED = "SCRAPPED";

    /** 流转类型：领用 */
    private static final String BIZ_RECEIVE = "RECEIVE";

    /** 流转类型：归还 */
    private static final String BIZ_RETURN = "RETURN";

    /** 流转类型：调拨 */
    private static final String BIZ_TRANSFER = "TRANSFER";

    /** 流转类型：报废 */
    private static final String BIZ_SCRAP = "SCRAP";

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private IAssetTransferRecordService transferRecordService;

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
        if (assetIds == null || assetIds.length == 0)
        {
            return 0;
        }
        for (Long assetId : assetIds)
        {
            List<AssetTransferRecord> records = transferRecordService.selectByAssetId(assetId);
            if (records != null && !records.isEmpty())
            {
                AssetInfo asset = getById(assetId);
                String assetLabel = asset != null && StringUtils.isNotEmpty(asset.getAssetNo())
                    ? asset.getAssetNo() : String.valueOf(assetId);
                throw new ServiceException("资产[" + assetLabel + "]已有流转记录，不允许删除，请使用报废操作");
            }
        }
        return removeByIds(Arrays.asList(assetIds)) ? assetIds.length : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int receiveAsset(AssetReceiveDTO dto)
    {
        if (dto == null || dto.getAssetId() == null)
        {
            throw new ServiceException("领用参数中资产ID不能为空");
        }
        if (dto.getDeptId() == null)
        {
            throw new ServiceException("领用部门不能为空");
        }
        if (StringUtils.isEmpty(dto.getUserName()))
        {
            throw new ServiceException("领用人不能为空");
        }
        AssetInfo asset = getById(dto.getAssetId());
        if (asset == null)
        {
            throw new ServiceException("操作失败，目标资产不存在");
        }
        if (!STATUS_IN_STOCK.equals(asset.getAssetStatus()))
        {
            throw new ServiceException("资产[" + assetLabel(asset) + "]当前状态为[" + asset.getAssetStatus() + "]，仅库存中的资产允许领用");
        }
        AssetTransferRecord record = new AssetTransferRecord();
        record.setAssetId(asset.getAssetId());
        record.setAssetNo(asset.getAssetNo());
        record.setBizType(BIZ_RECEIVE);
        record.setBeforeStatus(asset.getAssetStatus());
        record.setAfterStatus(STATUS_IN_USE);
        record.setBeforeDeptId(asset.getDeptId());
        record.setAfterDeptId(dto.getDeptId());
        record.setBeforeUserName(asset.getUserName());
        record.setAfterUserName(dto.getUserName());
        record.setBeforeCostCenter(asset.getCostCenter());
        record.setAfterCostCenter(dto.getCostCenter());
        record.setChangeReason(dto.getRemark());
        record.setOperator(dto.getOperator());
        record.setOperateTime(new Date());
        transferRecordService.insertTransferRecord(record);

        asset.setAssetStatus(STATUS_IN_USE);
        asset.setDeptId(dto.getDeptId());
        asset.setUserName(dto.getUserName());
        asset.setCostCenter(dto.getCostCenter());
        asset.setUpdateTime(DateUtils.getNowDate());
        return updateById(asset) ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int returnAsset(AssetReturnDTO dto)
    {
        if (dto == null || dto.getAssetId() == null)
        {
            throw new ServiceException("归还参数中资产ID不能为空");
        }
        AssetInfo asset = getById(dto.getAssetId());
        if (asset == null)
        {
            throw new ServiceException("操作失败，目标资产不存在");
        }
        if (!STATUS_IN_USE.equals(asset.getAssetStatus()))
        {
            throw new ServiceException("资产[" + assetLabel(asset) + "]当前状态为[" + asset.getAssetStatus() + "]，仅使用中的资产允许归还");
        }
        AssetTransferRecord record = new AssetTransferRecord();
        record.setAssetId(asset.getAssetId());
        record.setAssetNo(asset.getAssetNo());
        record.setBizType(BIZ_RETURN);
        record.setBeforeStatus(asset.getAssetStatus());
        record.setAfterStatus(STATUS_IN_STOCK);
        record.setBeforeDeptId(asset.getDeptId());
        record.setAfterDeptId(null);
        record.setBeforeUserName(asset.getUserName());
        record.setAfterUserName("");
        record.setBeforeCostCenter(asset.getCostCenter());
        record.setAfterCostCenter("");
        record.setChangeReason(dto.getRemark());
        record.setOperator(dto.getOperator());
        record.setOperateTime(new Date());
        transferRecordService.insertTransferRecord(record);

        asset.setAssetStatus(STATUS_IN_STOCK);
        asset.setDeptId(null);
        asset.setUserName("");
        asset.setCostCenter("");
        asset.setUpdateTime(DateUtils.getNowDate());
        return updateById(asset) ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int transferAsset(AssetTransferDTO dto)
    {
        if (dto == null || dto.getAssetId() == null)
        {
            throw new ServiceException("调拨参数中资产ID不能为空");
        }
        AssetInfo asset = getById(dto.getAssetId());
        if (asset == null)
        {
            throw new ServiceException("操作失败，目标资产不存在");
        }
        if (!STATUS_IN_USE.equals(asset.getAssetStatus()))
        {
            throw new ServiceException("资产[" + assetLabel(asset) + "]当前状态为[" + asset.getAssetStatus() + "]，仅使用中的资产允许调拨");
        }
        boolean deptChanged = dto.getDeptId() != null && !dto.getDeptId().equals(asset.getDeptId());
        boolean userChanged = StringUtils.isNotEmpty(dto.getUserName()) && !dto.getUserName().equals(asset.getUserName());
        boolean costChanged = StringUtils.isNotEmpty(dto.getCostCenter()) && !dto.getCostCenter().equals(asset.getCostCenter());
        if (!deptChanged && !userChanged && !costChanged)
        {
            throw new ServiceException("调拨目标与当前归属相同，无需调拨");
        }
        AssetTransferRecord record = new AssetTransferRecord();
        record.setAssetId(asset.getAssetId());
        record.setAssetNo(asset.getAssetNo());
        record.setBizType(BIZ_TRANSFER);
        record.setBeforeStatus(asset.getAssetStatus());
        record.setAfterStatus(STATUS_IN_USE);
        record.setBeforeDeptId(asset.getDeptId());
        record.setAfterDeptId(deptChanged ? dto.getDeptId() : asset.getDeptId());
        record.setBeforeUserName(asset.getUserName());
        record.setAfterUserName(userChanged ? dto.getUserName() : asset.getUserName());
        record.setBeforeCostCenter(asset.getCostCenter());
        record.setAfterCostCenter(costChanged ? dto.getCostCenter() : asset.getCostCenter());
        record.setChangeReason(dto.getRemark());
        record.setOperator(dto.getOperator());
        record.setOperateTime(new Date());
        transferRecordService.insertTransferRecord(record);

        if (deptChanged)
        {
            asset.setDeptId(dto.getDeptId());
        }
        if (userChanged)
        {
            asset.setUserName(dto.getUserName());
        }
        if (costChanged)
        {
            asset.setCostCenter(dto.getCostCenter());
        }
        asset.setUpdateTime(DateUtils.getNowDate());
        return updateById(asset) ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scrapAsset(AssetScrapDTO dto)
    {
        if (dto == null || dto.getAssetId() == null)
        {
            throw new ServiceException("报废参数中资产ID不能为空");
        }
        AssetInfo asset = getById(dto.getAssetId());
        if (asset == null)
        {
            throw new ServiceException("操作失败，目标资产不存在");
        }
        if (STATUS_SCRAPPED.equals(asset.getAssetStatus()))
        {
            throw new ServiceException("资产[" + assetLabel(asset) + "]已经是报废状态，不允许重复报废");
        }
        AssetTransferRecord record = new AssetTransferRecord();
        record.setAssetId(asset.getAssetId());
        record.setAssetNo(asset.getAssetNo());
        record.setBizType(BIZ_SCRAP);
        record.setBeforeStatus(asset.getAssetStatus());
        record.setAfterStatus(STATUS_SCRAPPED);
        record.setBeforeDeptId(asset.getDeptId());
        record.setAfterDeptId(null);
        record.setBeforeUserName(asset.getUserName());
        record.setAfterUserName("");
        record.setBeforeCostCenter(asset.getCostCenter());
        record.setAfterCostCenter("");
        record.setChangeReason(dto.getRemark());
        record.setOperator(dto.getOperator());
        record.setOperateTime(new Date());
        transferRecordService.insertTransferRecord(record);

        asset.setAssetStatus(STATUS_SCRAPPED);
        asset.setDeptId(null);
        asset.setUserName("");
        asset.setCostCenter("");
        asset.setUpdateTime(DateUtils.getNowDate());
        return updateById(asset) ? 1 : 0;
    }

    /**
     * 获取资产的可读标签，优先使用资产编号，没有则回退到ID。
     */
    private String assetLabel(AssetInfo asset)
    {
        if (asset == null)
        {
            return "未知资产";
        }
        return StringUtils.isNotEmpty(asset.getAssetNo()) ? asset.getAssetNo() : String.valueOf(asset.getAssetId());
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
