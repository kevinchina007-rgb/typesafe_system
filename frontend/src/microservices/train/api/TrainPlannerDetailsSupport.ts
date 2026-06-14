import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { TrainResponse } from '@/microservices/train/objects/TrainResponse'

export const getTrain = (trainId: string): Promise<TrainResponse> =>
  executeJsonApiRequest('/GetTrainDetailsPlanner', 'POST', { trainId })
