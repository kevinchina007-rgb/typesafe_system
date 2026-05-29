import type { ContentImageResponse } from '@/microservices/content/objects/ContentImageResponse'
import type { BlogPostListResponse } from '@/microservices/content/objects/BlogPostListResponse'
import type { BlogPostResponse } from '@/microservices/content/objects/BlogPostResponse'
import type { BlogTagResponse } from '@/microservices/content/objects/BlogPostSummaryResponse'
import type { BlogNotificationListResponse } from '@/microservices/content/objects/BlogNotificationResponse'
import type { BlogProfileResponse, BlogProfileUserListResponse } from '@/microservices/content/objects/BlogProfileResponse'
import type { SearchSuggestionListResponse } from '@/microservices/common/objects/SearchSuggestionListResponse'
import { createQueryString, createSingleFileFormData, executeJsonApiRequest, executeMultipartApiRequest } from '@/microservices/common/api/ApiTransport'

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

export const archiveBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/ArchiveBlogPostPlanner', 'POST', { postId, userId: payload.userId })

export const addBlogComment = (
  postId: string,
  payload: { userId: string; content: string; parentCommentId?: string | null; replyToUserId?: string | null },
): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/AddBlogCommentPlanner', 'POST', { postId, ...payload })

export const deleteBlogComment = (commentId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/DeleteBlogCommentPlanner', 'POST', { commentId, userId: payload.userId })

export const likeBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/LikeBlogPostPlanner', 'POST', { postId, userId: payload.userId })

export const unlikeBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/UnlikeBlogPostPlanner', 'POST', { postId, userId: payload.userId })

export const likeBlogComment = (commentId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/LikeBlogCommentPlanner', 'POST', { commentId, userId: payload.userId })

export const unlikeBlogComment = (commentId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/UnlikeBlogCommentPlanner', 'POST', { commentId, userId: payload.userId })

export const favoriteBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/FavoriteBlogPostPlanner', 'POST', { postId, userId: payload.userId })

export const unfavoriteBlogPost = (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
  executeJsonApiRequest('/UnfavoriteBlogPostPlanner', 'POST', { postId, userId: payload.userId })

export const followBlogUser = (userId: string, targetUserId: string): Promise<BlogProfileResponse> =>
  executeJsonApiRequest('/FollowBlogUserPlanner', 'POST', { userId, targetUserId })

export const blockBlogUser = (userId: string, targetUserId: string): Promise<BlogProfileResponse> =>
  executeJsonApiRequest('/BlockBlogUserPlanner', 'POST', { userId, targetUserId })

export const listBlogNotifications = (userId: string): Promise<BlogNotificationListResponse> =>
  executeJsonApiRequest('/ListBlogNotificationsPlanner', 'POST', { userId })

export const getBlogProfile = (profileUserId: string, viewerUserId?: string): Promise<BlogProfileResponse> =>
  executeJsonApiRequest('/GetBlogProfilePlanner', 'POST', { profileUserId, viewerUserId })

export const updateBlogProfilePrivacy = (userId: string, hideRelations: boolean): Promise<BlogProfileResponse> =>
  executeJsonApiRequest('/UpdateBlogProfilePrivacyPlanner', 'POST', { userId, hideRelations })

export const listBlogFollowers = (profileUserId: string, viewerUserId?: string): Promise<BlogProfileUserListResponse> =>
  executeJsonApiRequest('/ListBlogFollowersPlanner', 'POST', { profileUserId, viewerUserId })

export const listBlogFollowing = (profileUserId: string, viewerUserId?: string): Promise<BlogProfileUserListResponse> =>
  executeJsonApiRequest('/ListBlogFollowingPlanner', 'POST', { profileUserId, viewerUserId })
