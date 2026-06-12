import type { TrainResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime } from '@/lib/presenters/view-models'
import { resolveTrainSearchSegment } from '@/app/stores/models/train-booking-model'

// 把车站站点对象格式化成页面里可读的时间文本。
export function formatTrainStopTimeLabel(stop: TrainResponse['stops'][number] | null | undefined): string {
  return formatIsoDateTime(stop?.departureTime ?? stop?.arrivalTime ?? null, '-')
}

// 生成列车出发站标签，区分始发和经停。
export function getTrainDepartureStationLabel(trainResponse: TrainResponse, searchFromStation: string, searchToStation: string): string {
  const routeSegment = resolveTrainSearchSegment(trainResponse, searchFromStation, searchToStation)
  if (!routeSegment) {
    return '出发'
  }
  const firstStop = trainResponse.stops[0] ?? null
  return firstStop && routeSegment.fromStop.stopId === firstStop.stopId ? '始发' : '经停'
}

// 生成列车到达站标签，区分终到和经停。
export function getTrainDestinationStationLabel(trainResponse: TrainResponse, searchFromStation: string, searchToStation: string): string {
  const routeSegment = resolveTrainSearchSegment(trainResponse, searchFromStation, searchToStation)
  if (!routeSegment) {
    return '到达'
  }
  const terminalStop = trainResponse.stops[trainResponse.stops.length - 1] ?? null
  return terminalStop && routeSegment.toStop.stopId === terminalStop.stopId ? '终到' : '经停'
}
