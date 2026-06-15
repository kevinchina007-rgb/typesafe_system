// 本文件定义 BlogSuggestionsPlanner，负责博客域对应接口入口。

import type { SearchSuggestionListResponse } from '@/shared-kernel/objects/SearchSuggestionListResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const listBlogSuggestions = (q: string): Promise<SearchSuggestionListResponse> =>
  executeJsonApiRequest('/BlogSuggestionsPlanner', 'POST', { q })
