package com.ruoyi.manage.platform.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据库指标快照实体，对应 db_metric_snapshot 表。
 * 记录每次指标采集的结果快照，支持后续趋势图分析。
 */
@TableName(value = "db_metric_snapshot", excludeProperty = {"searchValue", "params"})
public class DbMetricSnapshot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 快照主键 */
    @TableId(value = "snapshot_id", type = IdType.AUTO)
    private Long snapshotId;

    /** 关联实例ID */
    private Long instanceId;

    /** 采集时间 */
    private Date metricTime;

    /** 当前连接数 */
    private Integer connections;

    /** 最大连接数 */
    private Integer maxConnections;

    /** 慢查询累计数 */
    private Integer slowQueries;

    /** 每秒查询数(近似) */
    private BigDecimal qps;

    /** 缓存命中率(%) */
    private BigDecimal cacheHitRatio;

    /** 内存使用率(%) */
    private BigDecimal memoryUsedPct;

    /** 采集时是否连通：1正常 0不可达 */
    private String isAlive;

    /**
     * 扩展指标JSON。不同数据库类型存放不同的差异化指标：
     * MySQL: longTransactions(长事务列表), tableSizes(表空间), deadlocks(死锁次数), poolStatus(连接池), innodbStatus等
     * SQL Server: waitStats(等待统计TOP10), blockedSessions(阻塞会话), deadlockInfo(死锁信息), bufferCacheHit
     * Redis: bigKeys, hotKeys, fragmentationRatio, opsPerSec, expiredKeys, evictedKeys
     */
    private String extraJson;

    // ========== 非数据库字段（列表展示用） ==========

    /** 实例名称（关联查询填充） */
    @TableField(exist = false)
    private String instanceName;

    /** 实例的数据库类型 */
    @TableField(exist = false)
    private String dbType;

    // ========== Getters & Setters ==========

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }

    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }

    public Date getMetricTime() { return metricTime; }
    public void setMetricTime(Date metricTime) { this.metricTime = metricTime; }

    public Integer getConnections() { return connections; }
    public void setConnections(Integer connections) { this.connections = connections; }

    public Integer getMaxConnections() { return maxConnections; }
    public void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }

    public Integer getSlowQueries() { return slowQueries; }
    public void setSlowQueries(Integer slowQueries) { this.slowQueries = slowQueries; }

    public BigDecimal getQps() { return qps; }
    public void setQps(BigDecimal qps) { this.qps = qps; }

    public BigDecimal getCacheHitRatio() { return cacheHitRatio; }
    public void setCacheHitRatio(BigDecimal cacheHitRatio) { this.cacheHitRatio = cacheHitRatio; }

    public BigDecimal getMemoryUsedPct() { return memoryUsedPct; }
    public void setMemoryUsedPct(BigDecimal memoryUsedPct) { this.memoryUsedPct = memoryUsedPct; }

    public String getIsAlive() { return isAlive; }
    public void setIsAlive(String isAlive) { this.isAlive = isAlive; }

    public String getExtraJson() { return extraJson; }
    public void setExtraJson(String extraJson) { this.extraJson = extraJson; }

    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }

    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }
}
