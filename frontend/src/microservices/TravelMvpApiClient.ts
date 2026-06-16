import { approveAdvertisement } from '@/microservices/advertising/api/ApproveAdvertisementPlanner'
import { assignAdvertisementSlot } from '@/microservices/advertising/api/AssignAdvertisementSlotPlanner'
import { getAdvertisementDeliverySettings } from '@/microservices/advertising/api/GetAdvertisementDeliverySettingsPlanner'
import { saveAdvertisementDeliverySettings } from '@/microservices/advertising/api/SaveAdvertisementDeliverySettingsPlanner'
import { createAdvertisement } from '@/microservices/advertising/api/CreateAdvertisementPlanner'
import { generateAdvertisementImageCandidates } from '@/microservices/advertising/api/GenerateAdvertisementImageCandidatesPlanner'
import { generateAdvertisementTextCandidates } from '@/microservices/advertising/api/GenerateAdvertisementTextCandidatesPlanner'
import { listMyAdvertisements, listPendingAdvertisements, listReviewedAdvertisements, listDeliverableAdvertisements } from '@/microservices/advertising/api/ListAdvertisementsPlanner'
import { pauseAdvertisement } from '@/microservices/advertising/api/PauseAdvertisementPlanner'
import { pauseAdvertisementDisplay } from '@/microservices/advertising/api/PauseAdvertisementDisplayPlanner'
import { rejectAdvertisement } from '@/microservices/advertising/api/RejectAdvertisementPlanner'
import { submitAdvertisementForReview } from '@/microservices/advertising/api/SubmitAdvertisementForReviewPlanner'
import { updateAdvertisement } from '@/microservices/advertising/api/UpdateAdvertisementPlanner'
import { uploadAdvertisementImage } from '@/microservices/advertising/api/UploadAdvertisementImagePlanner'

import { listAttractions } from '@/microservices/attraction/api/ListAttractionsPlanner'
import { getAttraction } from '@/microservices/attraction/api/GetAttractionDetailsPlanner'
import { uploadAttractionImage } from '@/microservices/attraction/api/UploadAttractionImagePlanner'

import { getHealth } from '@/microservices/auth/api/GetHealthPlanner'
import { signupUser } from '@/microservices/auth/api/SignupPlanner'
import { loginUserWithPassword } from '@/microservices/auth/api/LoginPlanner'
import { logoutUser } from '@/microservices/auth/api/LogoutPlanner'
import { getCurrentUserSession } from '@/microservices/auth/api/CurrentUserPlanner'
import { changeUserPassword } from '@/microservices/auth/api/ChangePasswordPlanner'
import { listUserSessions } from '@/microservices/auth/api/ListAuthSessionsPlanner'
import { logoutCurrentUserSession } from '@/microservices/auth/api/LogoutPlanner'
import { logoutOtherUserSessions } from '@/microservices/auth/api/LogoutOtherSessionsPlanner'
import { loginManagerAuth } from '@/microservices/auth/api/ManagerLoginPlanner'
import { logoutManagerAuth } from '@/microservices/auth/api/ManagerLogoutPlanner'
import { getCurrentManagerSession } from '@/microservices/auth/api/CurrentManagerPlanner'
import { changeManagerPassword } from '@/microservices/auth/api/ChangeManagerPasswordPlanner'
import { listManagerSessions } from '@/microservices/auth/api/ListManagerSessionsPlanner'
import { logoutCurrentManagerSession } from '@/microservices/auth/api/ManagerLogoutPlanner'
import { logoutOtherManagerSessions } from '@/microservices/auth/api/ManagerLogoutOtherSessionsPlanner'

import { listBlogPosts, listShortBlogPosts, listBlogSuggestions, uploadBlogImage, getBlogPost, listBlogModerationPosts, approveBlogPost, rejectBlogPost, saveBlogDraft, publishBlogPost, createBlogPost, updateBlogPost, archiveBlogPost, addBlogComment, deleteBlogComment, likeBlogPost, unlikeBlogPost, likeBlogComment, unlikeBlogComment, favoriteBlogPost, unfavoriteBlogPost, followBlogUser, blockBlogUser, listBlogNotifications, getBlogProfile, updateBlogProfilePrivacy, listBlogFollowers, listBlogFollowing } from '@/microservices/blog/api'
import { listExploreSuggestions } from '@/microservices/content/api/ExploreSuggestionsPlanner'
import { searchExplore } from '@/microservices/content/api/ExploreSearchPlanner'
import { ensureOrderCancellationThread } from '@/microservices/feedback/api/EnsureOrderCancellationThreadPlanner'
import { listMyFeedbackThreads, listManagerFeedbackThreads, listSiteAdminFeedbackThreads } from '@/microservices/feedback/api/ListFeedbackThreadsPlanner'
import { sendFeedbackMessage } from '@/microservices/feedback/api/SendFeedbackMessagePlanner'
import { createOrderCancellationMessage } from '@/microservices/feedback/api/CreateOrderCancellationMessagePlanner'
import { handleOrderCancellationRequest } from '@/microservices/feedback/api/HandleOrderCancellationRequestPlanner'
import { markFeedbackThreadRead } from '@/microservices/feedback/api/MarkFeedbackThreadReadPlanner'
import { escalateFeedbackThread } from '@/microservices/feedback/api/EscalateFeedbackThreadPlanner'
import { createFeedbackComplaint } from '@/microservices/feedback/api/CreateFeedbackComplaintPlanner'
import { openComplaintManagerThread } from '@/microservices/feedback/api/OpenComplaintManagerThreadPlanner'
import { listMyReviews } from '@/microservices/content/api/ListMyReviewsPlanner'
import { listReviewsByResource } from '@/microservices/content/api/ListReviewsByResourcePlanner'
import { getReviewResourceSummary } from '@/microservices/content/api/GetReviewSummaryPlanner'
import { getReviewEligibility } from '@/microservices/content/api/CheckReviewEligibilityPlanner'
import { createReview } from '@/microservices/content/api/CreateReviewPlanner'
import { updateReview } from '@/microservices/content/api/UpdateReviewPlanner'
import { uploadReviewImage } from '@/microservices/content/api/UploadReviewImagePlanner'
import { deleteReview } from '@/microservices/content/api/DeleteReviewPlanner'

import { searchFlightsPlanner } from '@/microservices/flight/api/SearchFlightsPlanner'
import { flightDailyLowestPricesPlanner } from '@/microservices/flight/api/FlightDailyLowestPricesPlanner'
import { getFlightDetailsPlanner } from '@/microservices/flight/api/GetFlightDetailsPlanner'
import { bookFlightPlanner } from '@/microservices/flight/api/BookFlightPlanner'
import { flightSuggestionsPlanner } from '@/microservices/flight/api/FlightSuggestionsPlanner'
import { searchHotelsPlanner } from '@/microservices/hotel/api/SearchHotelsPlanner'
import { getHotelDetailsPlanner } from '@/microservices/hotel/api/GetHotelDetailsPlanner'
import { hotelSuggestionsPlanner } from '@/microservices/hotel/api/HotelSuggestionsPlanner'
import { uploadHotelRoomTypeImage } from '@/microservices/hotel/api/UploadHotelRoomTypeImagePlanner'
import { bookHotelPlanner } from '@/microservices/hotel/api/BookHotelPlanner'
import { createUserPlanner } from '@/microservices/identity/api/CreateUserPlanner'
import { loginPlanner } from '@/microservices/identity/api/LoginPlanner'
import { getUserPlanner } from '@/microservices/identity/api/GetUserPlanner'
import { uploadUserAvatarPlanner } from '@/microservices/identity/api/UploadUserAvatarPlanner'
import { updateUserProfilePlanner } from '@/microservices/identity/api/UpdateUserProfilePlanner'
import { batchConfirmManagerBookingItems } from '@/microservices/operations/overall/api/BatchConfirmManagerTasksPlanner'
import { batchRejectManagerBookingItems } from '@/microservices/operations/overall/api/BatchRejectManagerTasksPlanner'
import { confirmManagerBookingItem } from '@/microservices/operations/overall/api/ConfirmManagerBookingItemPlanner'
import { rejectManagerBookingItem } from '@/microservices/operations/overall/api/RejectManagerBookingItemPlanner'
import { approveRefund } from '@/microservices/operations/overall/api/ApproveManagerRefundPlanner'
import { rejectRefund } from '@/microservices/operations/overall/api/RejectManagerRefundPlanner'
import { listManagerRefundTasks } from '@/microservices/operations/overall/api/ListManagerRefundTasksPlanner'
import { listManagerTasks } from '@/microservices/operations/overall/api/ListManagerTasksPlanner'
import { createManagerFlight } from '@/microservices/operations/airline/api/CreateManagerFlightPlanner'
import { listManagerFlights } from '@/microservices/operations/airline/api/ListManagerFlightsPlanner'
import { listManagerFlightOrders } from '@/microservices/operations/airline/api/ListManagerFlightOrdersPlanner'
import { registerAirlineManager } from '@/microservices/operations/airline/api/RegisterAirlineManagerPlanner'
import { toggleManagerFlightStatus } from '@/microservices/operations/airline/api/ToggleManagerFlightStatusPlanner'
import { updateAirlineManagerProfile } from '@/microservices/operations/airline/api/UpdateAirlineManagerProfilePlanner'
import { createManagerRoomType } from '@/microservices/operations/hotel/api/CreateManagerRoomTypePlanner'
import { listManagedHotels } from '@/microservices/operations/hotel/api/ListManagerHotelsPlanner'
import { registerHotelManager } from '@/microservices/operations/hotel/api/RegisterHotelManagerPlanner'
import { updateHotelManagerProfile } from '@/microservices/operations/hotel/api/UpdateHotelManagerProfilePlanner'
import { registerAttractionManager } from '@/microservices/operations/attraction/api/RegisterAttractionManagerPlanner'
import { listManagedAttractions } from '@/microservices/attraction/api/ListManagedAttractionsPlanner'
import { createAttraction } from '@/microservices/attraction/api/CreateAttractionPlanner'
import { createAttractionTicketType } from '@/microservices/attraction/api/CreateAttractionTicketTypePlanner'
import { createAttractionTicketSession } from '@/microservices/attraction/api/CreateAttractionTicketSessionPlanner'
import { createAttractionTicketRule } from '@/microservices/attraction/api/CreateAttractionTicketRulePlanner'
import { createTrainJourney } from '@/microservices/train/api/CreateTrainJourneyPlanner'
import { listManagedTrains } from '@/microservices/train/api/ListManagedTrainsPlanner'
import { registerRailwayManager } from '@/microservices/train/api/RegisterRailwayManagerPlanner'
import { registerSiteAdmin } from '@/microservices/operations/siteadmin/api/RegisterSiteAdminPlanner'
import { updateSiteAdminManagerProfile } from '@/microservices/operations/siteadmin/api/UpdateSiteAdminManagerProfilePlanner'
import { createPaymentLink } from '@/microservices/order/api/CreatePaymentLinkPlanner'
import { findOrderPayment } from '@/microservices/order/api/FindOrderPaymentPlanner'
import { createOrder } from '@/microservices/order/api/CreateOrderPlanner'
import { getOrder } from '@/microservices/order/api/GetOrderPlanner'
import { listOrders } from '@/microservices/order/api/ListOrdersPlanner'
import { payOrder } from '@/microservices/order/api/PayOrderPlanner'
import { cancelOrder } from '@/microservices/order/api/CancelOrderPlanner'
import { requestRefund } from '@/microservices/order/api/RequestRefundPlanner'
import { submitOrder } from '@/microservices/order/api/SubmitOrderPlanner'
import { settleRefund } from '@/microservices/order/api/SettleRefundPlanner'
import { addTrainItemToOrder } from '@/microservices/train/api/BookTrainItemPlanner'
import { addAttractionItemToOrder } from '@/microservices/attraction/api/BookAttractionItemPlanner'
import { createTourGroup } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { uploadTourGroupCoverImage } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { listTourGroups } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { getTourGroup } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { joinTourGroup } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { leaveTourGroup } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { addTourGroupMembershipTraveler } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { removeTourGroupMembershipTraveler } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { kickTourGroupMember } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { blacklistTourGroupMember } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { transferTourGroupLeader } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { createTourGroupPlanItem } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { createTourGroupPlanOption } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { createTourGroupSelection } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { submitTourGroupSelection } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { confirmTourGroupSelection } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { rejectTourGroupSelection } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { payTourGroupSelection } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { batchPayTourGroupSelections } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { batchConfirmTourGroupSelections } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { batchRejectTourGroupSelections } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { listTourGroupBookings } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { getTourGroupChatSettings } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { updateTourGroupChatSettings } from '@/microservices/tour-group/api/TourGroupPlannerSupportPlanning'
import { listTourGroupChatMessages } from '@/microservices/tour-group/api/TourGroupPlannerSupportChat'
import { sendTourGroupChatMessage } from '@/microservices/tour-group/api/TourGroupPlannerSupportChat'
import { listTourGroupDirectConversations } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { listTourGroupConversations } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { searchTourGroupConversations } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { searchTourGroupMessages } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { getOrCreateTourGroupDirectConversation } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { listDirectConversationMessages } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { listConversationMessages } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { markConversationRead } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { uploadConversationAttachment } from '@/microservices/tour-group/api/TourGroupPlannerSupportAttachment'
import { sendConversationMessage } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { sendDirectConversationMessage } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { editConversationMessage } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { deleteConversationMessage } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { recallConversationMessage } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { addConversationReaction } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { removeConversationReaction } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { updateDirectConversationMuteState } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { updateDirectConversationArchiveState } from '@/microservices/tour-group/api/TourGroupPlannerSupportConversation'
import { listTrains } from '@/microservices/train/api/SearchTrainsPlanner'
import { getTrain } from '@/microservices/train/api/GetTrainDetailsPlanner'
import { createTraveler } from '@/microservices/traveler/api/CreateTravelerPlanner'
import { updateTraveler } from '@/microservices/traveler/api/UpdateTravelerPlanner'
import { listTravelers } from '@/microservices/traveler/api/ListTravelersPlanner'
import { deleteTraveler } from '@/microservices/traveler/api/DeleteTravelerPlanner'
export const travelMvpApiClient = {
  approveAdvertisement,
  assignAdvertisementSlot,
  getAdvertisementDeliverySettings,
  saveAdvertisementDeliverySettings,
  createAdvertisement,
  generateAdvertisementImageCandidates,
  generateAdvertisementTextCandidates,
  listMyAdvertisements,
  listPendingAdvertisements,
  listReviewedAdvertisements,
  listDeliverableAdvertisements,
  pauseAdvertisement,
  pauseAdvertisementDisplay,
  rejectAdvertisement,
  submitAdvertisementForReview,
  updateAdvertisement,
  uploadAdvertisementImage,
  listAttractions,
  getAttraction,
  uploadAttractionImage,
  getHealth,
  signupUser,
  loginUserWithPassword,
  logoutUser,
  getCurrentUserSession,
  changeUserPassword,
  listUserSessions,
  logoutCurrentUserSession,
  logoutOtherUserSessions,
  loginManagerAuth,
  logoutManagerAuth,
  getCurrentManagerSession,
  changeManagerPassword,
  listManagerSessions,
  logoutCurrentManagerSession,
  logoutOtherManagerSessions,
  listBlogPosts,
  listShortBlogPosts,
  listBlogSuggestions,
  uploadBlogImage,
  getBlogPost,
  listBlogModerationPosts,
  approveBlogPost,
  rejectBlogPost,
  saveBlogDraft,
  publishBlogPost,
  createBlogPost,
  updateBlogPost,
  archiveBlogPost,
  addBlogComment,
  deleteBlogComment,
  likeBlogPost,
  unlikeBlogPost,
  likeBlogComment,
  unlikeBlogComment,
  favoriteBlogPost,
  unfavoriteBlogPost,
  followBlogUser,
  blockBlogUser,
  listBlogNotifications,
  getBlogProfile,
  updateBlogProfilePrivacy,
  listBlogFollowers,
  listBlogFollowing,
  listExploreSuggestions,
  searchExplore,
  ensureOrderCancellationThread,
  listMyFeedbackThreads,
  listManagerFeedbackThreads,
  listSiteAdminFeedbackThreads,
  sendFeedbackMessage,
  createOrderCancellationMessage,
  handleOrderCancellationRequest,
  markFeedbackThreadRead,
  escalateFeedbackThread,
  createFeedbackComplaint,
  openComplaintManagerThread,
  listMyReviews,
  listReviewsByResource,
  getReviewResourceSummary,
  getReviewEligibility,
  createReview,
  updateReview,
  uploadReviewImage,
  deleteReview,
  searchFlightsPlanner,
  flightDailyLowestPricesPlanner,
  getFlightDetailsPlanner,
  bookFlightPlanner,
  flightSuggestionsPlanner,
  searchHotelsPlanner,
  getHotelDetailsPlanner,
  hotelSuggestionsPlanner,
  uploadHotelRoomTypeImage,
  createUserPlanner,
  loginPlanner,
  getUserPlanner,
  uploadUserAvatarPlanner,
  updateUserProfilePlanner,
  registerAirlineManager,
  registerHotelManager,
  registerSiteAdmin,
  updateSiteAdminManagerProfile,
  registerRailwayManager,
  registerAttractionManager,
  listManagedAttractions,
  createAttraction,
  createAttractionTicketType,
  createAttractionTicketSession,
  createAttractionTicketRule,
  listManagedTrains,
  createTrainJourney,
  createManagerRoomType,
  listManagedHotels,
  listManagerTasks,
  batchConfirmManagerBookingItems,
  batchRejectManagerBookingItems,
  listManagerFlights,
  listManagerFlightOrders,
  listManagerRefundTasks,
  updateAirlineManagerProfile,
  updateHotelManagerProfile,
  createManagerFlight,
  toggleManagerFlightStatus,
  confirmManagerBookingItem,
  rejectManagerBookingItem,
  createPaymentLink,
  findOrderPayment,
  createOrder,
  addTrainItemToOrder,
  addAttractionItemToOrder,
  bookHotelPlanner,
  getOrder,
  listOrders,
  payOrder,
  cancelOrder,
  requestRefund,
  submitOrder,
  settleRefund,
  approveRefund,
  rejectRefund,
  createTourGroup,
  uploadTourGroupCoverImage,
  listTourGroups,
  getTourGroup,
  joinTourGroup,
  leaveTourGroup,
  addTourGroupMembershipTraveler,
  removeTourGroupMembershipTraveler,
  kickTourGroupMember,
  blacklistTourGroupMember,
  transferTourGroupLeader,
  createTourGroupPlanItem,
  createTourGroupPlanOption,
  createTourGroupSelection,
  submitTourGroupSelection,
  confirmTourGroupSelection,
  rejectTourGroupSelection,
  payTourGroupSelection,
  batchPayTourGroupSelections,
  batchConfirmTourGroupSelections,
  batchRejectTourGroupSelections,
  listTourGroupBookings,
  getTourGroupChatSettings,
  updateTourGroupChatSettings,
  listTourGroupChatMessages,
  sendTourGroupChatMessage,
  listTourGroupDirectConversations,
  listTourGroupConversations,
  searchTourGroupConversations,
  searchTourGroupMessages,
  getOrCreateTourGroupDirectConversation,
  listDirectConversationMessages,
  listConversationMessages,
  markConversationRead,
  uploadConversationAttachment,
  sendConversationMessage,
  sendDirectConversationMessage,
  editConversationMessage,
  deleteConversationMessage,
  recallConversationMessage,
  addConversationReaction,
  removeConversationReaction,
  updateDirectConversationMuteState,
  updateDirectConversationArchiveState,
  listTrains,
  getTrain,
  createTraveler,
  updateTraveler,
  listTravelers,
  deleteTraveler,
}









