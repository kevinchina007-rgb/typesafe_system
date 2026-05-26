import type { TourGroupMessageResponse } from './TourGroupMessageResponse'

export type TourGroupMessageSearchResultResponse = {
  conversationId: string
  conversationTitle: string
  message: TourGroupMessageResponse
}
export const tourGroupMessageSearchResultResponseFromJson = (json: string): TourGroupMessageSearchResultResponse =>
  JSON.parse(json) as TourGroupMessageSearchResultResponse

export const tourGroupMessageSearchResultResponseToJson = (value: TourGroupMessageSearchResultResponse): string =>
  JSON.stringify(value)
