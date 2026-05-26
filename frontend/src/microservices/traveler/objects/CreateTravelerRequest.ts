import type { TravelerProfileInput } from './TravelerProfileInput'

export type CreateTravelerRequest = TravelerProfileInput
export const createTravelerRequestFromJson = (json: string): CreateTravelerRequest =>
  JSON.parse(json) as CreateTravelerRequest

export const createTravelerRequestToJson = (value: CreateTravelerRequest): string =>
  JSON.stringify(value)
