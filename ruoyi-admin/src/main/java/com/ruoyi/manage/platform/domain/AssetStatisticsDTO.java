package com.ruoyi.manage.platform.domain;

import java.util.List;
import java.util.Map;

/**
 * 资产统计结果DTO，用于首页仪表盘展示。
 */
public class AssetStatisticsDTO
{
    /** 资产总数 */
    private Long totalCount;

    /** 库存中数量 */
    private Long inStockCount;

    /** 使用中数量 */
    private Long inUseCount;

    /** 已报废数量 */
    private Long scrappedCount;

    /** 资产状态分布饼图数据 */
    private List<Map<String, Object>> statusPieData;

    /** 资产类别分布饼图数据 */
    private List<Map<String, Object>> categoryPieData;

    public Long getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Long totalCount)
    {
        this.totalCount = totalCount;
    }

    public Long getInStockCount()
    {
        return inStockCount;
    }

    public void setInStockCount(Long inStockCount)
    {
        this.inStockCount = inStockCount;
    }

    public Long getInUseCount()
    {
        return inUseCount;
    }

    public void setInUseCount(Long inUseCount)
    {
        this.inUseCount = inUseCount;
    }

    public Long getScrappedCount()
    {
        return scrappedCount;
    }

    public void setScrappedCount(Long scrappedCount)
    {
        this.scrappedCount = scrappedCount;
    }

    public List<Map<String, Object>> getStatusPieData()
    {
        return statusPieData;
    }

    public void setStatusPieData(List<Map<String, Object>> statusPieData)
    {
        this.statusPieData = statusPieData;
    }

    public List<Map<String, Object>> getCategoryPieData()
    {
        return categoryPieData;
    }

    public void setCategoryPieData(List<Map<String, Object>> categoryPieData)
    {
        this.categoryPieData = categoryPieData;
    }
}
