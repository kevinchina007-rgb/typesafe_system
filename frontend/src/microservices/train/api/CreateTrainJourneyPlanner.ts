import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { CreateTrainJourneyPlannerRequest } from '@/microservices/train/objects/CreateTrainJourneyPlannerRequest'
import type { TrainPlannerResponse } from '@/microservices/train/objects/TrainPlannerResponse'

export const createTrainJourneyPlanner = (payload: CreateTrainJourneyPlannerRequest): Promise<TrainPlannerResponse> =>
  executeJsonApiRequest('/CreateTrainJourneyPlanner', 'POST', payload)

export const createTrainJourney = createTrainJourneyPlanner
