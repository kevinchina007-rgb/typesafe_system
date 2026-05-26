import type { TourGroupMessageResponse } from './TourGroupMessageResponse'

export type TourGroupMessageListResponse = {
  messages: TourGroupMessageResponse[]
}
export const tourGroupMessageListResponseFromJson = (json: string): TourGroupMessageListResponse =>
  JSON.parse(json) as TourGroupMessageListResponse

export const tourGroupMessageListResponseToJson = (value: TourGroupMessageListResponse): string =>
  JSON.stringify(value)
