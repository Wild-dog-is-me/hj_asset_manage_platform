import request from '@/utils/request'

export function listAssetInfo(query) {
  return request({
    url: '/manage/platform/asset/list',
    method: 'get',
    params: query
  })
}

export function getAssetInfo(assetId) {
  return request({
    url: '/manage/platform/asset/' + assetId,
    method: 'get'
  })
}

export function addAssetInfo(data) {
  return request({
    url: '/manage/platform/asset',
    method: 'post',
    data: data
  })
}

export function updateAssetInfo(data) {
  return request({
    url: '/manage/platform/asset',
    method: 'put',
    data: data
  })
}

export function delAssetInfo(assetId) {
  return request({
    url: '/manage/platform/asset/' + assetId,
    method: 'delete'
  })
}
