// 本文件定义 UpdateSiteAdminManagerProfilePlanner，负责 operations 模块的更新编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerSessionResponse } from '@/microservices/auth/objects/ManagerSessionResponse'

export const updateSiteAdminManagerProfile = (payload: {
  managerId: string
  displayName: string
  logoAssetPath?: string | null
}): Promise<ManagerSessionResponse> =>
  executeJsonApiRequest('/UpdateSiteAdminManagerProfilePlanner', 'POST', payload)
