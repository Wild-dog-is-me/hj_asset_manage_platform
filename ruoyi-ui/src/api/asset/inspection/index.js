import request from '@/utils/request'

export function listInspectionItem(query) {
  return request({
    url: '/manage/platform/inspection/item/list',
    method: 'get',
    params: query
  })
}

export function treeInspectionItem() {
  return request({
    url: '/manage/platform/inspection/item/tree',
    method: 'get'
  })
}

export function getInspectionItem(itemId) {
  return request({
    url: '/manage/platform/inspection/item/' + itemId,
    method: 'get'
  })
}

export function addInspectionItem(data) {
  return request({
    url: '/manage/platform/inspection/item',
    method: 'post',
    data: data
  })
}

export function updateInspectionItem(data) {
  return request({
    url: '/manage/platform/inspection/item',
    method: 'put',
    data: data
  })
}

export function delInspectionItem(itemId) {
  return request({
    url: '/manage/platform/inspection/item/' + itemId,
    method: 'delete'
  })
}

export function listInspectionRecord(query) {
  return request({
    url: '/manage/platform/inspection/record/list',
    method: 'get',
    params: query
  })
}

export function getInspectionRecord(recordId) {
  return request({
    url: '/manage/platform/inspection/record/' + recordId,
    method: 'get'
  })
}

export function buildInspectionRecordTemplate(data) {
  return request({
    url: '/manage/platform/inspection/record/template',
    method: 'post',
    data: data
  })
}

export function addInspectionRecord(data) {
  return request({
    url: '/manage/platform/inspection/record',
    method: 'post',
    data: data
  })
}

export function updateInspectionRecord(data) {
  return request({
    url: '/manage/platform/inspection/record',
    method: 'put',
    data: data
  })
}

export function delInspectionRecord(recordId) {
  return request({
    url: '/manage/platform/inspection/record/' + recordId,
    method: 'delete'
  })
}
