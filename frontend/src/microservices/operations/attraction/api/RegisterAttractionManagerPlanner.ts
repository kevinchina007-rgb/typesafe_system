// 鏈枃浠跺畾涔?RegisterAttractionManagerPlanner锛岃礋璐?operations 妯″潡鐨勬敞鍐岀紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { RegisterAttractionManagerPlannerRequest } from '@/microservices/operations/attraction/objects/RegisterAttractionManagerPlannerRequest'
import type { AttractionManagerSessionPlannerResponse } from '@/microservices/operations/attraction/objects/AttractionManagerSessionPlannerResponse'

export const registerAttractionManager = (payload: RegisterAttractionManagerPlannerRequest): Promise<AttractionManagerSessionPlannerResponse> =>
  executeJsonApiRequest<AttractionManagerSessionPlannerResponse>('/RegisterAttractionManagerPlanner', 'POST', payload)

