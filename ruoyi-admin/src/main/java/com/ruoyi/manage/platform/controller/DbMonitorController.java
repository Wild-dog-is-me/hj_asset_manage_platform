package com.ruoyi.manage.platform.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.manage.platform.domain.DbInstance;
import com.ruoyi.manage.platform.domain.DbMetricSnapshot;
import com.ruoyi.manage.platform.service.IDbInstanceService;
import com.ruoyi.manage.platform.service.IDbMetricSnapshotService;

/**
 * 数据库监控 Controller。
 * 提供实时指标采集、慢SQL查询、历史趋势、健康检查等接口。
 */
@RestController
@RequestMapping("/manage/platform/db/monitor")
public class DbMonitorController extends BaseController
{
    @Autowired
    private IDbInstanceService dbInstanceService;

    @Autowired
    private IDbMetricSnapshotService metricSnapshotService;

    /**
     * 采集指定实例的实时指标（不存库）。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:query')")
    @GetMapping("/collect/{instanceId}")
    public AjaxResult collect(@PathVariable Long instanceId)
    {
        DbInstance instance = dbInstanceService.getById(instanceId);
        if (instance == null)
        {
            return error("实例不存在");
        }
        // 解密密码
        DbInstance full = dbInstanceService.selectDbInstanceById(instanceId);
        Map<String, Object> metrics = metricSnapshotService.collectMetrics(full);
        return success(metrics);
    }

    /**
     * 采集并保存快照。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:query')")
    @GetMapping("/snapshot/{instanceId}")
    public AjaxResult snapshot(@PathVariable Long instanceId)
    {
        DbInstance instance = dbInstanceService.selectDbInstanceById(instanceId);
        if (instance == null)
        {
            return error("实例不存在");
        }
        DbMetricSnapshot snapshot = metricSnapshotService.collectAndSave(instance);
        return success(snapshot);
    }

    /**
     * 查询历史快照趋势数据。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:query')")
    @GetMapping("/history")
    public AjaxResult history(Long instanceId, String beginTime, String endTime)
    {
        List<DbMetricSnapshot> list = metricSnapshotService.selectSnapshotHistory(instanceId, beginTime, endTime);
        return success(list);
    }

    /**
     * 获取指定实例的慢SQL列表。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:query')")
    @GetMapping("/slowSql/{instanceId}")
    public AjaxResult slowSql(@PathVariable Long instanceId)
    {
        DbInstance instance = dbInstanceService.selectDbInstanceById(instanceId);
        if (instance == null)
        {
            return error("实例不存在");
        }
        List<Map<String, Object>> list = metricSnapshotService.getSlowSqlList(instance, 20);
        return success(list);
    }

    /**
     * 健康检查 - 对所有启用实例快速采集，返回简要连通性和核心指标。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:query')")
    @GetMapping("/health")
    public AjaxResult health()
    {
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        List<DbInstance> instances = dbInstanceService.selectEnabledInstances();
        for (DbInstance inst : instances)
        {
            DbInstance full = dbInstanceService.selectDbInstanceById(inst.getInstanceId());
            Map<String, Object> health = new java.util.LinkedHashMap<>();
            health.put("instanceId", full.getInstanceId());
            health.put("instanceName", full.getInstanceName());
            health.put("dbType", full.getDbType());
            try
            {
                Map<String, Object> metrics = metricSnapshotService.collectMetrics(full);
                health.put("isAlive", true);
                health.put("connections", metrics.get("connections"));
                health.put("maxConnections", metrics.get("maxConnections"));
                health.put("slowQueries", metrics.get("slowQueries"));
                health.put("cacheHitRatio", metrics.get("cacheHitRatio"));
            }
            catch (Exception e)
            {
                health.put("isAlive", false);
                health.put("error", e.getMessage());
            }
            list.add(health);
        }
        return success(list);
    }
}
