import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import type { BlogNotificationListResponse } from '@/microservices/blog/objects/BlogNotificationResponse'
import type { BlogProfileResponse, BlogProfileUserListResponse } from '@/microservices/blog/objects/BlogProfileResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

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

