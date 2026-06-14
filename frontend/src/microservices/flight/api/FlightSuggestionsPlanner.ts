// 本文件定义 FlightSuggestionsPlanner，负责 flight 模块的建议查询入口。

import type { SearchSuggestionListPlannerResponse } from '@/microservices/flight/objects/SearchSuggestionListPlannerResponse'
import type { FlightSuggestionsPlannerRequest } from '@/microservices/flight/objects/FlightSuggestionsPlannerRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const flightSuggestionsPlanner = (
  payload: FlightSuggestionsPlannerRequest,
): Promise<SearchSuggestionListPlannerResponse> =>
  executeJsonApiRequest('/FlightSuggestionsPlanner', 'POST', payload)

