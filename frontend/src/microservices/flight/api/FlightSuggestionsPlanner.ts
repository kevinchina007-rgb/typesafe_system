import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export type FlightSuggestionRequest = {
  q: string
}

export type SearchSuggestionResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}

export type SearchSuggestionListResponse = {
  suggestions: SearchSuggestionResponse[]
}

export const listFlightSuggestions = (payload: FlightSuggestionRequest): Promise<SearchSuggestionListResponse> =>
  executeJsonApiRequest('/FlightSuggestionsPlanner', 'POST', payload)
