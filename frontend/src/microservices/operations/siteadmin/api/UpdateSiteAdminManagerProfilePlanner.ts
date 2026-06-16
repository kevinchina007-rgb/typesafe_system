// 本文件定义 UpdateSiteAdminManagerProfilePlanner，负责 operations 模块的更新编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { UpdateSiteAdminManagerProfilePlannerRequest } from '@/microservices/operations/siteadmin/objects/UpdateSiteAdminManagerProfilePlannerRequest'
import type { SiteAdminManagerSessionPlannerResponse } from '@/microservices/operations/siteadmin/objects/SiteAdminManagerSessionPlannerResponse'

export const updateSiteAdminManagerProfile = (payload: UpdateSiteAdminManagerProfilePlannerRequest): Promise<SiteAdminManagerSessionPlannerResponse> =>
  executeJsonApiRequest<SiteAdminManagerSessionPlannerResponse>('/UpdateSiteAdminManagerProfilePlanner', 'POST', payload)
