import type { TravelerResponse } from './TravelerResponse'

export type TravelerListResponse = {
  travelers: TravelerResponse[]
}
export const travelerListResponseFromJson = (json: string): TravelerListResponse =>
  JSON.parse(json) as TravelerListResponse

export const travelerListResponseToJson = (value: TravelerListResponse): string =>
  JSON.stringify(value)
