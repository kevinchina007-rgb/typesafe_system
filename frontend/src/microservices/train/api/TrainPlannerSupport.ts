// 本文件定义 train 模块的 TrainPlannerSupport 共享函数，供多个 planner 复用参数整理和结果映射。

import type { TrainAdminSessionResponse } from '@/microservices/auth/objects/TrainAdminSessionResponse'
import type { TrainListResponse } from '@/microservices/train/objects/TrainListResponse'
import type { TrainResponse } from '@/microservices/train/objects/TrainResponse'
import type { TrainSearchQuery } from '@/microservices/train/objects/TrainSearchQuery'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listTrains = (query: TrainSearchQuery): Promise<TrainListResponse> =>
  executeJsonApiRequest('/SearchTrainsPlanner', 'POST', query)

export const getTrain = (trainId: string): Promise<TrainResponse> =>
  executeJsonApiRequest('/GetTrainDetailsPlanner', 'POST', { trainId })

export const createTrainJourney = (payload: {
  managerId: string
  trainNumber: string
  saleStartsAt: string
  stops: Array<{ stationCode: string; stationName: string; arrivalTime?: string | null; departureTime?: string | null }>
  seatInventories: Array<{ seatClass: string; totalSeats: number; saleableSeats: number; carriageCount: number; rowsPerCarriage: number; seatLayoutSpec: string }>
  segmentPrices: Array<{ fromStationCode: string; toStationCode: string; seatClass: string; amount: string; currency: string }>
  refundPolicies: Array<{ startOffsetMinutesBeforeDeparture: number; endOffsetMinutesBeforeDeparture: number; refundType: string; refundRate: string }>
}) =>
  executeJsonApiRequest('/CreateTrainJourneyPlanner', 'POST', payload)

export const listManagedTrains = (managerId: string): Promise<TrainListResponse> =>
  executeJsonApiRequest('/ListManagedTrainsPlanner', 'POST', { managerId })

export const registerRailwayManager = (payload: { operatorCode: string; email: string; displayName: string; password: string }): Promise<TrainAdminSessionResponse> =>
  executeJsonApiRequest('/RegisterRailwayManagerPlanner', 'POST', payload)
