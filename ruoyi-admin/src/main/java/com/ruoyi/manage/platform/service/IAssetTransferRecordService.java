package com.ruoyi.manage.platform.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.manage.platform.domain.AssetTransferRecord;

/**
 * 资产流转记录 Service 接口。
 */
public interface IAssetTransferRecordService extends IService<AssetTransferRecord>
{
    /**
     * 根据资产ID查询该资产的全部流转记录，按操作时间倒序排列。
     *
     * @param assetId 资产ID
     * @return 流转记录列表
     */
    public List<AssetTransferRecord> selectByAssetId(Long assetId);

    /**
     * 新增流转记录。
     * 调用方必须保证事务一致性。
     *
     * @param record 流转记录
     * @return 影响行数
     */
    public int insertTransferRecord(AssetTransferRecord record);

    /**
     * 分页查询流转记录（管理端审计用）。
     *
     * @param record 查询条件
     * @return 流转记录列表
     */
    public List<AssetTransferRecord> selectTransferRecordList(AssetTransferRecord record);
}
