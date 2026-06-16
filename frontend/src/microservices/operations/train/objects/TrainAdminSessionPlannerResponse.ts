import type { TrainPlannerResponse } from '@/microservices/train/objects/TrainPlannerResponse'

export type TrainAdminSessionPlannerResponse = {
  managerId: string
  operatorCode: string
  email: string
  displayName: string
  status: string
  managedTrains: TrainPlannerResponse[]
}

export const trainAdminSessionPlannerResponseFromJson = (json: string): TrainAdminSessionPlannerResponse =>
  JSON.parse(json) as TrainAdminSessionPlannerResponse

export const trainAdminSessionPlannerResponseToJson = (value: TrainAdminSessionPlannerResponse): string =>
  JSON.stringify(value)

