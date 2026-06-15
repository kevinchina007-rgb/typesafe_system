// 本文件定义 UpdateBlogProfilePrivacyPlanner，负责博客域对应接口入口。

import type { BlogProfileResponse } from '@/microservices/blog/objects/BlogProfileResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const updateBlogProfilePrivacy = (userId: string, hideRelations: boolean): Promise<BlogProfileResponse> =>
  executeJsonApiRequest('/UpdateBlogProfilePrivacyPlanner', 'POST', { userId, hideRelations })
