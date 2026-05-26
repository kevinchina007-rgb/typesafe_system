import type { AttractionResponse } from './AttractionResponse'

export type AttractionListResponse = {
  attractions: AttractionResponse[]
}
export const attractionListResponseFromJson = (json: string): AttractionListResponse =>
  JSON.parse(json) as AttractionListResponse

export const attractionListResponseToJson = (value: AttractionListResponse): string =>
  JSON.stringify(value)
