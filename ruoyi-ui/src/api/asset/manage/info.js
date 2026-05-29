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

export function receiveAsset(data) {
  return request({
    url: '/manage/platform/asset/receive',
    method: 'post',
    data: data
  })
}

export function returnAsset(data) {
  return request({
    url: '/manage/platform/asset/return',
    method: 'post',
    data: data
  })
}

export function transferAsset(data) {
  return request({
    url: '/manage/platform/asset/transfer',
    method: 'post',
    data: data
  })
}

export function scrapAsset(data) {
  return request({
    url: '/manage/platform/asset/scrap',
    method: 'post',
    data: data
  })
}

export function listTransferRecord(assetId) {
  return request({
    url: '/manage/platform/asset/transfer/list',
    method: 'get',
    params: { assetId: assetId }
  })
}
