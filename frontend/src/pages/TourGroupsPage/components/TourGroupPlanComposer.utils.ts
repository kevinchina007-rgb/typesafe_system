import type { TrainResponse } from '@/lib/mvp-types/index'

export function nextDay(dateText: string): string {
  const date = new Date(`${dateText}T00:00:00`)
  date.setDate(date.getDate() + 1)
  return date.toISOString().slice(0, 10)
}

export function atUtc(dateText: string, hour: string): string {
  return `${dateText}T${hour}:00Z`
}

export function toInstantString(value: string): string {
  const parsedDate = new Date(value)
  if (Number.isNaN(parsedDate.getTime())) {
    return value
  }
  return parsedDate.toISOString()
}

export function normalizeSearchToken(value: string): string {
  return value.trim().toLowerCase()
}

export function resolveTrainStop(train: TrainResponse, query: string): TrainResponse['stops'][number] | null {
  const normalizedQuery = normalizeSearchToken(query)
  return (
    train.stops.find(stop => stop.stationCode.trim().toLowerCase() === normalizedQuery) ??
    train.stops.find(stop => normalizeSearchToken(stop.stationName) === normalizedQuery) ??
    train.stops.find(stop => normalizeSearchToken(stop.stationName).includes(normalizedQuery)) ??
    null
  )
}
