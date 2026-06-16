import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { TrainListPlannerResponse } from '@/microservices/train/objects/TrainListPlannerResponse'
import type { ListManagedTrainsPlannerRequest } from '@/microservices/operations/train/objects/ListManagedTrainsPlannerRequest'

export const listManagedTrainsPlanner = (payload: ListManagedTrainsPlannerRequest): Promise<TrainListPlannerResponse> =>
  executeJsonApiRequest('/ListManagedTrainsPlanner', 'POST', payload)

export const listManagedTrains = (managerId: string): Promise<TrainListPlannerResponse> =>
  listManagedTrainsPlanner({ managerId })

