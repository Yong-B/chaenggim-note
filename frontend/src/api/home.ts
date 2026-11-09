import { request } from './client'
import type { HomeResponse } from './types'

export const getHome = () => request<HomeResponse>('/home')
