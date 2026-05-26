import type { TrainListResponse } from '@/microservices/train/objects/TrainListResponse'
import type { TrainResponse } from '@/microservices/train/objects/TrainResponse'
import type { TrainSearchQuery } from '@/microservices/train/objects/TrainSearchQuery'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listTrains = (query: TrainSearchQuery): Promise<TrainListResponse> =>
  executeJsonApiRequest('/SearchTrainsPlanner', 'POST', query)

export const getTrain = (trainId: string): Promise<TrainResponse> =>
  executeJsonApiRequest('/GetTrainDetailsPlanner', 'POST', { trainId })
