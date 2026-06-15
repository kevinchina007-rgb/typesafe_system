// ExploreSearchPlanner：content 域探索搜索入口。

import type { ExploreSearchPlannerRequest } from '@/microservices/content/objects/ExploreSearchPlannerRequest'
import type { ExploreSearchListPlannerResponse } from '@/microservices/content/objects/ExploreSearchListPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const searchExplore = (payload: ExploreSearchPlannerRequest): Promise<ExploreSearchListPlannerResponse> =>
  executeJsonApiRequest('/ExploreSearchPlanner', 'POST', payload)
