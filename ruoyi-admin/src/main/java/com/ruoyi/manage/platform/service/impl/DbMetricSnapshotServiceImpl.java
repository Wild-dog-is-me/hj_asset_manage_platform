package com.ruoyi.manage.platform.service.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.manage.platform.domain.DbInstance;
import com.ruoyi.manage.platform.domain.DbMetricSnapshot;
import com.ruoyi.manage.platform.mapper.DbMetricSnapshotMapper;
import com.ruoyi.manage.platform.service.IDbMetricSnapshotService;
import redis.clients.jedis.Jedis;

/**
 * 数据库指标快照 Service 实现。
 * 内置三种数据库类型的指标采集器：MysqlCollector / SqlServerCollector / RedisCollector。
 */
@Service
public class DbMetricSnapshotServiceImpl extends ServiceImpl<DbMetricSnapshotMapper, DbMetricSnapshot> implements IDbMetricSnapshotService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * SQL Server 性能计数器是累积值，需要保留上次采样点计算区间 QPS。
     */
    private static final Map<String, SqlServerCounterSample> SQL_SERVER_QPS_SAMPLES = new ConcurrentHashMap<>();

    @Autowired
    private DbMetricSnapshotMapper snapshotMapper;

    // ==================== 采集入口 ====================

    @Override
    public Map<String, Object> collectMetrics(DbInstance instance)
    {
        String dbType = instance.getDbType();
        if ("MYSQL".equalsIgnoreCase(dbType))
        {
            return new MysqlCollector().collect(instance);
        }
        else if ("SQLSERVER".equalsIgnoreCase(dbType))
        {
            return new SqlServerCollector().collect(instance);
        }
        else if ("POSTGRESQL".equalsIgnoreCase(dbType))
        {
            return new PostgreSqlCollector().collect(instance);
        }
        else if ("REDIS".equalsIgnoreCase(dbType))
        {
            return new RedisCollector().collect(instance);
        }
        else
        {
            throw new RuntimeException("不支持的数据库类型: " + dbType);
        }
    }

    @Override
    public DbMetricSnapshot collectAndSave(DbInstance instance)
    {
        Map<String, Object> metrics = collectMetrics(instance);
        DbMetricSnapshot snapshot = mapToSnapshot(instance, metrics);
        save(snapshot);
        return snapshot;
    }

    @Override
    public List<DbMetricSnapshot> selectSnapshotHistory(Long instanceId, String beginTime, String endTime)
    {
        LambdaQueryWrapper<DbMetricSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DbMetricSnapshot::getInstanceId, instanceId)
            .ge(StringUtils.isNotEmpty(beginTime), DbMetricSnapshot::getMetricTime, beginTime)
            .le(StringUtils.isNotEmpty(endTime), DbMetricSnapshot::getMetricTime, endTime)
            .orderByAsc(DbMetricSnapshot::getMetricTime);
        return list(wrapper);
    }

    @Override
    public List<DbMetricSnapshot> selectLatestSnapshots(Long instanceId, int count)
    {
        LambdaQueryWrapper<DbMetricSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DbMetricSnapshot::getInstanceId, instanceId)
            .orderByDesc(DbMetricSnapshot::getMetricTime)
            .last("LIMIT " + count);
        return list(wrapper);
    }

    @Override
    public List<Map<String, Object>> getSlowSqlList(DbInstance instance, int topN)
    {
        Map<String, Object> metrics = collectMetrics(instance);
        Object extra = metrics.get("extraJson");
        if (extra instanceof Map)
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> extraMap = (Map<String, Object>) extra;
            Object slowList = extraMap.get("slowSqlList");
            if (slowList instanceof List)
            {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) slowList;
                if (list.size() > topN)
                {
                    return list.subList(0, topN);
                }
                return list;
            }
        }
        return new ArrayList<>();
    }

    // ==================== 快照映射 ====================

    private DbMetricSnapshot mapToSnapshot(DbInstance instance, Map<String, Object> metrics)
    {
        DbMetricSnapshot snapshot = new DbMetricSnapshot();
        snapshot.setInstanceId(instance.getInstanceId());
        snapshot.setMetricTime(new Date());
        snapshot.setConnections(toInt(metrics.get("connections")));
        snapshot.setMaxConnections(toInt(metrics.get("maxConnections")));
        snapshot.setSlowQueries(toInt(metrics.get("slowQueries")));
        snapshot.setQps(toBigDecimal(metrics.get("qps")));
        snapshot.setCacheHitRatio(toBigDecimal(metrics.get("cacheHitRatio")));
        snapshot.setMemoryUsedPct(toBigDecimal(metrics.get("memoryUsedPct")));
        snapshot.setIsAlive("true".equals(String.valueOf(metrics.get("isAlive"))) ? "1" : "0");
        try
        {
            Object extra = metrics.get("extraJson");
            if (extra != null)
            {
                snapshot.setExtraJson(OBJECT_MAPPER.writeValueAsString(extra));
            }
        }
        catch (Exception ignored) {}
        return snapshot;
    }

    private Integer toInt(Object obj) { return obj instanceof Number ? ((Number) obj).intValue() : 0; }
    private BigDecimal toBigDecimal(Object obj) { return obj instanceof Number ? new BigDecimal(obj.toString()) : BigDecimal.ZERO; }

    // ==================== MySQL 采集器 ====================

    private static class MysqlCollector
    {
        Map<String, Object> collect(DbInstance instance)
        {
            Map<String, Object> result = new LinkedHashMap<>();
            Map<String, Object> extra = new LinkedHashMap<>();
            result.put("extraJson", extra);

            try (Connection conn = getMysqlConnection(instance))
            {
                result.put("isAlive", true);

                // 版本和运行时长
                try (PreparedStatement ps = conn.prepareStatement("SELECT VERSION()"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) extra.put("version", rs.getString(1));
                    rs.close();
                }
                try (PreparedStatement ps = conn.prepareStatement("SHOW GLOBAL STATUS LIKE 'Uptime'"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) extra.put("uptimeSeconds", Long.parseLong(rs.getString(2)));
                    rs.close();
                }

                // 连接数和最大连接数
                int threadsConnected = 0, threadsRunning = 0, maxConn = 0;
                try (PreparedStatement ps = conn.prepareStatement("SHOW GLOBAL STATUS LIKE 'Threads%'"))
                {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                    {
                        if ("Threads_connected".equals(rs.getString(1))) threadsConnected = Integer.parseInt(rs.getString(2));
                        if ("Threads_running".equals(rs.getString(1)))  threadsRunning  = Integer.parseInt(rs.getString(2));
                    }
                    rs.close();
                }
                try (PreparedStatement ps = conn.prepareStatement("SHOW VARIABLES LIKE 'max_connections'"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) maxConn = Integer.parseInt(rs.getString(2));
                    rs.close();
                }
                result.put("connections", threadsConnected);
                result.put("maxConnections", maxConn);
                extra.put("threadsRunning", threadsRunning);

                // 慢查询
                int slowQueries = 0;
                try (PreparedStatement ps = conn.prepareStatement("SHOW GLOBAL STATUS LIKE 'Slow_queries'"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) slowQueries = Integer.parseInt(rs.getString(2));
                    rs.close();
                }
                result.put("slowQueries", slowQueries);

                // QPS估算(取Questions值)
                long questions = 0;
                try (PreparedStatement ps = conn.prepareStatement("SHOW GLOBAL STATUS LIKE 'Questions'"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) questions = Long.parseLong(rs.getString(2));
                    rs.close();
                }
                result.put("qps", new BigDecimal(questions).divide(new BigDecimal(extra.get("uptimeSeconds").toString()), 2, java.math.RoundingMode.HALF_UP));

                // InnoDB缓冲池命中率
                long reads = 0, readRequests = 0;
                try (PreparedStatement ps = conn.prepareStatement("SHOW GLOBAL STATUS WHERE Variable_name IN ('Innodb_buffer_pool_reads','Innodb_buffer_pool_read_requests')"))
                {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                    {
                        if ("Innodb_buffer_pool_reads".equals(rs.getString(1))) reads = Long.parseLong(rs.getString(2));
                        if ("Innodb_buffer_pool_read_requests".equals(rs.getString(1))) readRequests = Long.parseLong(rs.getString(2));
                    }
                    rs.close();
                }
                if (readRequests > 0)
                    result.put("cacheHitRatio", new BigDecimal(100.0 * (1.0 - (double) reads / readRequests)).setScale(2, java.math.RoundingMode.HALF_UP));
                else
                    result.put("cacheHitRatio", BigDecimal.valueOf(100));

                result.put("memoryUsedPct", BigDecimal.ZERO); // MySQL不直接暴露内存

                // 死锁次数
                try (PreparedStatement ps = conn.prepareStatement("SHOW GLOBAL STATUS LIKE 'Innodb_deadlocks'"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) extra.put("deadlocks", Integer.parseInt(rs.getString(2)));
                    rs.close();
                }

                // 异常连接数
                try (PreparedStatement ps = conn.prepareStatement("SHOW GLOBAL STATUS LIKE 'Aborted_connects'"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) extra.put("abortedConnects", Integer.parseInt(rs.getString(2)));
                    rs.close();
                }

                // 锁等待
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.innodb_lock_waits"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) extra.put("lockWaits", rs.getInt(1));
                    rs.close();
                }
                catch (Exception ignored) { extra.put("lockWaits", -1); }

                // 长事务（超过60秒）
                List<Map<String, Object>> longTrx = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT trx_id, trx_state, trx_started, TIMESTAMPDIFF(SECOND, trx_started, NOW()) AS duration_sec, trx_mysql_thread_id " +
                    "FROM information_schema.innodb_trx WHERE TIMESTAMPDIFF(SECOND, trx_started, NOW()) > 60 ORDER BY trx_started"))
                {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                    {
                        Map<String, Object> trx = new LinkedHashMap<>();
                        trx.put("trxId", rs.getString("trx_id"));
                        trx.put("state", rs.getString("trx_state"));
                        trx.put("started", rs.getString("trx_started"));
                        trx.put("durationSec", rs.getInt("duration_sec"));
                        trx.put("threadId", rs.getInt("trx_mysql_thread_id"));
                        longTrx.add(trx);
                    }
                    rs.close();
                }
                catch (Exception ignored) {}
                extra.put("longTransactions", longTrx);
                extra.put("longTransactionCount", longTrx.size());

                // 表空间统计
                List<Map<String, Object>> tableSizes = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT TABLE_NAME, TABLE_ROWS, ROUND(DATA_LENGTH/1024/1024,2) AS data_mb, " +
                    "ROUND(INDEX_LENGTH/1024/1024,2) AS index_mb, ROUND((DATA_LENGTH+INDEX_LENGTH)/1024/1024,2) AS total_mb " +
                    "FROM information_schema.tables WHERE TABLE_SCHEMA = DATABASE() ORDER BY (DATA_LENGTH+INDEX_LENGTH) DESC LIMIT 20"))
                {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                    {
                        Map<String, Object> t = new LinkedHashMap<>();
                        t.put("tableName", rs.getString("TABLE_NAME"));
                        t.put("tableRows", rs.getLong("TABLE_ROWS"));
                        t.put("dataMb", rs.getDouble("data_mb"));
                        t.put("indexMb", rs.getDouble("index_mb"));
                        t.put("totalMb", rs.getDouble("total_mb"));
                        tableSizes.add(t);
                    }
                    rs.close();
                }
                catch (Exception ignored) {}
                extra.put("tableSizes", tableSizes);

                // 连接池状态（平台HikariCP）
                extra.put("poolStatus", "可通过Spring Actuator获取");

                // 慢SQL列表（从performance_schema）
                List<Map<String, Object>> slowSqlList = getMysqlSlowQueries(conn);
                extra.put("slowSqlList", slowSqlList);
            }
            catch (Exception e)
            {
                result.put("isAlive", false);
                extra.put("error", e.getMessage());
            }
            return result;
        }

        private Connection getMysqlConnection(DbInstance instance) throws Exception
        {
            String url = "jdbc:mysql://" + instance.getHost() + ":" + instance.getPort()
                + "/" + (StringUtils.isNotEmpty(instance.getDbName()) ? instance.getDbName() : "")
                + "?useSSL=false&connectTimeout=5000&socketTimeout=130000";
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, instance.getUsername(),
                StringUtils.isNotEmpty(instance.getPasswordRaw()) ? instance.getPasswordRaw() : instance.getPassword());
        }

        private List<Map<String, Object>> getMysqlSlowQueries(Connection conn)
        {
            List<Map<String, Object>> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT DIGEST_TEXT, COUNT_STAR, AVG_TIMER_WAIT/1000000000000 AS avg_sec, " +
                "SUM_ROWS_EXAMINED, SUM_ROWS_SENT FROM performance_schema.events_statements_summary_by_digest " +
                "WHERE DIGEST_TEXT IS NOT NULL ORDER BY AVG_TIMER_WAIT DESC LIMIT 20"))
            {
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sqlText", rs.getString("DIGEST_TEXT"));
                    row.put("execCount", rs.getLong("COUNT_STAR"));
                    row.put("avgSec", rs.getBigDecimal("avg_sec"));
                    row.put("rowsExamined", rs.getLong("SUM_ROWS_EXAMINED"));
                    row.put("rowsSent", rs.getLong("SUM_ROWS_SENT"));
                    list.add(row);
                }
                rs.close();
            }
            catch (Exception ignored) {}
            return list;
        }
    }

    // ==================== SQL Server 采集器 ====================

    /**
     * SQL Server 累积性能计数器采样点。
     */
    private static class SqlServerCounterSample
    {
        private final long value;
        private final long timeMillis;

        SqlServerCounterSample(long value, long timeMillis)
        {
            this.value = value;
            this.timeMillis = timeMillis;
        }
    }

    private static class SqlServerCollector
    {
        Map<String, Object> collect(DbInstance instance)
        {
            Map<String, Object> result = new LinkedHashMap<>();
            Map<String, Object> extra = new LinkedHashMap<>();
            result.put("extraJson", extra);
            result.put("connections", 0);
            result.put("maxConnections", 32767);
            result.put("slowQueries", 0);
            result.put("qps", BigDecimal.ZERO);
            result.put("cacheHitRatio", BigDecimal.ZERO);
            result.put("memoryUsedPct", BigDecimal.ZERO);

            try (Connection conn = getSqlServerConnection(instance))
            {
                result.put("isAlive", true);

                // 版本和启动时间
                try (PreparedStatement ps = conn.prepareStatement("SELECT @@VERSION, sqlserver_start_time FROM sys.dm_os_sys_info"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) { extra.put("version", rs.getString(1)); extra.put("startTime", rs.getString(2)); }
                    rs.close();
                }

                // 连接数
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM sys.dm_exec_connections"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) result.put("connections", rs.getInt(1));
                    rs.close();
                }
                // 最大连接数，SQL Server 默认上限为 32767；显式配置时从 sys.configurations 读取，读取失败时保留默认值。
                result.put("maxConnections", 32767);
                try (PreparedStatement ps = conn.prepareStatement("SELECT CAST(value_in_use AS int) FROM sys.configurations WHERE name = 'user connections'"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) result.put("maxConnections", rs.getInt(1));
                    rs.close();
                }
                catch (Exception e) { extra.put("maxConnectionsError", e.getMessage()); }

                // QPS：SQL Server 的 Batch Requests/sec 是累积性能计数器，需要用相邻两次采样差值计算区间每秒请求数。
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT cntr_value FROM sys.dm_os_performance_counters " +
                    "WHERE counter_name = 'Batch Requests/sec' AND object_name LIKE '%SQL Statistics%'"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next())
                    {
                        long counterValue = rs.getLong(1);
                        result.put("qps", calculateSqlServerQps(instance, counterValue));
                    }
                    rs.close();
                }
                catch (Exception e) { result.put("qps", BigDecimal.ZERO); extra.put("qpsError", e.getMessage()); }

                // SQL Server 缓存命中率由分子和 base 共同计算，直接读取单个计数器会得到错误比例。
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT counter_name, cntr_value FROM sys.dm_os_performance_counters " +
                    "WHERE counter_name IN ('Buffer cache hit ratio', 'Buffer cache hit ratio base') " +
                    "AND object_name LIKE '%Buffer Manager%'"))
                {
                    ResultSet rs = ps.executeQuery();
                    BigDecimal hitRatio = BigDecimal.ZERO;
                    BigDecimal hitRatioBase = BigDecimal.ZERO;
                    while (rs.next())
                    {
                        if ("Buffer cache hit ratio".equals(rs.getString("counter_name"))) hitRatio = rs.getBigDecimal("cntr_value");
                        if ("Buffer cache hit ratio base".equals(rs.getString("counter_name"))) hitRatioBase = rs.getBigDecimal("cntr_value");
                    }
                    if (hitRatioBase.compareTo(BigDecimal.ZERO) > 0)
                    {
                        result.put("cacheHitRatio", hitRatio.multiply(new BigDecimal(100)).divide(hitRatioBase, 2, java.math.RoundingMode.HALF_UP));
                    }
                    else
                    {
                        result.put("cacheHitRatio", BigDecimal.ZERO);
                    }
                    rs.close();
                }
                catch (Exception e) { result.put("cacheHitRatio", BigDecimal.ZERO); extra.put("cacheHitRatioError", e.getMessage()); }

                collectSqlServerSlowSql(instance, result, extra);

                result.put("memoryUsedPct", BigDecimal.ZERO);

                // 阻塞会话数
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(DISTINCT blocking_session_id) FROM sys.dm_exec_requests WHERE blocking_session_id <> 0"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) extra.put("blockedSessions", rs.getInt(1));
                    rs.close();
                }
                catch (Exception ignored) { extra.put("blockedSessions", 0); }

                // 等待统计TOP10
                List<Map<String, Object>> waitStats = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT TOP 10 wait_type, wait_time_ms, waiting_tasks_count FROM sys.dm_os_wait_stats " +
                    "WHERE wait_type NOT LIKE '%SLEEP%' ORDER BY wait_time_ms DESC"))
                {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                    {
                        Map<String, Object> w = new LinkedHashMap<>();
                        w.put("waitType", rs.getString("wait_type"));
                        w.put("waitTimeMs", rs.getLong("wait_time_ms"));
                        w.put("waitCount", rs.getLong("waiting_tasks_count"));
                        waitStats.add(w);
                    }
                    rs.close();
                }
                catch (Exception ignored) {}
                extra.put("waitStats", waitStats);

                // 数据库文件大小
                List<Map<String, Object>> dbSizes = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT DB_NAME(database_id) AS db_name, ROUND(SUM(size)*8/1024,2) AS size_mb " +
                    "FROM sys.master_files GROUP BY database_id"))
                {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                    {
                        Map<String, Object> d = new LinkedHashMap<>();
                        d.put("dbName", rs.getString("db_name"));
                        d.put("sizeMb", rs.getDouble("size_mb"));
                        dbSizes.add(d);
                    }
                    rs.close();
                }
                catch (Exception ignored) {}
                extra.put("dbSizes", dbSizes);
            }
            catch (Exception e)
            {
                result.put("isAlive", false);
                extra.put("error", e.getMessage());
            }
            return result;
        }

        /**
         * 慢 SQL 采集使用独立连接，避免计划缓存查询超时后关闭主监控连接。
         */
        private void collectSqlServerSlowSql(DbInstance instance, Map<String, Object> result, Map<String, Object> extra)
        {
            List<Map<String, Object>> slowSqlList = new ArrayList<>();
            try (Connection slowConn = getSqlServerConnection(instance, 5, 5000);
                 PreparedStatement ps = slowConn.prepareStatement(
                     "SELECT TOP 20 total_elapsed_time / 1000000.0 AS total_sec, execution_count, " +
                     "CASE WHEN execution_count > 0 THEN total_elapsed_time * 1.0 / execution_count / 1000000 ELSE 0 END AS avg_sec, " +
                     "total_logical_reads, CONVERT(NVARCHAR(MAX), text) AS sql_text " +
                     "FROM sys.dm_exec_query_stats CROSS APPLY sys.dm_exec_sql_text(sql_handle) " +
                     "WHERE execution_count > 0 ORDER BY total_elapsed_time * 1.0 / execution_count DESC"))
            {
                ps.setQueryTimeout(2);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sqlText", rs.getString("sql_text"));
                    row.put("execCount", rs.getLong("execution_count"));
                    row.put("avgSec", rs.getBigDecimal("avg_sec"));
                    row.put("totalSec", rs.getBigDecimal("total_sec"));
                    row.put("logicalReads", rs.getLong("total_logical_reads"));
                    slowSqlList.add(row);
                }
                rs.close();
            }
            catch (Exception e)
            {
                extra.put("slowSqlError", e.getMessage());
            }
            result.put("slowQueries", slowSqlList.size());
            extra.put("slowSqlList", slowSqlList);
        }

        /**
         * 根据 SQL Server 累积批处理请求计数器计算本次采样周期 QPS。
         */
        private BigDecimal calculateSqlServerQps(DbInstance instance, long currentValue)
        {
            String sampleKey = instance.getHost() + ":" + instance.getPort() + ":" + instance.getDbName();
            long now = System.currentTimeMillis();
            SqlServerCounterSample previous = SQL_SERVER_QPS_SAMPLES.put(sampleKey, new SqlServerCounterSample(currentValue, now));
            if (previous == null || now <= previous.timeMillis || currentValue < previous.value)
            {
                return BigDecimal.ZERO;
            }
            long elapsedMillis = now - previous.timeMillis;
            if (elapsedMillis <= 0)
            {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(currentValue - previous.value)
                .multiply(BigDecimal.valueOf(1000))
                .divide(BigDecimal.valueOf(elapsedMillis), 2, java.math.RoundingMode.HALF_UP);
        }

        private Connection getSqlServerConnection(DbInstance instance) throws Exception
        {
            return getSqlServerConnection(instance, 5, 30000);
        }

        /**
         * 创建 SQL Server 监控连接，可按采集项设置不同超时。socketTimeout 使用 JDBC 驱动要求的毫秒单位。
         */
        private Connection getSqlServerConnection(DbInstance instance, int loginTimeoutSeconds, int socketTimeoutMillis) throws Exception
        {
            String url = "jdbc:sqlserver://" + instance.getHost() + ":" + instance.getPort()
                + (StringUtils.isNotEmpty(instance.getDbName()) ? ";databaseName=" + instance.getDbName() : "")
                + ";encrypt=false;trustServerCertificate=true;loginTimeout=" + loginTimeoutSeconds
                + ";socketTimeout=" + socketTimeoutMillis + ";applicationName=RuoYiDbMonitor";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(url, instance.getUsername(),
                StringUtils.isNotEmpty(instance.getPasswordRaw()) ? instance.getPasswordRaw() : instance.getPassword());
        }
    }

    // ==================== PostgreSQL 采集器 ====================

    private static class PostgreSqlCollector
    {
        Map<String, Object> collect(DbInstance instance)
        {
            Map<String, Object> result = new LinkedHashMap<>();
            Map<String, Object> extra = new LinkedHashMap<>();
            result.put("extraJson", extra);

            try (Connection conn = getPostgreSqlConnection(instance))
            {
                result.put("isAlive", true);

                // 版本和启动时间
                try (PreparedStatement ps = conn.prepareStatement("SELECT version(), pg_postmaster_start_time()"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) { extra.put("version", rs.getString(1)); extra.put("startTime", rs.getString(2)); }
                    rs.close();
                }

                // 连接数
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT numbackends, (SELECT setting::int FROM pg_settings WHERE name='max_connections') AS maxconn FROM pg_stat_database WHERE datname=current_database()"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        result.put("connections", rs.getInt("numbackends"));
                        result.put("maxConnections", rs.getInt("maxconn"));
                    }
                    rs.close();
                }

                // 慢查询（pg_stat_statements扩展）, 运行时计数器, 用 committed queries per second 近似
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT xact_commit + xact_rollback AS total_tx FROM pg_stat_database WHERE datname = current_database()"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        long totalTx = rs.getLong("total_tx");
                        // 近似QPS用事务数代替
                        result.put("qps", BigDecimal.valueOf(totalTx));
                    }
                    rs.close();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT deadlocks FROM pg_stat_database WHERE datname = current_database()"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) extra.put("deadlocks", rs.getInt("deadlocks"));
                    rs.close();
                }

                // 慢查询列表（从pg_stat_statements）
                List<Map<String, Object>> slowSqlList = getPgSlowQueries(conn);
                result.put("slowQueries", slowSqlList.size());
                extra.put("slowSqlList", slowSqlList);

                // 缓存命中率：从pg_stat_bgwriter或pg_stat_io(PostgreSQL 16+)
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT CASE WHEN blks_hit + blks_read > 0 THEN round(100.0 * blks_hit / (blks_hit + blks_read), 2) ELSE 100 END " +
                    "FROM pg_stat_database WHERE datname = current_database()"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) result.put("cacheHitRatio", rs.getBigDecimal(1));
                    rs.close();
                }
                catch (Exception ignored) { result.put("cacheHitRatio", BigDecimal.valueOf(100)); }

                result.put("memoryUsedPct", BigDecimal.ZERO);

                // 锁等待数
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM pg_locks WHERE NOT granted"))
                {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) extra.put("lockWaits", rs.getInt(1));
                    rs.close();
                }
                catch (Exception ignored) { extra.put("lockWaits", -1); }

                // 长事务（超过60秒）
                List<Map<String, Object>> longTrx = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT pid, usename, application_name, state, " +
                    "EXTRACT(EPOCH FROM (now() - xact_start))::int AS duration_sec, query " +
                    "FROM pg_stat_activity WHERE state = 'active' AND xact_start IS NOT NULL " +
                    "AND EXTRACT(EPOCH FROM (now() - xact_start)) > 60 ORDER BY xact_start"))
                {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                    {
                        Map<String, Object> trx = new LinkedHashMap<>();
                        trx.put("pid", rs.getInt("pid"));
                        trx.put("user", rs.getString("usename"));
                        trx.put("app", rs.getString("application_name"));
                        trx.put("state", rs.getString("state"));
                        trx.put("durationSec", rs.getInt("duration_sec"));
                        trx.put("query", rs.getString("query"));
                        longTrx.add(trx);
                    }
                    rs.close();
                }
                catch (Exception ignored) {}
                extra.put("longTransactions", longTrx);
                extra.put("longTransactionCount", longTrx.size());

                // 表空间统计
                List<Map<String, Object>> tableSizes = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT relname AS table_name, n_live_tup AS row_count, " +
                    "pg_size_pretty(pg_total_relation_size(relid)) AS total_size, " +
                    "pg_total_relation_size(relid) AS total_bytes " +
                    "FROM pg_stat_user_tables ORDER BY pg_total_relation_size(relid) DESC LIMIT 20"))
                {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                    {
                        Map<String, Object> t = new LinkedHashMap<>();
                        t.put("tableName", rs.getString("table_name"));
                        t.put("tableRows", rs.getLong("row_count"));
                        t.put("totalSize", rs.getString("total_size"));
                        t.put("totalBytes", rs.getLong("total_bytes"));
                        tableSizes.add(t);
                    }
                    rs.close();
                }
                catch (Exception ignored) {}
                extra.put("tableSizes", tableSizes);

                // 异常连接数
                extra.put("abortedConnects", "N/A (pg_stat_database 不含此指标)");
            }
            catch (Exception e)
            {
                result.put("isAlive", false);
                extra.put("error", e.getMessage());
            }
            return result;
        }

        private Connection getPostgreSqlConnection(DbInstance instance) throws Exception
        {
            String url = "jdbc:postgresql://" + instance.getHost() + ":" + instance.getPort()
                + "/" + (StringUtils.isNotEmpty(instance.getDbName()) ? instance.getDbName() : "postgres")
                + "?connectTimeout=5&socketTimeout=300";
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, instance.getUsername(),
                StringUtils.isNotEmpty(instance.getPasswordRaw()) ? instance.getPasswordRaw() : instance.getPassword());
        }

        private List<Map<String, Object>> getPgSlowQueries(Connection conn)
        {
            List<Map<String, Object>> list = new ArrayList<>();
            // pg_stat_statements 需要超级用户启用扩展，容错处理
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT query, calls, mean_exec_time, total_exec_time, rows " +
                "FROM pg_stat_statements WHERE query NOT LIKE '%pg_stat%' ORDER BY mean_exec_time DESC LIMIT 20"))
            {
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sqlText", rs.getString("query"));
                    row.put("execCount", rs.getLong("calls"));
                    row.put("avgSec", rs.getBigDecimal("mean_exec_time").divide(new BigDecimal(1000), 2, java.math.RoundingMode.HALF_UP));
                    row.put("totalSec", rs.getBigDecimal("total_exec_time").divide(new BigDecimal(1000), 2, java.math.RoundingMode.HALF_UP));
                    row.put("rowsExamined", rs.getLong("rows"));
                    list.add(row);
                }
                rs.close();
            }
            catch (Exception ignored) {}
            return list;
        }
    }

    // ==================== Redis 采集器 ====================

    private static class RedisCollector
    {
        Map<String, Object> collect(DbInstance instance)
        {
            Map<String, Object> result = new LinkedHashMap<>();
            Map<String, Object> extra = new LinkedHashMap<>();
            result.put("extraJson", extra);

            String host = instance.getHost();
            int port = instance.getPort();
            String password = StringUtils.isNotEmpty(instance.getPasswordRaw())
                ? instance.getPasswordRaw() : instance.getPassword();

            try (Jedis jedis = new Jedis(host, port, 5000))
            {
                if (StringUtils.isNotEmpty(password))
                {
                    jedis.auth(password);
                }
                jedis.ping();
                result.put("isAlive", true);

                // INFO SERVER
                String serverInfo = jedis.info("server");
                Map<String, String> serverMap = parseRedisInfo(serverInfo);
                extra.put("version", serverMap.getOrDefault("redis_version", ""));
                extra.put("uptimeDays", String.valueOf(Long.parseLong(serverMap.getOrDefault("uptime_in_seconds", "0")) / 86400));

                // INFO CLIENTS
                String clientsInfo = jedis.info("clients");
                Map<String, String> clientsMap = parseRedisInfo(clientsInfo);
                result.put("connections", Integer.parseInt(clientsMap.getOrDefault("connected_clients", "0")));
                result.put("maxConnections", 0); // Redis无硬上限概念
                extra.put("blockedClients", Integer.parseInt(clientsMap.getOrDefault("blocked_clients", "0")));

                // INFO MEMORY
                String memInfo = jedis.info("memory");
                Map<String, String> memMap = parseRedisInfo(memInfo);
                long usedMemory = Long.parseLong(memMap.getOrDefault("used_memory", "0"));
                long maxMemory = Long.parseLong(memMap.getOrDefault("maxmemory", "0"));
                double memFragRatio = Double.parseDouble(memMap.getOrDefault("mem_fragmentation_ratio", "1.0"));
                if (maxMemory > 0)
                {
                    result.put("memoryUsedPct", new BigDecimal(100.0 * usedMemory / maxMemory).setScale(2, java.math.RoundingMode.HALF_UP));
                }
                else
                {
                    result.put("memoryUsedPct", BigDecimal.ZERO);
                }
                extra.put("usedMemoryMb", usedMemory / 1024 / 1024);
                extra.put("maxMemoryMb", maxMemory > 0 ? maxMemory / 1024 / 1024 : 0);
                extra.put("fragmentationRatio", memFragRatio);

                // INFO STATS
                String statsInfo = jedis.info("stats");
                Map<String, String> statsMap = parseRedisInfo(statsInfo);
                long hits = Long.parseLong(statsMap.getOrDefault("keyspace_hits", "0"));
                long misses = Long.parseLong(statsMap.getOrDefault("keyspace_misses", "0"));
                long total = hits + misses;
                if (total > 0)
                    result.put("cacheHitRatio", new BigDecimal(100.0 * hits / total).setScale(2, java.math.RoundingMode.HALF_UP));
                else
                    result.put("cacheHitRatio", BigDecimal.valueOf(100));
                result.put("qps", new BigDecimal(statsMap.getOrDefault("instantaneous_ops_per_sec", "0")));
                extra.put("expiredKeys", Long.parseLong(statsMap.getOrDefault("expired_keys", "0")));
                extra.put("evictedKeys", Long.parseLong(statsMap.getOrDefault("evicted_keys", "0")));

                result.put("slowQueries", 0); // Redis无慢查询概念

                // 热Key: 用 --hotkeys 需要 redis-cli，简化实现为每个DB的key数量
                Map<String, Long> dbKeys = new LinkedHashMap<>();
                for (int i = 0; i < 16; i++)
                {
                    try
                    {
                        jedis.select(i);
                        long size = jedis.dbSize();
                        if (size > 0) dbKeys.put("db" + i, size);
                    }
                    catch (Exception ignored) {}
                }
                extra.put("dbKeyCounts", dbKeys);

                // 大Key检测：采样方式扫描（仅检查当前DB中key数量较少时）
                List<Map<String, Object>> bigKeys = new ArrayList<>();
                extra.put("bigKeys", bigKeys);
                extra.put("bigKeyNote", "大Key精确检测需使用redis-cli --bigkeys命令");
            }
            catch (Exception e)
            {
                result.put("isAlive", false);
                extra.put("error", e.getMessage());
            }
            return result;
        }

        /**
         * 解析Redis INFO命令返回的key:value格式文本。
         */
        private Map<String, String> parseRedisInfo(String info)
        {
            Map<String, String> map = new LinkedHashMap<>();
            if (info == null) return map;
            for (String line : info.split("\n"))
            {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                int idx = line.indexOf(':');
                if (idx > 0)
                {
                    map.put(line.substring(0, idx), line.substring(idx + 1).trim());
                }
            }
            return map;
        }
    }
}
