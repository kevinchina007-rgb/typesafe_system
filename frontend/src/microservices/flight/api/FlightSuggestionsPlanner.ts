import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export type FlightSuggestionsPlannerRequest = {
  q: string
}

export type FlightSuggestionRequest = FlightSuggestionsPlannerRequest

export type SearchSuggestionPlannerResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}

export type SearchSuggestionListPlannerResponse = {
  suggestions: SearchSuggestionPlannerResponse[]
}

export const flightSuggestionsPlanner = (payload: FlightSuggestionsPlannerRequest): Promise<SearchSuggestionListPlannerResponse> =>
  executeJsonApiRequest('/FlightSuggestionsPlanner', 'POST', payload)
