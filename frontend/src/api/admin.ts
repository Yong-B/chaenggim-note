import { request } from './client'

/** 전체 삭제 후 가상 데이터로 다시 채운다. */
export const resetData = () => request<null>('/admin/reset', { method: 'POST' })
