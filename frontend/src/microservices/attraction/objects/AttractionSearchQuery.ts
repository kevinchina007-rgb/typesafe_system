export type AttractionSearchQuery = {
  city?: string
  keyword?: string
  useDate?: string
}

export const attractionSearchQueryFromJson = (json: string): AttractionSearchQuery =>
  JSON.parse(json) as AttractionSearchQuery

export const attractionSearchQueryToJson = (value: AttractionSearchQuery): string =>
  JSON.stringify(value)
