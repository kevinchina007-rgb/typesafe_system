import type { TrainResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime } from '@/lib/presenters/view-models'
import { resolveTrainSearchSegment } from '@/app/stores/models/train-booking-model'

export function formatTrainStopTimeLabel(stop: TrainResponse['stops'][number] | null | undefined): string {
  return formatIsoDateTime(stop?.departureTime ?? stop?.arrivalTime ?? null, '-')
}

export function getTrainDestinationStationLabel(trainResponse: TrainResponse, searchFromStation: string, searchToStation: string): string {
  const routeSegment = resolveTrainSearchSegment(trainResponse, searchFromStation, searchToStation)
  if (!routeSegment) {
    return '到达'
  }
  const terminalStop = trainResponse.stops[trainResponse.stops.length - 1] ?? null
  return terminalStop && routeSegment.toStop.stopId === terminalStop.stopId ? '到达' : '经停'
}
