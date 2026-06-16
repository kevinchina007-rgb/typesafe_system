import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { TrainAdminSessionPlannerResponse } from '@/microservices/operations/train/objects/TrainAdminSessionPlannerResponse'
import type { RegisterRailwayManagerPlannerRequest } from '@/microservices/operations/train/objects/RegisterRailwayManagerPlannerRequest'

export const registerRailwayManagerPlanner = (payload: RegisterRailwayManagerPlannerRequest): Promise<TrainAdminSessionPlannerResponse> =>
  executeJsonApiRequest('/RegisterRailwayManagerPlanner', 'POST', payload)

export const registerRailwayManager = registerRailwayManagerPlanner

