export type HealthResponse = {
  status: string
  service: string
  backendPort: number
}

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

export type SearchSuggestionResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}

export type SearchSuggestionListResponse = {
  suggestions: SearchSuggestionResponse[]
}

export type ExploreSearchResultResponse = {
  resourceType: string
  resourceId: string
  title: string
  summary: string
  metaLabel: string
  navigationHint: string
  imageUrl: string | null
}

export type ExploreSearchResponse = {
  results: ExploreSearchResultResponse[]
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

export type ApiErrorResponse = {
  code: string
  message: string
}

export type UserResponse = {
  userId: string
  email: string
  nickname: string
  phone: string
  avatarUrl: string | null
  status: string
  membershipLevel: string
  points: number
  defaultTravelerProfileId: string | null
  createdAt: string
}

export type CurrentUserSessionResponse = {
  user: UserResponse
  expiresAt: string
}

export type AuthSessionResponse = {
  sessionId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
  isCurrent: boolean
}

export type AuthSessionListResponse = {
  sessions: AuthSessionResponse[]
}

export type TravelerResponse = {
  travelerId: string
  ownerUserId: string
  fullName: string
  documentType: string
  documentNumber: string
  phone: string
  birthDate: string
  travelerType: string
  status: string
  isDefault: boolean
}

export type TravelerListResponse = {
  travelers: TravelerResponse[]
}

export type CabinInventoryResponse = {
  inventoryId: string
  cabinClass: string
  availableSeats: number
  unitPrice: string
  currency: string
  status: string
  isBookable: boolean
}

export type FlightResponse = {
  flightId: string
  airlineId: string
  airlineName: string
  airlineCode: string
  flightNumber: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  status: string
  basePrice: string
  currency: string
  createdAt: string
  cabinInventories: CabinInventoryResponse[]
}

export type FlightListResponse = {
  flights: FlightResponse[]
}

export type RoomTypeSummaryResponse = {
  roomTypeId: string
  roomTypeName: string
  capacity: number
  bedType: string
  basePrice: string
  currency: string
  status: string
  isBookableForRequestedStay: boolean
  availableRoomsForRequestedStay: number | null
}

export type HotelResponse = {
  hotelId: string
  hotelName: string
  location: string
  status: string
  createdAt: string
  roomTypes: RoomTypeSummaryResponse[]
}

export type HotelListResponse = {
  hotels: HotelResponse[]
}

export type TrainStopResponse = {
  stopId: string
  stationCode: string
  stationName: string
  sequenceNo: number
  arrivalTime: string | null
  departureTime: string | null
}

export type TrainSeatInventoryResponse = {
  inventoryId: string
  seatClass: string
  totalSeats: number
  saleableSeats: number
  status: string
}

export type TrainSeatResponse = {
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatClass: string
  seatPositionType: string
  status: string
}

export type TrainSegmentPriceResponse = {
  fromStationCode: string
  toStationCode: string
  seatClass: string
  amount: string
  currency: string
}

export type TrainRefundPolicyResponse = {
  startOffsetMinutesBeforeDeparture: number
  endOffsetMinutesBeforeDeparture: number
  refundType: string
  refundRate: string
}

export type AttractionTicketTypeRuleResponse = {
  ruleId: string
  ruleType: string
  summary: string
}

export type AttractionTicketTypeResponse = {
  ticketTypeId: string
  ticketTypeName: string
  description: string
  priceAmount: string
  priceCurrency: string
  status: string
  availableFromDate: string
  availableToDate: string
  totalQuantity: number
  validWeekdays: string[]
  availableQuantityForRequestedDate: number | null
  isAvailableForRequestedDate: boolean
  rules: AttractionTicketTypeRuleResponse[]
  sessions: AttractionTicketSessionResponse[]
}

export type AttractionTicketSessionResponse = {
  sessionId: string
  sessionName: string
  useDate: string
  startsAt: string
  endsAt: string
  capacity: number
  availableQuantity: number | null
  status: string
}

export type AttractionResponse = {
  attractionId: string
  attractionName: string
  city: string
  location: string
  description: string
  status: string
  ticketTypes: AttractionTicketTypeResponse[]
}

export type AttractionListResponse = {
  attractions: AttractionResponse[]
}

export type TrainResponse = {
  trainId: string
  trainNumber: string
  saleStartsAt: string
  status: string
  stops: TrainStopResponse[]
  seatInventories: TrainSeatInventoryResponse[]
  seats: TrainSeatResponse[]
  segmentPrices: TrainSegmentPriceResponse[]
  refundPolicies: TrainRefundPolicyResponse[]
}

export type TrainSeatAssignmentResponse = {
  travelerId: string
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatPositionType: string
}

export type TrainListResponse = {
  trains: TrainResponse[]
}

export type FlightItemDetailsResponse = {
  airlineName: string
  airlineCode: string
  flightId: string
  flightNumber: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  cabinClass: string
  travelerIds: string[]
  reservationStatus: string | null
  reservationExpiresAt: string | null
  unitPrice: string
  currency: string
}

export type HotelItemDetailsResponse = {
  hotelId: string
  hotelName: string
  location: string
  roomTypeId: string
  roomTypeName: string
  checkInDate: string
  checkOutDate: string
  guestTravelerIds: string[]
  roomCount: number
  reservationStatus: string | null
  reservationExpiresAt: string | null
  unitPrice: string
  totalPrice: string
  currency: string
}

export type TrainItemDetailsResponse = {
  trainId: string
  trainNumber: string
  fromStationCode: string
  fromStationName: string
  toStationCode: string
  toStationName: string
  departureTime: string
  arrivalTime: string
  seatClass: string
  requestedSeatPreference: string | null
  seatAssignments: TrainSeatAssignmentResponse[]
  travelerIds: string[]
  reservationStatus: string | null
  reservationExpiresAt: string | null
  unitPrice: string
  totalPrice: string
  currency: string
}

export type AttractionItemDetailsResponse = {
  attractionId: string
  attractionName: string
  ticketTypeId: string
  ticketTypeName: string
  sessionId: string | null
  sessionName: string | null
  sessionStartsAt: string | null
  sessionEndsAt: string | null
  useDate: string
  travelerIds: string[]
  unitPrice: string
  totalPrice: string
  currency: string
  eligibilityRuleSummaries: string[]
  eligibilityValidatedAt: string
}

export type SupplierReviewDecisionResponse = {
  decision: string
  reason: string | null
  decidedAt: string
  managerId: string
}

export type OrderLineItemResponse = {
  orderItemId: string
  orderItemKind: string
  orderItemStatus: string
  supplierReviewStatus: string
  supplierReviewDecision: SupplierReviewDecisionResponse | null
  bookedAmount: string
  bookedCurrency: string
  summaryLabel: string
  flightDetails: FlightItemDetailsResponse | null
  hotelDetails: HotelItemDetailsResponse | null
  trainDetails: TrainItemDetailsResponse | null
  attractionDetails: AttractionItemDetailsResponse | null
}

export type PaymentResponse = {
  paymentId: string
  paymentAmount: string
  paymentCurrency: string
  paymentMethod: string
  paymentStatus: string
  authorizedAt: string
  capturedAt: string | null
}

export type RefundResponse = {
  refundId: string
  refundAmount: string
  refundCurrency: string
  refundReason: string
  refundStatus: string
  requestedAt: string
  approvedAt: string | null
  settledAt: string | null
}

export type OrderResponse = {
  orderId: string
  buyerUserId: string
  orderType: string
  status: string
  orderCurrency: string
  totalPrice: string
  totalCapturedAmount: string
  totalSettledRefundAmount: string
  remainingRefundableAmount: string
  createdAt: string
  paidAt: string | null
  confirmedAt: string | null
  completedAt: string | null
  cancelledAt: string | null
  orderLineItems: OrderLineItemResponse[]
  orderPayments: PaymentResponse[]
  orderRefunds: RefundResponse[]
}

export type OrderListResponse = {
  orders: OrderResponse[]
}

export type AppLanguage = 'en' | 'zh'

export type ManagerType = 'airline' | 'hotel' | 'train' | 'attraction'

export type TrainAdminSessionResponse = {
  managerId: string
  operatorCode: string
  email: string
  displayName: string
  status: string
  managedTrains: TrainResponse[]
}

export type AttractionAdminSessionResponse = {
  managerId: string
  email: string
  displayName: string
  status: string
  managedAttractions: AttractionResponse[]
}

export type TourGroupSummaryResponse = {
  groupId: string
  organizerUserId: string
  title: string
  description: string
  destination: string
  startDate: string
  endDate: string
  capacity: number
  usedCapacity: number
  isFull: boolean
  memberCount: number
  activeTravelerCount: number
  pendingSelectionCount: number
  confirmedSelectionCount: number
  convertedOrderCount: number
  status: string
  createdAt: string
}

export type TourGroupMembershipResponse = {
  membershipId: string
  userId: string
  status: string
  joinedAt: string
}

export type TourGroupMembershipTravelerResponse = {
  membershipTravelerId: string
  membershipId: string
  travelerId: string
  status: string
  joinedAt: string
}

export type GroupPlanItemResponse = {
  planItemId: string
  itemType: string
  title: string
  description: string
  scheduledAt: string
  endsAt: string | null
  sequenceNo: number
  status: string
}

export type GroupPlanOptionResponse = {
  optionId: string
  planItemId: string
  resourceType: string
  resourceId: string
  resourceVariantCode: string | null
  resourceContext: string | null
  label: string
  description: string
  defaultQuantity: number
  status: string
}

export type GroupPlanSelectionResponse = {
  selectionId: string
  groupId: string
  planItemId: string
  optionId: string
  membershipId: string
  quantity: number
  travelerIds: string[]
  status: string
  createdAt: string
  confirmedAt: string | null
  reviewedByOrganizerUserId: string | null
  reviewNote: string | null
}

export type GroupSelectionOrderLinkResponse = {
  selectionId: string
  orderId: string
  createdAt: string
}

export type GroupSelectionOrderProjectionResponse = {
  selectionId: string
  orderId: string
  orderStatus: string
  paymentStatus: string
  supplierReviewStatus: string
  refundStatus: string | null
  bookingSummaryLabel: string
}

export type TourGroupChatSettingsResponse = {
  groupId: string
  allowMemberDirectChat: boolean
  updatedAt: string
  updatedByUserId: string
  canUpdate: boolean
}

export type TourGroupConversationSummaryResponse = {
  conversationId: string
  conversationType: string
  status: string
  counterpartUserId: string | null
  counterpartDisplayName: string | null
  counterpartAvatarUrl: string | null
  conversationTitle: string
  participantsSummary: string
  lastMessagePreview: string | null
  lastMessageAt: string | null
  unreadCount: number
  isMuted: boolean
  isArchived: boolean
  canSendMessage: boolean
}

export type TourGroupConversationListResponse = {
  conversations: TourGroupConversationSummaryResponse[]
  groupChatConversationId: string | null
}

export type TourGroupMessageAttachmentResponse = {
  attachmentId: string
  attachmentType: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}

export type TourGroupUploadedAttachmentResponse = {
  attachmentId: string
  attachmentType: string
  publicUrl: string
  storagePath: string
  originalFileName: string
  mimeType: string
  fileSize: number
  sortOrder: number
  createdAt: string
}

export type TourGroupMessageReactionResponse = {
  reactionType: string
  count: number
  reactedByCurrentUser: boolean
}

export type TourGroupMessageResponse = {
  messageId: string
  conversationId: string
  messageType: string
  senderUserId: string
  senderDisplayName: string
  senderAvatarUrl: string | null
  content: string
  replyToMessageId: string | null
  replyToPreview: string | null
  status: string
  createdAt: string
  updatedAt: string
  attachments: TourGroupMessageAttachmentResponse[]
  reactions: TourGroupMessageReactionResponse[]
  canEdit: boolean
  canDelete: boolean
  canRecall: boolean
  canReact: boolean
  isMine: boolean
}

export type TourGroupMessageListResponse = {
  messages: TourGroupMessageResponse[]
}

export type TourGroupMessageSearchResultResponse = {
  conversationId: string
  conversationTitle: string
  message: TourGroupMessageResponse
}

export type TourGroupMessageSearchResponse = {
  results: TourGroupMessageSearchResultResponse[]
}

export type TourGroupDetailsResponse = {
  group: TourGroupSummaryResponse
  memberships: TourGroupMembershipResponse[]
  membershipTravelers: TourGroupMembershipTravelerResponse[]
  planItems: GroupPlanItemResponse[]
  planOptions: GroupPlanOptionResponse[]
  selections: GroupPlanSelectionResponse[]
  selectionOrderLinks: GroupSelectionOrderLinkResponse[]
  selectionOrderProjections: GroupSelectionOrderProjectionResponse[]
  bookings: OrderResponse[]
}

export type TourGroupListResponse = {
  groups: TourGroupSummaryResponse[]
}

export type TourGroupPaySelectionResponse = {
  group: TourGroupDetailsResponse
  orders: OrderResponse[]
}

export type ManagerSessionResponse = {
  managerId: string
  managerType: string
  email: string
  displayName: string
  status: string
  scopeId: string
  createdAt: string
}

export type CurrentManagerSessionResponse = {
  managerId: string
  managerType: string
  email: string
  displayName: string
  status: string
  scopeId: string
  createdAt: string
  expiresAt: string
}

export type ManagerTaskResponse = {
  orderId: string
  orderItemId: string
  buyerUserId: string
  taskType: string
  supplierReviewStatus: string
  summaryLabel: string
  detailLabel: string
  requestedAt: string
  reviewDecision: SupplierReviewDecisionResponse | null
  reviewedBy: string | null
  reviewedAt: string | null
  reviewNote: string | null
}

export type ManagerTaskListResponse = {
  tasks: ManagerTaskResponse[]
}

export type ManagerBatchDecisionResponse = {
  processedCount: number
  orderItemIds: string[]
  action: string
}

export type ManagerRefundTaskResponse = {
  orderId: string
  buyerUserId: string
  taskType: string
  summaryLabel: string
  refundId: string
  refundReason: string
  refundAmount: string
  refundCurrency: string
  requestedAt: string
}

export type ManagerRefundTaskListResponse = {
  tasks: ManagerRefundTaskResponse[]
}

export type AppViewKey =
  | 'blog'
  | 'reviews'
  | 'explore'
  | 'account'
  | 'travelers'
  | 'flights'
  | 'hotels'
  | 'trains'
  | 'attractions'
  | 'tourGroups'
  | 'bookings'
  | 'manager'
  | 'trainAdmin'
  | 'attractionAdmin'

export type AppNotice = {
  id: number
  kind: 'success' | 'error' | 'info'
  title: string
  description: string
  technicalMessage?: string
}
