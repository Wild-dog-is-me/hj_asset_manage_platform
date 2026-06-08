import request from '@/utils/request'

// ==================== 数据库实例配置 ====================

/** 查询实例列表 */
export function listDbInstance(query) {
  return request({ url: '/manage/platform/db/instance/list', method: 'get', params: query })
}

/** 查询实例详情 */
export function getDbInstance(instanceId) {
  return request({ url: '/manage/platform/db/instance/' + instanceId, method: 'get' })
}

/** 新增实例 */
export function addDbInstance(data) {
  return request({ url: '/manage/platform/db/instance', method: 'post', data: data })
}

/** 修改实例 */
export function updateDbInstance(data) {
  return request({ url: '/manage/platform/db/instance', method: 'put', data: data })
}

/** 删除实例 */
export function delDbInstance(instanceId) {
  return request({ url: '/manage/platform/db/instance/' + instanceId, method: 'delete' })
}

/** 测试已有实例连接 */
export function testDbConnection(instanceId) {
  return request({ url: '/manage/platform/db/instance/test/' + instanceId, method: 'post' })
}

/** 测试表单中的连接（不先保存） */
export function testDbFormConnection(data) {
  return request({ url: '/manage/platform/db/instance/testForm', method: 'post', data: data })
}

// ==================== 数据库监控指标 ====================

/** 采集指定实例的实时指标 */
export function collectMetrics(instanceId) {
  return request({ url: '/manage/platform/db/monitor/collect/' + instanceId, method: 'get' })
}

/** 采集并保存快照 */
export function snapshotMetrics(instanceId) {
  return request({ url: '/manage/platform/db/monitor/snapshot/' + instanceId, method: 'get' })
}

/** 查询历史快照趋势数据 */
export function getSnapshotHistory(instanceId, beginTime, endTime) {
  return request({ url: '/manage/platform/db/monitor/history', method: 'get', params: { instanceId, beginTime, endTime } })
}

/** 查询慢SQL列表 */
export function getSlowSqlList(instanceId) {
  return request({ url: '/manage/platform/db/monitor/slowSql/' + instanceId, method: 'get' })
}

/** 健康检查（所有启用实例） */
export function getHealthCheck() {
  return request({ url: '/manage/platform/db/monitor/health', method: 'get' })
}
