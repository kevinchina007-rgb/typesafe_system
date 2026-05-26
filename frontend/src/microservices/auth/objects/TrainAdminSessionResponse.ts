import type { TrainResponse } from '@/microservices/train/objects/TrainResponse'

export type TrainAdminSessionResponse = {
  managerId: string
  operatorCode: string
  email: string
  displayName: string
  status: string
  managedTrains: TrainResponse[]
}
export const trainAdminSessionResponseFromJson = (json: string): TrainAdminSessionResponse =>
  JSON.parse(json) as TrainAdminSessionResponse

export const trainAdminSessionResponseToJson = (value: TrainAdminSessionResponse): string =>
  JSON.stringify(value)
