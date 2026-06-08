package com.ruoyi.manage.platform.service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.manage.platform.domain.DbInstance;
import com.ruoyi.manage.platform.domain.DbMetricSnapshot;

/**
 * 数据库指标快照 Service 接口。
 */
public interface IDbMetricSnapshotService extends IService<DbMetricSnapshot>
{
    /**
     * 采集指定实例的实时指标，不存库（仅在页面展示）。
     *
     * @param instance 数据库实例配置
     * @return 指标Map，包含 connections/maxConnections/slowQueries/qps/cacheHitRatio/memoryUsedPct/isAlive/extraJson
     */
    public Map<String, Object> collectMetrics(DbInstance instance);

    /**
     * 采集并保存快照记录。
     *
     * @param instance 数据库实例配置
     * @return 保存的快照
     */
    public DbMetricSnapshot collectAndSave(DbInstance instance);

    /**
     * 查询指定实例的历史快照（用于趋势图）。
     *
     * @param instanceId 实例ID
     * @param beginTime  开始时间
     * @param endTime    结束时间
     * @return 快照列表
     */
    public List<DbMetricSnapshot> selectSnapshotHistory(Long instanceId, String beginTime, String endTime);

    /**
     * 查询指定实例最近的N条快照。
     */
    public List<DbMetricSnapshot> selectLatestSnapshots(Long instanceId, int count);

    /**
     * 获取慢SQL列表（走 extraJson 中的 slowSqlList）。
     */
    public List<Map<String, Object>> getSlowSqlList(DbInstance instance, int topN);
}
