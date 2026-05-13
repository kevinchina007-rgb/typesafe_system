import type { ContentImageResponse } from '@/microservices/content/objects/ContentImageResponse'
import type { BlogPostListResponse } from '@/microservices/content/objects/BlogPostListResponse'
import type { BlogPostResponse } from '@/microservices/content/objects/BlogPostResponse'












import type { SearchSuggestionListResponse } from '@/microservices/common/objects/SearchSuggestionListResponse'
import { createQueryString, createSingleFileFormData, executeApiRequest, executeJsonApiRequest, executeMultipartApiRequest } from '@/microservices/common/api/ApiTransport'

export const listBlogPosts = (scope: 'latest' | 'mine' = 'latest', userId?: string, q?: string): Promise<BlogPostListResponse> =>
    executeApiRequest(`/blog/posts${createQueryString({ scope, userId, q: q?.trim() })}`)

export const listBlogSuggestions = (q: string): Promise<SearchSuggestionListResponse> =>
    executeApiRequest(`/blog/suggestions${createQueryString({ q })}`)

export const uploadBlogImage = (userId: string, imageFile: File): Promise<ContentImageResponse> =>
    executeMultipartApiRequest(
      `/blog/images${createQueryString({ userId })}`,
      'POST',
      createSingleFileFormData('image', imageFile),
    )

export const getBlogPost = (postId: string, userId?: string): Promise<BlogPostResponse> =>
    executeApiRequest(`/blog/posts/${postId}${createQueryString({ userId })}`)

export const listBlogModerationPosts = (scope: 'pending' | 'reviewed' = 'pending'): Promise<BlogPostListResponse> =>
    executeApiRequest(`/blog/moderation/posts${createQueryString({ scope })}`)

export const approveBlogPost = (postId: string): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/moderation/posts/${postId}/approve`, 'POST', {})

export const rejectBlogPost = (postId: string): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/moderation/posts/${postId}/reject`, 'POST', {})

export const createBlogPost = (payload: {
    userId: string
    title: string
    summary: string
    content: string
    images: ContentImageResponse[]
  }): Promise<BlogPostResponse> =>
    executeJsonApiRequest('/blog/posts', 'POST', payload)

export const updateBlogPost = (
    postId: string,
    payload: {
      userId: string
      title: string
      summary: string
      content: string
      images: ContentImageResponse[]
    },
  ): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}`, 'PATCH', payload)

export const archiveBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}/archive`, 'POST', payload)

export const addBlogComment = (postId: string, payload: { userId: string; content: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}/comments`, 'POST', payload)

export const deleteBlogComment = (commentId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/comments/${commentId}`, 'DELETE', payload)

export const likeBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}/likes`, 'POST', payload)

export const unlikeBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}/unlike`, 'POST', payload)
