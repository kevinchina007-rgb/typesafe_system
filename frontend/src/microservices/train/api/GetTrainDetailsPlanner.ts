import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { GetTrainDetailsPlannerRequest } from '@/microservices/train/objects/GetTrainDetailsPlannerRequest'
import type { TrainPlannerResponse } from '@/microservices/train/objects/TrainPlannerResponse'

export const getTrainDetailsPlanner = (payload: GetTrainDetailsPlannerRequest): Promise<TrainPlannerResponse> =>
  executeJsonApiRequest('/GetTrainDetailsPlanner', 'POST', payload)

export const getTrain = (trainId: string): Promise<TrainPlannerResponse> =>
  getTrainDetailsPlanner({ trainId })
