// 本文件定义 CreateAttractionPlanner，负责 operations 模块的创建编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const createAttraction = (payload: {
  managerId: string
  attractionName: string
  city: string
  location: string
  description: string
  imageUrl?: string | null
}): Promise<unknown> =>
  executeJsonApiRequest('/CreateAttractionPlanner', 'POST', payload)
