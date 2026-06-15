import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { TrainAdminSessionPlannerResponse } from '@/microservices/train/objects/TrainAdminSessionPlannerResponse'
import type { RegisterRailwayManagerPlannerRequest } from '@/microservices/train/objects/RegisterRailwayManagerPlannerRequest'

export const registerRailwayManagerPlanner = (payload: RegisterRailwayManagerPlannerRequest): Promise<TrainAdminSessionPlannerResponse> =>
  executeJsonApiRequest('/RegisterRailwayManagerPlanner', 'POST', payload)

export const registerRailwayManager = registerRailwayManagerPlanner
