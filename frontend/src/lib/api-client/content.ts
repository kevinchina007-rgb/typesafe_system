import type {
  BlogPostListResponse,
  BlogPostResponse,
  ContentImageResponse,
  CreateReviewFeedbackThreadRequest,
  EscalateFeedbackThreadRequest,
  ExploreSearchResponse,
  FeedbackThreadListResponse,
  FeedbackThreadResponse,
  FeedbackSiteAdminChannel,
  MarkFeedbackThreadReadRequest,
  ResourceReviewSummaryResponse,
  ReviewEligibilityResponse,
  ReviewListResponse,
  ReviewResponse,
  SearchSuggestionListResponse,
  SendFeedbackMessageRequest,
} from '../api-dtos'
import { createQueryString, createSingleFileFormData, executeApiRequest, executeJsonApiRequest, executeMultipartApiRequest } from '../api-transport'

export const contentApiClient = {
  listBlogPosts: (scope: 'latest' | 'mine' = 'latest', userId?: string, q?: string): Promise<BlogPostListResponse> =>
    executeApiRequest(`/blog/posts${createQueryString({ scope, userId, q: q?.trim() })}`),

  listBlogSuggestions: (q: string): Promise<SearchSuggestionListResponse> =>
    executeApiRequest(`/blog/suggestions${createQueryString({ q })}`),

  listExploreSuggestions: (q: string): Promise<SearchSuggestionListResponse> =>
    executeApiRequest(`/explore/suggestions${createQueryString({ q })}`),

  searchExplore: (payload: { q: string; type?: string }): Promise<ExploreSearchResponse> =>
    executeApiRequest(`/explore/search${createQueryString({ q: payload.q, type: payload.type && payload.type !== 'all' ? payload.type : undefined })}`),

  uploadBlogImage: (userId: string, imageFile: File): Promise<ContentImageResponse> =>
    executeMultipartApiRequest(
      `/blog/images${createQueryString({ userId })}`,
      'POST',
      createSingleFileFormData('image', imageFile),
    ),

  getBlogPost: (postId: string, userId?: string): Promise<BlogPostResponse> =>
    executeApiRequest(`/blog/posts/${postId}${createQueryString({ userId })}`),

  listBlogModerationPosts: (scope: 'pending' | 'reviewed' = 'pending'): Promise<BlogPostListResponse> =>
    executeApiRequest(`/blog/moderation/posts${createQueryString({ scope })}`),

  approveBlogPost: (postId: string): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/moderation/posts/${postId}/approve`, 'POST', {}),

  rejectBlogPost: (postId: string): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/moderation/posts/${postId}/reject`, 'POST', {}),

  createBlogPost: (payload: {
    userId: string
    title: string
    summary: string
    content: string
    images: ContentImageResponse[]
  }): Promise<BlogPostResponse> =>
    executeJsonApiRequest('/blog/posts', 'POST', payload),

  updateBlogPost: (
    postId: string,
    payload: {
      userId: string
      title: string
      summary: string
      content: string
      images: ContentImageResponse[]
    },
  ): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}`, 'PATCH', payload),

  archiveBlogPost: (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}/archive`, 'POST', payload),

  addBlogComment: (postId: string, payload: { userId: string; content: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}/comments`, 'POST', payload),

  deleteBlogComment: (commentId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/comments/${commentId}`, 'DELETE', payload),

  likeBlogPost: (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}/likes`, 'POST', payload),

  unlikeBlogPost: (postId: string, payload: { userId: string }): Promise<BlogPostResponse> =>
    executeJsonApiRequest(`/blog/posts/${postId}/unlike`, 'POST', payload),

  listMyReviews: (userId: string): Promise<ReviewListResponse> =>
    executeApiRequest(`/reviews/mine${createQueryString({ userId })}`),

  listReviewsByResource: (payload: { userId: string; resourceType: string; resourceId: string }): Promise<ReviewListResponse> =>
    executeApiRequest(`/reviews${createQueryString(payload)}`),

  getReviewResourceSummary: (payload: { userId: string; resourceType: string; resourceId: string }): Promise<ResourceReviewSummaryResponse> =>
    executeApiRequest(`/reviews/summary${createQueryString(payload)}`),

  getReviewEligibility: (payload: { userId: string; orderItemId: string }): Promise<ReviewEligibilityResponse> =>
    executeApiRequest(`/reviews/eligibility${createQueryString(payload)}`),

  createReview: (payload: {
    userId: string
    orderId: string
    orderItemId: string
    rating: number
    title: string
    content: string
    images: ContentImageResponse[]
  }): Promise<ReviewResponse> =>
    executeJsonApiRequest('/reviews', 'POST', payload),

  updateReview: (
    reviewId: string,
    payload: {
      userId: string
      rating: number
      title: string
      content: string
      images: ContentImageResponse[]
    },
  ): Promise<ReviewResponse> =>
    executeJsonApiRequest(`/reviews/${reviewId}`, 'PATCH', payload),

  uploadReviewImage: (userId: string, imageFile: File): Promise<ContentImageResponse> =>
    executeMultipartApiRequest(
      `/reviews/images${createQueryString({ userId })}`,
      'POST',
      createSingleFileFormData('image', imageFile),
    ),

  deleteReview: (reviewId: string, payload: { userId: string }): Promise<void> =>
    executeJsonApiRequest(`/reviews/${reviewId}`, 'DELETE', payload),

  createReviewFeedbackThread: (payload: CreateReviewFeedbackThreadRequest): Promise<FeedbackThreadResponse> =>
    executeJsonApiRequest('/feedback/review-threads', 'POST', payload),

  listMyFeedbackThreads: (): Promise<FeedbackThreadListResponse> =>
    executeApiRequest('/feedback/threads/mine'),

  listManagerFeedbackThreads: (): Promise<FeedbackThreadListResponse> =>
    executeApiRequest('/feedback/threads/manager'),

  listSiteAdminFeedbackThreads: (channel: FeedbackSiteAdminChannel): Promise<FeedbackThreadListResponse> =>
    executeApiRequest(`/feedback/threads/site-admin${createQueryString({ channel })}`),

  sendFeedbackMessage: (threadId: string, payload: SendFeedbackMessageRequest): Promise<FeedbackThreadResponse> =>
    executeJsonApiRequest(`/feedback/threads/${threadId}/messages`, 'POST', payload),

  markFeedbackThreadRead: (threadId: string, payload: MarkFeedbackThreadReadRequest): Promise<FeedbackThreadResponse> =>
    executeJsonApiRequest(`/feedback/threads/${threadId}/read`, 'POST', payload),

  escalateFeedbackThread: (threadId: string, payload: EscalateFeedbackThreadRequest): Promise<FeedbackThreadResponse> =>
    executeJsonApiRequest(`/feedback/threads/${threadId}/escalate`, 'POST', payload),
}
