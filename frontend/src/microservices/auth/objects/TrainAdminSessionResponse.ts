import type { TrainResponse } from '@/microservices/train/objects/TrainResponse'

export type TrainAdminSessionResponse = {
  managerId: string
  operatorCode: string
  email: string
  displayName: string
  status: string
  managedTrains: TrainResponse[]
}
