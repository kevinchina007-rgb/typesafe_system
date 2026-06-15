// 博客域前端对象统一导出入口，同时保留对象模块清单，方便追踪每个数据结构的实际定义。

import * as BlogCommentResponseModule from './BlogCommentResponse'
import * as BlogNotificationListResponseModule from './BlogNotificationListResponse'
import * as BlogNotificationResponseModule from './BlogNotificationResponse'
import * as BlogPostListResponseModule from './BlogPostListResponse'
import * as BlogPostResponseModule from './BlogPostResponse'
import * as BlogPostSummaryResponseModule from './BlogPostSummaryResponse'
import * as BlogProfileResponseModule from './BlogProfileResponse'
import * as BlogProfileUserListResponseModule from './BlogProfileUserListResponse'
import * as BlogProfileUserResponseModule from './BlogProfileUserResponse'
import * as BlogTagResponseModule from './BlogTagResponse'

export const blogObjectModules = Object.freeze({
  BlogCommentResponseModule,
  BlogNotificationListResponseModule,
  BlogNotificationResponseModule,
  BlogPostListResponseModule,
  BlogPostResponseModule,
  BlogPostSummaryResponseModule,
  BlogProfileResponseModule,
  BlogProfileUserListResponseModule,
  BlogProfileUserResponseModule,
  BlogTagResponseModule,
})

export * from './BlogCommentResponse'
export * from './BlogNotificationListResponse'
export * from './BlogNotificationResponse'
export * from './BlogPostListResponse'
export * from './BlogPostResponse'
export * from './BlogPostSummaryResponse'
export * from './BlogProfileResponse'
export * from './BlogProfileUserListResponse'
export * from './BlogProfileUserResponse'
export * from './BlogTagResponse'
