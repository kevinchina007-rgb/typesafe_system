import type { TrainListResponse } from '@/microservices/train/objects/TrainListResponse'
import type { TrainResponse } from '@/microservices/train/objects/TrainResponse'
import type { TrainSearchQuery } from '@/microservices/train/objects/TrainSearchQuery'
import { createQueryString, executeApiRequest } from '@/microservices/common/api/ApiTransport'

export const listTrains = (query: TrainSearchQuery): Promise<TrainListResponse> =>
    executeApiRequest(`/trains${createQueryString(query)}`)

export const getTrain = (trainId: string): Promise<TrainResponse> =>
    executeApiRequest(`/trains/${trainId}`)
