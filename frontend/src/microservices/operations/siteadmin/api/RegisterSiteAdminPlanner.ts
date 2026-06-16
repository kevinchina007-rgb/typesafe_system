// 本文件定义 RegisterSiteAdminPlanner，负责 operations 模块的注册编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { RegisterSiteAdminPlannerRequest } from '@/microservices/operations/siteadmin/objects/RegisterSiteAdminPlannerRequest'
import type { SiteAdminManagerSessionPlannerResponse } from '@/microservices/operations/siteadmin/objects/SiteAdminManagerSessionPlannerResponse'

export const registerSiteAdmin = (payload: RegisterSiteAdminPlannerRequest): Promise<SiteAdminManagerSessionPlannerResponse> =>
  executeJsonApiRequest<SiteAdminManagerSessionPlannerResponse>('/RegisterSiteAdminPlanner', 'POST', payload)
