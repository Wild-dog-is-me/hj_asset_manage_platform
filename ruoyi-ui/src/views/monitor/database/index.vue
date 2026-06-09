<template>
  <div class="app-container db-monitor-dashboard">
    <!-- 顶部工具栏 -->
    <el-card shadow="never" class="toolbar-card">
      <el-row :gutter="16" type="flex" align="middle">
        <el-col>
          <span class="toolbar-label">选择实例：</span>
          <el-select v-model="selectedInstanceIds" multiple collapse-tags placeholder="请选择数据库实例" clearable @change="handleInstanceChange" style="width: 460px">
            <el-option v-for="item in instanceList" :key="item.instanceId" :label="item.instanceName + ' (' + item.dbType + ')'" :value="item.instanceId" />
          </el-select>
        </el-col>
        <el-col>
          <span class="toolbar-label">自动刷新：</span>
          <el-switch v-model="autoRefresh" active-text="5s" inactive-text="关" @change="toggleAutoRefresh" />
        </el-col>
        <el-col>
          <span v-if="lastRefreshTime" class="toolbar-sub">上次刷新：{{ lastRefreshTime }}</span>
        </el-col>
        <el-col style="margin-left: auto;">
          <el-button type="primary" icon="el-icon-refresh" size="mini" @click="refreshAll">立即刷新</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 指标卡片行 -->
    <el-row :gutter="16" class="metric-cards" v-if="selectedInstanceIds.length > 0">
      <el-col :xs="12" :sm="6">
        <div class="metric-card metric-connections">
          <div class="metric-icon"><i class="el-icon-connection" /></div>
          <div class="metric-body">
            <div class="metric-value">{{ summary.connections }}<span class="metric-unit" v-if="summary.maxConnections">/{{ summary.maxConnections }}</span></div>
            <div class="metric-label">连接数</div>
            <div class="metric-bar"><div class="metric-bar-inner" :style="{ width: summary.connPct + '%' }" :class="barClass(summary.connPct)" /></div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="metric-card metric-slow">
          <div class="metric-icon"><i class="el-icon-warning-outline" /></div>
          <div class="metric-body">
            <div class="metric-value">{{ summary.slowQueries }}</div>
            <div class="metric-label">慢查询</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="metric-card metric-qps">
          <div class="metric-icon"><i class="el-icon-s-data" /></div>
          <div class="metric-body">
            <div class="metric-value">{{ summary.qps }}</div>
            <div class="metric-label">QPS</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="metric-card metric-cache">
          <div class="metric-icon"><i class="el-icon-s-marketing" /></div>
          <div class="metric-body">
            <div class="metric-value">{{ summary.cacheHitRatio }}<span class="metric-unit">%</span></div>
            <div class="metric-label">缓存命中率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-empty v-if="selectedInstanceIds.length === 0" description="请选择要监控的数据库实例" style="margin-top: 40px" />

    <template v-if="selectedInstanceIds.length > 0">
      <!-- 趋势图 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="chart-header"><span>连接数趋势</span></div>
            <div ref="connChart" class="chart-box" />
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="chart-header"><span>QPS 趋势</span></div>
            <div ref="qpsChart" class="chart-box" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 指标采集提示 -->
      <el-alert
        v-if="metricWarnings.length > 0"
        :title="metricWarnings.join('；')"
        type="warning"
        show-icon
        :closable="false"
        class="metric-warning"
      />

      <!-- 慢SQL列表 -->
      <el-card shadow="never" class="table-card">
        <div slot="header" class="chart-header"><span>慢SQL列表 TOP20</span></div>
        <el-table :data="slowSqlData" v-loading="slowSqlLoading" size="small" max-height="400">
          <el-table-column label="执行次数" prop="execCount" width="90" align="center" />
          <el-table-column label="平均耗时(秒)" prop="avgSec" width="120" align="center">
            <template slot-scope="scope">
              <span :style="{ color: Number(scope.row.avgSec || 0) > 5 ? '#f56c6c' : Number(scope.row.avgSec || 0) > 1 ? '#e6a23c' : '' }">{{ formatSeconds(scope.row.avgSec) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="总耗时(秒)" prop="totalSec" width="120" align="center">
            <template slot-scope="scope">{{ formatSeconds(scope.row.totalSec) }}</template>
          </el-table-column>
          <el-table-column label="扫描行数" prop="rowsExamined" width="100" align="center" />
          <el-table-column label="SQL 摘要" prop="sqlText" min-width="300">
            <template slot-scope="scope">
              <span>{{ formatSqlSummary(scope.row.sqlText) }}</span>
              <el-button type="text" size="mini" class="sql-detail-btn" @click="showFullSql(scope.row.sqlText)">查看完整SQL</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 健康一览表 -->
      <el-card shadow="never" class="table-card">
        <div slot="header" class="chart-header"><span>实例健康一览</span></div>
        <el-table :data="healthTableData" size="small">
          <el-table-column label="实例名称" prop="instanceName" min-width="140" />
          <el-table-column label="类型" prop="dbType" width="100" align="center">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.db_monitor_db_type" :value="scope.row.dbType" />
            </template>
          </el-table-column>
          <el-table-column label="连通性" width="80" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.isAlive ? 'success' : 'danger'" size="mini">{{ scope.row.isAlive ? '正常' : '不可达' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="连接数/上限" width="110" align="center" prop="connStr" />
          <el-table-column label="慢查询" width="80" align="center" prop="slowQueries" />
          <el-table-column label="命中率" width="90" align="center">
            <template slot-scope="scope">
              <span :style="{ color: (scope.row.cacheHitRatio || 0) < 90 ? '#f56c6c' : '' }">{{ scope.row.cacheHitRatio }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="综合健康" width="120" align="center">
            <template slot-scope="scope">
              <el-tag :type="healthTagType(scope.row)" size="mini">{{ healthStatusText(scope.row) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 扩展指标面板（按数据库类型展示差异化内容） -->
      <el-card shadow="never" class="table-card" v-if="hasTypeSpecificData">
        <div slot="header" class="chart-header"><span>扩展指标详情</span></div>
        <el-collapse accordion>
          <el-collapse-item v-if="showMysqlExtra" title="MySQL - 长事务 / 表空间 / 死锁" name="mysql">
            <el-descriptions :column="2" border size="small" v-if="mysqlExtra">
              <el-descriptions-item label="当前长事务数">{{ mysqlExtra.longTransactionCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="死锁次数">{{ mysqlExtra.deadlocks || 0 }}</el-descriptions-item>
              <el-descriptions-item label="异常断开连接">{{ mysqlExtra.abortedConnects || 0 }}</el-descriptions-item>
              <el-descriptions-item label="锁等待数">{{ mysqlExtra.lockWaits >= 0 ? mysqlExtra.lockWaits : 'N/A' }}</el-descriptions-item>
            </el-descriptions>
            <el-table :data="mysqlExtra.tableSizes || []" size="small" max-height="300" style="margin-top: 12px" v-if="mysqlExtra">
              <el-table-column label="表名" prop="tableName" min-width="160" />
              <el-table-column label="行数" prop="tableRows" width="100" align="right" />
              <el-table-column label="数据(MB)" prop="dataMb" width="100" align="right" />
              <el-table-column label="索引(MB)" prop="indexMb" width="100" align="right" />
              <el-table-column label="总计(MB)" prop="totalMb" width="100" align="right" />
            </el-table>
          </el-collapse-item>
          <el-collapse-item v-if="showSqlserverExtra" title="SQL Server - 等待统计 / 阻塞 / 数据库大小" name="sqlserver">
            <template v-if="sqlserverExtra">
              <div class="sub-section-title">等待统计 TOP10</div>
              <el-table :data="sqlserverExtra.waitStats || []" size="small" max-height="300">
                <el-table-column label="等待类型" prop="waitType" min-width="200" />
                <el-table-column label="等待时间(ms)" prop="waitTimeMs" width="130" align="right" />
                <el-table-column label="等待次数" prop="waitCount" width="100" align="right" />
              </el-table>
              <div class="sub-section-title" style="margin-top: 12px">数据库文件大小</div>
              <el-table :data="sqlserverExtra.dbSizes || []" size="small" max-height="200">
                <el-table-column label="数据库" prop="dbName" min-width="160" />
                <el-table-column label="大小(MB)" prop="sizeMb" width="100" align="right" />
              </el-table>
            </template>
          </el-collapse-item>
          <el-collapse-item v-if="showRedisExtra" title="Redis - 内存 / Key 分布 / 碎片率" name="redis">
            <el-descriptions :column="3" border size="small" v-if="redisExtra">
              <el-descriptions-item label="已用内存(MB)">{{ redisExtra.usedMemoryMb }}</el-descriptions-item>
              <el-descriptions-item label="最大内存(MB)">{{ redisExtra.maxMemoryMb || '无限制' }}</el-descriptions-item>
              <el-descriptions-item label="内存碎片率">
                <span :style="{ color: (redisExtra.fragmentationRatio || 1) > 1.5 ? '#f56c6c' : '' }">{{ redisExtra.fragmentationRatio }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="过期Key数">{{ redisExtra.expiredKeys }}</el-descriptions-item>
              <el-descriptions-item label="淘汰Key数">{{ redisExtra.evictedKeys }}</el-descriptions-item>
              <el-descriptions-item label="阻塞客户端">{{ redisExtra.blockedClients || 0 }}</el-descriptions-item>
            </el-descriptions>
            <el-table :data="redisDbKeyList" size="small" max-height="200" style="margin-top: 12px" v-if="redisDbKeyList.length > 0">
              <el-table-column label="数据库" prop="db" width="100" />
              <el-table-column label="Key数量" prop="count" />
            </el-table>
          </el-collapse-item>
        </el-collapse>
      </el-card>
      <!-- 完整SQL查看 -->
      <el-dialog title="完整 SQL" :visible.sync="sqlDialogOpen" width="70%" append-to-body>
        <el-input
          :value="currentSqlText"
          type="textarea"
          :rows="16"
          readonly
          class="sql-full-text"
        />
      </el-dialog>
    </template>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import 'echarts/theme/macarons'
import { listDbInstance, collectMetrics } from '@/api/monitor/database'

export default {
  name: 'DbMonitorDashboard',
  dicts: ['db_monitor_db_type'],
  data() {
    return {
      instanceList: [],
      selectedInstanceIds: [],
      autoRefresh: true,
      refreshTimer: null,
      refreshing: false,
      lastRefreshTime: '',
      metricsCache: {},
      slowSqlData: [],
      slowSqlLoading: false,
      sqlDialogOpen: false,
      currentSqlText: '',
      connChart: null,
      qpsChart: null,
      historyData: {}
    }
  },
  computed: {
    summary() {
      const metrics = Object.values(this.metricsCache)
      let connections = 0, maxConnections = 0, slowQueries = 0, qps = 0, cacheHitRatio = 0
      metrics.forEach(m => {
        connections += (m.connections || 0)
        maxConnections += (m.maxConnections || 0)
        slowQueries += (m.slowQueries || 0)
        qps = Math.max(qps, +(m.qps || 0))
        cacheHitRatio = Math.max(cacheHitRatio, +(m.cacheHitRatio || 0))
      })
      if (metrics.length > 1) {
        qps = +(metrics.reduce((s, m) => s + +(m.qps || 0), 0) / metrics.length).toFixed(1)
        cacheHitRatio = +(metrics.reduce((s, m) => s + +(m.cacheHitRatio || 0), 0) / metrics.length).toFixed(1)
      }
      const connPct = maxConnections > 0 ? Math.round((connections / maxConnections) * 100) : 0
      return { connections, maxConnections, connPct, slowQueries, qps, cacheHitRatio }
    },
    healthTableData() {
      return this.selectedInstanceIds.map(id => {
        const inst = this.instanceList.find(i => i.instanceId === id) || {}
        const m = this.metricsCache[id]
        return {
          instanceId: id,
          instanceName: inst.instanceName || '',
          dbType: inst.dbType || '',
          isAlive: m ? m.isAlive : false,
          connections: m ? m.connections : '-',
          maxConnections: m ? m.maxConnections : '-',
          connStr: m ? (m.connections || 0) + '/' + (m.maxConnections || '-') : '-/-',
          slowQueries: m ? m.slowQueries : '-',
          cacheHitRatio: m ? (m.cacheHitRatio || 0) : 0
        }
      })
    },
    metricWarnings() {
      const warnings = []
      this.selectedInstanceIds.forEach(id => {
        const inst = this.instanceList.find(i => i.instanceId === id) || {}
        const extra = (this.metricsCache[id] && this.metricsCache[id].extraJson) || {}
        if (extra.error) warnings.push((inst.instanceName || id) + ' 连接异常：' + extra.error)
        if (extra.slowSqlError) warnings.push((inst.instanceName || id) + ' 慢SQL采集失败：' + extra.slowSqlError)
        if (extra.qpsError) warnings.push((inst.instanceName || id) + ' QPS采集失败：' + extra.qpsError)
        if (extra.cacheHitRatioError) warnings.push((inst.instanceName || id) + ' 缓存命中率采集失败：' + extra.cacheHitRatioError)
      })
      return warnings
    },
    hasTypeSpecificData() {
      return this.showMysqlExtra || this.showSqlserverExtra || this.showRedisExtra
    },
    showMysqlExtra() {
      return this.selectedInstanceIds.some(id => {
        const inst = this.instanceList.find(i => i.instanceId === id)
        return inst && inst.dbType === 'MYSQL'
      })
    },
    showSqlserverExtra() {
      return this.selectedInstanceIds.some(id => {
        const inst = this.instanceList.find(i => i.instanceId === id)
        return inst && inst.dbType === 'SQLSERVER'
      })
    },
    showRedisExtra() {
      return this.selectedInstanceIds.some(id => {
        const inst = this.instanceList.find(i => i.instanceId === id)
        return inst && inst.dbType === 'REDIS'
      })
    },
    mysqlExtra() {
      for (const id of this.selectedInstanceIds) {
        const m = this.metricsCache[id]
        if (m && m.extraJson) return m.extraJson
      }
      return null
    },
    sqlserverExtra() {
      for (const id of this.selectedInstanceIds) {
        const m = this.metricsCache[id]
        if (m && m.extraJson) return m.extraJson
      }
      return null
    },
    redisExtra() {
      for (const id of this.selectedInstanceIds) {
        const m = this.metricsCache[id]
        if (m && m.extraJson) return m.extraJson
      }
      return null
    },
    redisDbKeyList() {
      const extra = this.redisExtra
      if (!extra || !extra.dbKeyCounts) return []
      return Object.entries(extra.dbKeyCounts).map(([db, count]) => ({ db, count }))
    }
  },
  created() {
    this.loadInstances()
    this.startAutoRefresh()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    this.stopAutoRefresh()
    window.removeEventListener('resize', this.handleResize)
    this.disposeCharts()
  },
  methods: {
    /** 加载所有启用的实例 */
    loadInstances() {
      listDbInstance({ status: 'ENABLED' }).then(res => {
        this.instanceList = res.rows || []
      })
    },
    /** 实例选择变化 */
    handleInstanceChange() {
      // 清理未选中实例的缓存
      const newCache = {}
      this.selectedInstanceIds.forEach(id => {
        if (this.metricsCache[id]) newCache[id] = this.metricsCache[id]
      })
      this.metricsCache = newCache
      this.refreshAll()
    },
    /** 刷新所有选中实例 */
    async refreshAll() {
      if (this.selectedInstanceIds.length === 0 || this.refreshing) return
      this.refreshing = true
      const now = new Date()
      this.lastRefreshTime = now.toLocaleTimeString()
      try {
        const promises = this.selectedInstanceIds.map(id =>
          collectMetrics(id).then(res => ({ id, data: res.data })).catch(error => ({ id, data: null, error }))
        )
        const results = await Promise.all(promises)
        results.forEach(({ id, data, error }) => {
          if (data) {
            this.metricsCache[id] = data
            // 收集慢SQL
            if (data.extraJson && data.extraJson.slowSqlList) {
              this.slowSqlData = data.extraJson.slowSqlList
            }
          } else {
            this.metricsCache[id] = {
              isAlive: false,
              connections: 0,
              maxConnections: 0,
              slowQueries: 0,
              qps: 0,
              cacheHitRatio: 0,
              extraJson: { error: (error && (error.msg || error.message)) || '采集接口请求失败' }
            }
          }
        })
        this.metricsCache = { ...this.metricsCache }
        // 添加到历史数据用于趋势图
        const ts = now.toISOString()
        if (!this.historyData['_all']) this.historyData['_all'] = []
        this.historyData['_all'].push({
          time: ts,
          connections: this.summary.connections,
          qps: this.summary.qps
        })
        if (this.historyData['_all'].length > 60) {
          this.historyData['_all'] = this.historyData['_all'].slice(-60)
        }
        this.$nextTick(() => {
          this.initConnChart()
          this.initQpsChart()
        })
      } finally {
        this.slowSqlLoading = false
        this.refreshing = false
      }
    },
    /** 切换自动刷新 */
    toggleAutoRefresh(val) {
      if (val) this.startAutoRefresh()
      else this.stopAutoRefresh()
    },
    startAutoRefresh() {
      this.stopAutoRefresh()
      if (this.autoRefresh) {
        this.refreshTimer = setInterval(() => this.refreshAll(), 5000)
      }
    },
    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },
    /** 连接数趋势图 */
    initConnChart() {
      this.disposeChart('connChart')
      const el = this.$refs.connChart
      if (!el) return
      this.connChart = echarts.init(el, 'macarons')
      const data = (this.historyData['_all'] || []).map(d => [d.time, d.connections])
      this.connChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'time', axisLabel: { formatter: '{HH}:{mm}:{ss}' } },
        yAxis: { type: 'value', name: '连接数' },
        series: [{ type: 'line', data, smooth: true, areaStyle: { opacity: 0.15 }, lineStyle: { color: '#409eff' }, itemStyle: { color: '#409eff' } }]
      })
    },
    /** QPS趋势图 */
    initQpsChart() {
      this.disposeChart('qpsChart')
      const el = this.$refs.qpsChart
      if (!el) return
      this.qpsChart = echarts.init(el, 'macarons')
      const data = (this.historyData['_all'] || []).map(d => [d.time, d.qps])
      this.qpsChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'time', axisLabel: { formatter: '{HH}:{mm}:{ss}' } },
        yAxis: { type: 'value', name: 'QPS' },
        series: [{ type: 'line', data, smooth: true, areaStyle: { opacity: 0.15 }, lineStyle: { color: '#67c23a' }, itemStyle: { color: '#67c23a' } }]
      })
    },
    disposeChart(key) {
      const chart = this[key]
      if (chart) { chart.dispose(); this[key] = null }
    },
    disposeCharts() {
      this.disposeChart('connChart')
      this.disposeChart('qpsChart')
    },
    handleResize() {
      if (this.connChart) this.connChart.resize()
      if (this.qpsChart) this.qpsChart.resize()
    },
    barClass(pct) {
      if (pct > 80) return 'bar-danger'
      if (pct > 60) return 'bar-warning'
      return 'bar-normal'
    },
    formatSeconds(value) {
      const num = Number(value || 0)
      return Number.isFinite(num) ? num.toFixed(2) : '0.00'
    },
    formatSqlSummary(sqlText) {
      const normalized = (sqlText || '').replace(/\s+/g, ' ').trim()
      return normalized.length > 120 ? normalized.substring(0, 120) + '...' : normalized
    },
    showFullSql(sqlText) {
      this.currentSqlText = sqlText || ''
      this.sqlDialogOpen = true
    },
    healthTagType(row) {
      if (!row.isAlive) return 'danger'
      if ((row.cacheHitRatio || 0) < 85) return 'warning'
      return 'success'
    },
    healthStatusText(row) {
      if (!row.isAlive) return '不可达'
      if ((row.cacheHitRatio || 0) < 85) return '警告'
      return '健康'
    }
  }
}
</script>

<style scoped lang="scss">
.db-monitor-dashboard {
  padding: 4px;

  .toolbar-card {
    margin-bottom: 16px;
    .toolbar-label { font-size: 14px; color: #606266; margin-right: 6px; }
    .toolbar-sub { font-size: 12px; color: #c0c4cc; }
  }

  .metric-cards {
    margin-bottom: 16px;
    .metric-card {
      display: flex;
      align-items: center;
      padding: 18px 16px;
      border-radius: 8px;
      color: #fff;
      margin-bottom: 12px;
      .metric-icon {
        width: 44px; height: 44px; border-radius: 10px;
        background: rgba(255,255,255,.2); display: flex;
        align-items: center; justify-content: center; font-size: 22px; margin-right: 14px; flex-shrink: 0;
      }
      .metric-body { flex: 1; }
      .metric-value { font-size: 26px; font-weight: 700; line-height: 1.2; }
      .metric-unit { font-size: 14px; opacity: .75; }
      .metric-label { font-size: 12px; opacity: .85; margin-top: 2px; }
      .metric-bar {
        height: 4px; background: rgba(255,255,255,.25); border-radius: 2px; margin-top: 6px;
        .metric-bar-inner { height: 100%; border-radius: 2px; transition: width .3s; }
        .bar-normal { background: #67c23a; }
        .bar-warning { background: #e6a23c; }
        .bar-danger { background: #f56c6c; }
      }
    }
    .metric-connections { background: linear-gradient(135deg, #667eea, #764ba2); }
    .metric-slow { background: linear-gradient(135deg, #f093fb, #f5576c); }
    .metric-qps { background: linear-gradient(135deg, #4facfe, #00f2fe); }
    .metric-cache { background: linear-gradient(135deg, #43e97b, #38f9d7); color: #303133;
      .metric-icon { background: rgba(0,0,0,.08); }
      .metric-bar { background: rgba(0,0,0,.15); }
    }
  }

  .chart-row {
    margin-bottom: 16px;
    .chart-card .chart-box { width: 100%; height: 300px; }
    .chart-header { font-size: 15px; font-weight: 600; color: #303133; }
  }

  .metric-warning {
    margin-bottom: 16px;
  }

  .sql-detail-btn {
    margin-left: 8px;
  }

  .sql-full-text ::v-deep textarea {
    font-family: Menlo, Monaco, Consolas, "Courier New", monospace;
    line-height: 1.5;
  }

  .table-card {
    margin-bottom: 16px;
    .chart-header { font-size: 15px; font-weight: 600; color: #303133; }
  }

  .sub-section-title {
    font-size: 13px; font-weight: 600; color: #606266; margin-bottom: 8px;
  }
}
</style>
