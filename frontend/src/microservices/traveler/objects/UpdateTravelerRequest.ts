import type { TravelerProfileInput } from './TravelerProfileInput'

export type UpdateTravelerRequest = TravelerProfileInput
export const updateTravelerRequestFromJson = (json: string): UpdateTravelerRequest =>
  JSON.parse(json) as UpdateTravelerRequest

export const updateTravelerRequestToJson = (value: UpdateTravelerRequest): string =>
  JSON.stringify(value)
