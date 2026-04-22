import type { BookTrainItemRequestDto, TrainListResponse, TrainResponse, TrainSearchQueryDto } from '../api-dtos/trains'
import type { OrderResponse } from '../api-dtos/orders'
import { createQueryString, executeApiRequest, executeJsonApiRequest } from '../api-transport'

export const trainApiClient = {
  listTrains: (query: TrainSearchQueryDto): Promise<TrainListResponse> =>
    executeApiRequest(`/trains${createQueryString(query)}`),

  getTrain: (trainId: string): Promise<TrainResponse> =>
    executeApiRequest(`/trains/${trainId}`),

  addTrainItemToOrder: (orderId: string, payload: BookTrainItemRequestDto): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/train-items`, 'POST', payload),
}
