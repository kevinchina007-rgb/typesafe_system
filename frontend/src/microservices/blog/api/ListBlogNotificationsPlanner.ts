// 本文件定义 ListBlogNotificationsPlanner，负责博客域对应接口入口。

import type { BlogNotificationListResponse } from '@/microservices/blog/objects/BlogNotificationListResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const listBlogNotifications = (userId: string): Promise<BlogNotificationListResponse> =>
  executeJsonApiRequest('/ListBlogNotificationsPlanner', 'POST', { userId })
