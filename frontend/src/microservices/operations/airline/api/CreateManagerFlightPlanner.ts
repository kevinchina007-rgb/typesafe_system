// 鏈枃浠跺畾涔?CreateManagerFlightPlanner锛岃礋璐?operations 妯″潡鐨勫垱寤虹紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { CreateManagerFlightPlannerRequest } from '@/microservices/operations/airline/objects/CreateManagerFlightPlannerRequest'
import type { ManagerFlightPlannerResponse } from '@/microservices/operations/airline/objects/ManagerFlightPlannerResponse'

export const createManagerFlight = (payload: CreateManagerFlightPlannerRequest): Promise<ManagerFlightPlannerResponse> =>
  executeJsonApiRequest<ManagerFlightPlannerResponse>('/CreateManagerFlightPlanner', 'POST', payload)

