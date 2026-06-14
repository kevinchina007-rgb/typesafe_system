import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { TrainListResponse } from '@/microservices/train/objects/TrainListResponse'
import type { TrainSearchQuery } from '@/microservices/train/objects/TrainSearchQuery'

export const listTrains = (query: TrainSearchQuery): Promise<TrainListResponse> =>
  executeJsonApiRequest('/SearchTrainsPlanner', 'POST', query)
