// 本文件定义 ListBlogFollowersPlanner，负责博客域对应接口入口。

import type { BlogProfileUserListResponse } from '@/microservices/blog/objects/BlogProfileUserListResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const listBlogFollowers = (profileUserId: string, viewerUserId?: string): Promise<BlogProfileUserListResponse> =>
  executeJsonApiRequest('/ListBlogFollowersPlanner', 'POST', { profileUserId, viewerUserId })
