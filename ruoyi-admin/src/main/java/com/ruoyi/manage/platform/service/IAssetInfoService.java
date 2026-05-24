package com.ruoyi.manage.platform.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.manage.platform.domain.AssetInfo;

public interface IAssetInfoService extends IService<AssetInfo>
{
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo);

    public AssetInfo selectAssetInfoByAssetId(Long assetId);

    public int insertAssetInfo(AssetInfo assetInfo);

    public int updateAssetInfo(AssetInfo assetInfo);

    public int deleteAssetInfoByAssetIds(Long[] assetIds);
}
