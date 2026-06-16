// 鏈枃浠跺畾涔?UpdateAirlineManagerProfilePlanner锛岃礋璐?operations 妯″潡鐨勬洿鏂扮紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { UpdateAirlineManagerProfilePlannerRequest } from '@/microservices/operations/airline/objects/UpdateAirlineManagerProfilePlannerRequest'
import type { AirlineManagerSessionPlannerResponse } from '@/microservices/operations/airline/objects/AirlineManagerSessionPlannerResponse'

export const updateAirlineManagerProfile = (payload: UpdateAirlineManagerProfilePlannerRequest): Promise<AirlineManagerSessionPlannerResponse> =>
  executeJsonApiRequest<AirlineManagerSessionPlannerResponse>('/UpdateAirlineManagerProfilePlanner', 'POST', payload)

