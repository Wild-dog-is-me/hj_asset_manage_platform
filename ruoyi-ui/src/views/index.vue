<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-total">
          <div class="stat-icon"><i class="el-icon-s-data" /></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.totalCount != null ? stats.totalCount : '-' }}</div>
            <div class="stat-label">资产总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-instock">
          <div class="stat-icon"><i class="el-icon-s-home" /></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.inStockCount != null ? stats.inStockCount : '-' }}</div>
            <div class="stat-label">库存中</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-inuse">
          <div class="stat-icon"><i class="el-icon-s-marketing" /></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.inUseCount != null ? stats.inUseCount : '-' }}</div>
            <div class="stat-label">使用中</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-scrapped">
          <div class="stat-icon"><i class="el-icon-delete-solid" /></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.scrappedCount != null ? stats.scrappedCount : '-' }}</div>
            <div class="stat-label">已报废</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="chart-header">
            <span>资产状态分布</span>
          </div>
          <div ref="statusChart" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="chart-header">
            <span>资产类别分布</span>
          </div>
          <div ref="categoryChart" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import 'echarts/theme/macarons'
import { getAssetStatistics } from '@/api/asset/manage/info'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {},
      statusChart: null,
      categoryChart: null
    }
  },
  mounted() {
    this.fetchData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    this.disposeCharts()
  },
  methods: {
    fetchData() {
      getAssetStatistics().then(response => {
        this.stats = response.data || {}
        this.$nextTick(() => {
          this.initStatusChart()
          this.initCategoryChart()
        })
      })
    },
    initStatusChart() {
      this.disposeChart('statusChart')
      const el = this.$refs.statusChart
      if (!el) return
      this.statusChart = echarts.init(el, 'macarons')
      const data = (this.stats.statusPieData || []).map(item => ({
        name: item.name,
        value: item.value
      }))
      this.statusChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        series: [{
          type: 'pie',
          radius: ['45%', '72%'],
          center: ['50%', '50%'],
          label: { show: true, formatter: '{b}\n{d}%' },
          emphasis: { label: { fontSize: 16, fontWeight: 'bold' } },
          data: data
        }]
      })
    },
    initCategoryChart() {
      this.disposeChart('categoryChart')
      const el = this.$refs.categoryChart
      if (!el) return
      this.categoryChart = echarts.init(el, 'macarons')
      const data = (this.stats.categoryPieData || []).map(item => ({
        name: item.name,
        value: item.value
      }))
      this.categoryChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        series: [{
          type: 'pie',
          radius: ['45%', '72%'],
          center: ['50%', '50%'],
          label: { show: true, formatter: '{b}\n{d}%' },
          emphasis: { label: { fontSize: 16, fontWeight: 'bold' } },
          data: data
        }]
      })
    },
    handleResize() {
      if (this.statusChart) this.statusChart.resize()
      if (this.categoryChart) this.categoryChart.resize()
    },
    disposeChart(key) {
      const chart = this[key]
      if (chart) {
        chart.dispose()
        this[key] = null
      }
    },
    disposeCharts() {
      this.disposeChart('statusChart')
      this.disposeChart('categoryChart')
    }
  }
}
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 4px;
}

.stat-cards {
  margin-bottom: 20px;

  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px 18px;
    border-radius: 8px;
    color: #fff;
    margin-bottom: 16px;
  }
  .stat-total { background: linear-gradient(135deg, #667eea, #764ba2); }
  .stat-instock { background: linear-gradient(135deg, #43e97b, #38f9d7); color: #303133; }
  .stat-inuse { background: linear-gradient(135deg, #fa709a, #fee140); color: #303133; }
  .stat-scrapped { background: linear-gradient(135deg, #a18cd1, #fbc2eb); }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: rgba(255,255,255,.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    margin-right: 16px;
    flex-shrink: 0;
  }
  .stat-instock .stat-icon,
  .stat-inuse .stat-icon {
    background: rgba(0,0,0,.08);
  }

  .stat-body {
    flex: 1;
  }
  .stat-value {
    font-size: 28px;
    font-weight: 700;
    line-height: 1.2;
  }
  .stat-label {
    font-size: 13px;
    opacity: .85;
    margin-top: 2px;
  }
}

.chart-row {
  .chart-card {
    margin-bottom: 20px;

    .chart-header {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }
    .chart-box {
      width: 100%;
      height: 340px;
    }
  }
}
</style>
