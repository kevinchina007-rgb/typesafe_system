import type { ContentImageResponse } from '@/microservices/content/objects/ContentImageResponse'
import type { BlogPostListResponse } from '@/microservices/content/objects/BlogPostListResponse'
import type { BlogPostResponse } from '@/microservices/content/objects/BlogPostResponse'












import type { SearchSuggestionListResponse } from '@/microservices/common/objects/SearchSuggestionListResponse'
import { createQueryString, createSingleFileFormData, executeJsonApiRequest, executeMultipartApiRequest } from '@/microservices/common/api/ApiTransport'

export const listBlogPosts = (scope: 'latest' | 'mine' = 'latest', userId?: string, q?: string): Promise<BlogPostListResponse> =>
    executeJsonApiRequest('/ListBlogPostsPlanner', 'POST', { scope, userId, q: q?.trim() })

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

export const createBlogPost = (payload: {
    userId: string
    title: string
    summary: string
    content: string
    images: ContentImageResponse[]
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
    },
  ): Promise<BlogPostResponse> =>
    executeJsonApiRequest('/UpdateBlogPostPlanner', 'POST', { postId, ...payload })

export const archiveBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest('/ArchiveBlogPostPlanner', 'POST', { postId, userId: payload.userId })

export const addBlogComment = (postId: string, payload: { userId: string; content: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest('/AddBlogCommentPlanner', 'POST', { postId, ...payload })

export const deleteBlogComment = (commentId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest('/DeleteBlogCommentPlanner', 'POST', { commentId, userId: payload.userId })

export const likeBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest('/LikeBlogPostPlanner', 'POST', { postId, userId: payload.userId })

export const unlikeBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest('/UnlikeBlogPostPlanner', 'POST', { postId, userId: payload.userId })
