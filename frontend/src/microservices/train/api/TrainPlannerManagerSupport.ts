import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { TrainAdminSessionPlannerResponse } from '@/microservices/train/objects/TrainAdminSessionPlannerResponse'
import type { TrainListPlannerResponse } from '@/microservices/train/objects/TrainListPlannerResponse'
import type { CreateTrainJourneyPlannerRequest } from '@/microservices/train/objects/CreateTrainJourneyPlannerRequest'
import type { ListManagedTrainsPlannerRequest } from '@/microservices/train/objects/ListManagedTrainsPlannerRequest'
import type { RegisterRailwayManagerPlannerRequest } from '@/microservices/train/objects/RegisterRailwayManagerPlannerRequest'
import type { TrainPlannerResponse } from '@/microservices/train/objects/TrainPlannerResponse'

export const createTrainJourneyPlanner = (payload: CreateTrainJourneyPlannerRequest): Promise<TrainPlannerResponse> =>
  executeJsonApiRequest('/CreateTrainJourneyPlanner', 'POST', payload)

export const createTrainJourney = createTrainJourneyPlanner

export const listManagedTrainsPlanner = (payload: ListManagedTrainsPlannerRequest): Promise<TrainListPlannerResponse> =>
  executeJsonApiRequest('/ListManagedTrainsPlanner', 'POST', payload)

export const listManagedTrains = (managerId: string): Promise<TrainListPlannerResponse> =>
  listManagedTrainsPlanner({ managerId })

export const registerRailwayManagerPlanner = (payload: RegisterRailwayManagerPlannerRequest): Promise<TrainAdminSessionPlannerResponse> =>
  executeJsonApiRequest('/RegisterRailwayManagerPlanner', 'POST', payload)

export const registerRailwayManager = registerRailwayManagerPlanner
