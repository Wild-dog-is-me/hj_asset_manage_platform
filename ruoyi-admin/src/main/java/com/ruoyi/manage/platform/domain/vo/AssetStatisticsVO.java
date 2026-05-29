package com.ruoyi.manage.platform.domain.vo;

import java.util.List;
import java.util.Map;

/**
 * 资产统计结果VO，用于首页仪表盘展示。
 */
public class AssetStatisticsVO
{
    private Long totalCount;
    private Long inStockCount;
    private Long inUseCount;
    private Long scrappedCount;
    private List<Map<String, Object>> statusPieData;
    private List<Map<String, Object>> categoryPieData;

    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
    public Long getInStockCount() { return inStockCount; }
    public void setInStockCount(Long inStockCount) { this.inStockCount = inStockCount; }
    public Long getInUseCount() { return inUseCount; }
    public void setInUseCount(Long inUseCount) { this.inUseCount = inUseCount; }
    public Long getScrappedCount() { return scrappedCount; }
    public void setScrappedCount(Long scrappedCount) { this.scrappedCount = scrappedCount; }
    public List<Map<String, Object>> getStatusPieData() { return statusPieData; }
    public void setStatusPieData(List<Map<String, Object>> statusPieData) { this.statusPieData = statusPieData; }
    public List<Map<String, Object>> getCategoryPieData() { return categoryPieData; }
    public void setCategoryPieData(List<Map<String, Object>> categoryPieData) { this.categoryPieData = categoryPieData; }
}
