import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { TrainPlannerResponse } from '@/microservices/train/objects/TrainPlannerResponse'
import type { GetTrainDetailsPlannerRequest } from '@/microservices/train/objects/GetTrainDetailsPlannerRequest'

export const getTrainDetailsPlanner = (payload: GetTrainDetailsPlannerRequest): Promise<TrainPlannerResponse> =>
  executeJsonApiRequest('/GetTrainDetailsPlanner', 'POST', payload)

export const getTrain = (trainId: string): Promise<TrainPlannerResponse> =>
  getTrainDetailsPlanner({ trainId })
