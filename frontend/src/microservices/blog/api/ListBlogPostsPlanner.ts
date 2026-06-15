// 本文件定义 ListBlogPostsPlanner，负责博客域对应接口入口。

import type { BlogPostListResponse } from '@/microservices/blog/objects/BlogPostListResponse'
import type { ListBlogPostsPlannerRequest } from '@/microservices/blog/objects/ListBlogPostsPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export type BlogPostQuery = ListBlogPostsPlannerRequest

export const listBlogPosts = (scope: BlogPostQuery['scope'] = 'home', userId?: string, q?: string): Promise<BlogPostListResponse> =>
  executeJsonApiRequest('/ListBlogPostsPlanner', 'POST', { scope, userId, q: q?.trim() })

export const listShortBlogPosts = (query: BlogPostQuery): Promise<BlogPostListResponse> =>
  executeJsonApiRequest('/ListBlogPostsPlanner', 'POST', query)

export const listBlogModerationPosts = (scope: 'pending' | 'reviewed' = 'pending'): Promise<BlogPostListResponse> =>
  executeJsonApiRequest('/ListBlogPostsPlanner', 'POST', { scope })
