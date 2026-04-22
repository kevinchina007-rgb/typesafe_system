export type ContentImageResponse = {
  imageId: string
  publicUrl: string
  originalFileName: string
  sortOrder: number
  createdAt: string
}

export type BlogCommentResponse = {
  commentId: string
  postId: string
  authorUserId: string
  authorDisplayName: string
  authorAvatarUrl: string | null
  content: string
  createdAt: string
  isMyComment: boolean
  canDelete: boolean
}

export type BlogPostSummaryResponse = {
  postId: string
  authorUserId: string
  authorDisplayName: string
  authorAvatarUrl: string | null
  title: string
  summary: string
  status: string
  createdAt: string
  updatedAt: string
  publishedAt: string | null
  commentCount: number
  likeCount: number
  likedByCurrentUser: boolean
  isMyPost: boolean
  canEdit: boolean
  canArchive: boolean
  images: ContentImageResponse[]
  searchResultSnippet: string | null
}

export type BlogPostResponse = {
  post: BlogPostSummaryResponse
  content: string
  comments: BlogCommentResponse[]
}

export type BlogPostListResponse = {
  posts: BlogPostSummaryResponse[]
}

export type ReviewEligibilityResponse = {
  orderId: string
  orderItemId: string
  canReview: boolean
  alreadyReviewed: boolean
  reason: string | null
  resourceSummaryTitle: string
}

export type ReviewResponse = {
  reviewId: string
  authorUserId: string
  authorDisplayName: string
  authorAvatarUrl: string | null
  resourceType: string
  resourceId: string
  resourceSummaryTitle: string
  resourceSummarySubtitle: string
  orderId: string
  orderItemId: string
  rating: number
  title: string
  content: string
  status: string
  createdAt: string
  updatedAt: string
  isMyReview: boolean
  canEdit: boolean
  canDelete: boolean
  images: ContentImageResponse[]
}

export type ReviewListResponse = {
  reviews: ReviewResponse[]
}

export type ResourceReviewSummaryResponse = {
  resourceType: string
  resourceId: string
  averageRating: string
  reviewCount: number
}

export type FeedbackSenderRole = 'User' | 'Manager' | 'SiteAdmin' | 'System'
export type FeedbackAudience = 'User' | 'Manager' | 'SiteAdmin'
export type FeedbackThreadKind = 'ServiceReview' | 'ManagerEscalation'
export type FeedbackManagerType = 'Airline' | 'Hotel' | 'Train' | 'Attraction' | 'SiteAdmin'
export type FeedbackSiteAdminChannel = 'user' | 'manager'

export type FeedbackMessageResponse = {
  messageId: string
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  body: string
  sentAt: string
}

export type FeedbackThreadResponse = {
  threadId: string
  kind: FeedbackThreadKind
  managerType: FeedbackManagerType
  ownerUserId: string | null
  ownerUserDisplayName: string
  title: string
  subtitle: string
  resourceType: string
  resourceSummaryTitle: string
  orderId: string | null
  orderItemId: string | null
  reviewId: string | null
  relatedThreadId: string | null
  unreadByUser: number
  unreadByManager: number
  unreadBySiteAdmin: number
  createdAt: string
  updatedAt: string
  messages: FeedbackMessageResponse[]
}

export type FeedbackThreadListResponse = {
  threads: FeedbackThreadResponse[]
}

export type FeedbackMessage = FeedbackMessageResponse
export type FeedbackThread = FeedbackThreadResponse

export type CreateReviewFeedbackThreadRequest = {
  reviewId: string
}

export type SendFeedbackMessageRequest = {
  senderRole: FeedbackSenderRole
  senderDisplayName: string
  body: string
}

export type MarkFeedbackThreadReadRequest = {
  audience: FeedbackAudience
}

export type EscalateFeedbackThreadRequest = {
  senderDisplayName: string
  body: string
}
