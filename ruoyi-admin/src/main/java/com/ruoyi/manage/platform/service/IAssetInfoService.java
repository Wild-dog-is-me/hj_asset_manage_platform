package com.ruoyi.manage.platform.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.manage.platform.domain.AssetInfo;
import com.ruoyi.manage.platform.domain.AssetReceiveDTO;
import com.ruoyi.manage.platform.domain.AssetReturnDTO;
import com.ruoyi.manage.platform.domain.AssetScrapDTO;
import com.ruoyi.manage.platform.domain.AssetStatisticsDTO;
import com.ruoyi.manage.platform.domain.AssetTransferDTO;

/**
 * 资产信息 Service 接口。
 */
public interface IAssetInfoService extends IService<AssetInfo>
{
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo);

    public AssetInfo selectAssetInfoByAssetId(Long assetId);

    public int insertAssetInfo(AssetInfo assetInfo);

    public int updateAssetInfo(AssetInfo assetInfo);

    public int deleteAssetInfoByAssetIds(Long[] assetIds);

    /**
     * 领用资产。
     * 将资产从库存中状态变更为使用中，并绑定使用部门、使用人、成本中心。
     *
     * @param dto 领用参数
     * @return 影响行数
     */
    public int receiveAsset(AssetReceiveDTO dto);

    /**
     * 归还资产。
     * 将使用中的资产归还至库存，清空使用部门、使用人、成本中心。
     *
     * @param dto 归还参数
     * @return 影响行数
     */
    public int returnAsset(AssetReturnDTO dto);

    /**
     * 调拨资产。
     * 将资产从当前部门/使用人调拨到新部门/使用人，状态保持使用中不变。
     *
     * @param dto 调拨参数
     * @return 影响行数
     */
    public int transferAsset(AssetTransferDTO dto);

    /**
     * 报废资产。
     * 将资产状态变更为已报废，清空使用部门、使用人、成本中心。
     * 报废后不允许再做任何流转操作。
     *
     * @param dto 报废参数
     * @return 影响行数
     */
    public int scrapAsset(AssetScrapDTO dto);

    /**
     * 获取资产统计数据（首页仪表盘用）。
     *
     * @return 统计结果
     */
    public AssetStatisticsDTO getAssetStatistics();
}
