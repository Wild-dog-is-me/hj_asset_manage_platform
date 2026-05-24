import request from '@/utils/request'

export function listIssueInfo(query) {
  return request({
    url: '/manage/platform/issue/list',
    method: 'get',
    params: query
  })
}

export function getIssueInfo(issueId) {
  return request({
    url: '/manage/platform/issue/' + issueId,
    method: 'get'
  })
}

export function addIssueInfo(data) {
  return request({
    url: '/manage/platform/issue',
    method: 'post',
    data: data
  })
}

export function updateIssueInfo(data) {
  return request({
    url: '/manage/platform/issue',
    method: 'put',
    data: data
  })
}

export function delIssueInfo(issueId) {
  return request({
    url: '/manage/platform/issue/' + issueId,
    method: 'delete'
  })
}
