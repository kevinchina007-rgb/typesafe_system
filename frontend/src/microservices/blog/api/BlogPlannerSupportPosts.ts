import type { ContentImageResponse } from '@/microservices/blog/objects/ContentImageResponse'
import type { BlogPostListResponse } from '@/microservices/blog/objects/BlogPostListResponse'
import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import type { BlogTagResponse } from '@/microservices/blog/objects/BlogPostSummaryResponse'
import type { SearchSuggestionListResponse } from '@/shared-kernel/objects/SearchSuggestionListResponse'
import { createQueryString, createSingleFileFormData, executeJsonApiRequest, executeMultipartApiRequest } from '@/shared-kernel/api/ApiTransport'

export type BlogPostQuery = {
  scope?: 'home' | 'latest' | 'mine' | 'profile' | 'drafts' | 'favorites' | 'pending' | 'reviewed'
  userId?: string
  q?: string
  tagType?: string
  tagValue?: string
  travelCity?: string
  travelCities?: string[]
}

export type SaveBlogDraftPayload = {
  userId: string
  postId?: string | null
  title: string
  summary: string
  coverText: string
  content: string
  images: ContentImageResponse[]
  tags: BlogTagResponse[]
  travelCity?: string | null
  travelCities?: string[] | null
}

export const listBlogPosts = (scope: BlogPostQuery['scope'] = 'home', userId?: string, q?: string): Promise<BlogPostListResponse> =>
  executeJsonApiRequest('/ListBlogPostsPlanner', 'POST', { scope, userId, q: q?.trim() })

export const listShortBlogPosts = (query: BlogPostQuery): Promise<BlogPostListResponse> =>
  executeJsonApiRequest('/ListBlogPostsPlanner', 'POST', query)

export const listBlogSuggestions = (q: string): Promise<SearchSuggestionListResponse> =>
  executeJsonApiRequest('/BlogSuggestionsPlanner', 'POST', { q })

export const uploadBlogImage = (userId: string, imageFile: File): Promise<ContentImageResponse> =>
  executeMultipartApiRequest(
    `/blog/images${createQueryString({ userId })}`,
    'POST',
    createSingleFileFormData('image', imageFile),
  )

export const getBlogPost = (postId: string, userId?: string): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/GetBlogPostPlanner', 'POST', { postId, userId })

export const listBlogModerationPosts = (scope: 'pending' | 'reviewed' = 'pending'): Promise<BlogPostListResponse> =>
  executeJsonApiRequest('/ListBlogPostsPlanner', 'POST', { scope })

export const approveBlogPost = (postId: string): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/ApproveBlogPostPlanner', 'POST', { postId })

export const rejectBlogPost = (postId: string): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/RejectBlogPostPlanner', 'POST', { postId })

export const saveBlogDraft = (payload: SaveBlogDraftPayload): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/SaveBlogDraftPlanner', 'POST', payload)

export const publishBlogPost = (postId: string, userId: string): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/PublishBlogPostPlanner', 'POST', { postId, userId })

export const createBlogPost = (payload: {
  userId: string
  title: string
  summary: string
  content: string
  images: ContentImageResponse[]
  tags?: BlogTagResponse[]
  coverText?: string
  travelCity?: string | null
  travelCities?: string[] | null
}): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/CreateBlogPostPlanner', 'POST', payload)

export const updateBlogPost = (
  postId: string,
  payload: {
    userId: string
    title: string
    summary: string
    content: string
    images: ContentImageResponse[]
    tags?: BlogTagResponse[]
    coverText?: string
    travelCity?: string | null
    travelCities?: string[] | null
  },
): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/UpdateBlogPostPlanner', 'POST', { postId, ...payload })
