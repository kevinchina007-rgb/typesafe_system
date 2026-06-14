import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { TrainListPlannerResponse } from '@/microservices/train/objects/TrainListPlannerResponse'
import type { SearchTrainsPlannerRequest } from '@/microservices/train/objects/SearchTrainsPlannerRequest'

export const searchTrainsPlanner = (query: SearchTrainsPlannerRequest): Promise<TrainListPlannerResponse> =>
  executeJsonApiRequest('/SearchTrainsPlanner', 'POST', query)

export const listTrains = searchTrainsPlanner
