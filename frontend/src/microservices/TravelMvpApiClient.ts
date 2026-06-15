// 本文件汇总前端可调用的后端 Planner 接口。
// 下方按业务域分组导出。
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

// 广告相关 API。
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

// 内容 / 文档相关 API。
import { listBlogPosts, listShortBlogPosts, listBlogSuggestions, uploadBlogImage, getBlogPost, listBlogModerationPosts, approveBlogPost, rejectBlogPost, saveBlogDraft, publishBlogPost, createBlogPost, updateBlogPost, archiveBlogPost, addBlogComment, deleteBlogComment, likeBlogPost, unlikeBlogPost, likeBlogComment, unlikeBlogComment, favoriteBlogPost, unfavoriteBlogPost, followBlogUser, blockBlogUser, listBlogNotifications, getBlogProfile, updateBlogProfilePrivacy, listBlogFollowers, listBlogFollowing } from '@/microservices/blog/api'
import { listExploreSuggestions, searchExplore } from '@/microservices/content/api/ExplorePlannerSupport'
import { ensureOrderCancellationThread, listMyFeedbackThreads, listManagerFeedbackThreads, listSiteAdminFeedbackThreads, sendFeedbackMessage, createOrderCancellationMessage, handleOrderCancellationRequest, markFeedbackThreadRead, escalateFeedbackThread, createFeedbackComplaint, openComplaintManagerThread } from '@/microservices/feedback/api'
import { listMyReviews, listReviewsByResource, getReviewResourceSummary, getReviewEligibility, createReview, updateReview, uploadReviewImage, deleteReview } from '@/microservices/content/api/ReviewPlannerSupport'

// 航班、酒店、订单、旅游团和出行相关 API。
import { searchFlightsPlanner, flightDailyLowestPricesPlanner, getFlightDetailsPlanner, bookFlightPlanner, flightSuggestionsPlanner } from '@/microservices/flight/api'
import { searchHotelsPlanner, getHotelDetailsPlanner, hotelSuggestionsPlanner, uploadHotelRoomTypeImage } from '@/microservices/hotel/api'
import { createUser } from '@/microservices/identity/api/CreateUserPlanner'
import { loginUser } from '@/microservices/identity/api/LoginPlanner'
import { getUser } from '@/microservices/identity/api/GetUserPlanner'
import { uploadUserAvatar } from '@/microservices/identity/api/UploadUserAvatarPlanner'
import { updateUserProfile } from '@/microservices/identity/api/UpdateUserProfilePlanner'
import { registerAirlineManager, registerHotelManager, registerSiteAdmin, updateSiteAdminManagerProfile, registerRailwayManager, registerAttractionManager, listManagedAttractions, createAttraction, createAttractionTicketType, createAttractionTicketSession, createAttractionTicketRule, listManagedTrains, createTrainJourney, createManagerRoomType, listManagedHotels, listManagerTasks, batchConfirmManagerBookingItems, batchRejectManagerBookingItems, listManagerFlights, listManagerFlightOrders, listManagerRefundTasks, updateAirlineManagerProfile, updateHotelManagerProfile, createManagerFlight, toggleManagerFlightStatus, confirmManagerBookingItem, rejectManagerBookingItem } from '@/microservices/operations/api/ManagerPlanners'
import { createPaymentLink } from '@/microservices/order/api/FindOrderPaymentPlanner'
import { createOrder } from '@/microservices/order/api/CreateOrderPlanner'
import { getOrder } from '@/microservices/order/api/GetOrderPlanner'
import { listOrders } from '@/microservices/order/api/ListOrdersPlanner'
import { payOrder } from '@/microservices/order/api/PayOrderPlanner'
import { cancelOrder } from '@/microservices/order/api/CancelOrderPlanner'
import { requestRefund } from '@/microservices/order/api/RequestRefundPlanner'
import { approveRefund } from '@/microservices/operations/api/ApproveManagerRefundPlanner'
import { rejectRefund } from '@/microservices/operations/api/RejectManagerRefundPlanner'
import { addTrainItemToOrder } from '@/microservices/train/api/BookTrainItemPlanner'
import { addAttractionItemToOrder } from '@/microservices/attraction/api/BookAttractionItemPlanner'
import { bookHotelPlanner } from '@/microservices/hotel/api/BookHotelPlanner'
import { createTourGroup, uploadTourGroupCoverImage, listTourGroups, getTourGroup, joinTourGroup, leaveTourGroup, addTourGroupMembershipTraveler, removeTourGroupMembershipTraveler, kickTourGroupMember, blacklistTourGroupMember, transferTourGroupLeader, createTourGroupPlanItem, createTourGroupPlanOption, createTourGroupSelection, submitTourGroupSelection, confirmTourGroupSelection, rejectTourGroupSelection, payTourGroupSelection, batchPayTourGroupSelections, batchConfirmTourGroupSelections, batchRejectTourGroupSelections, listTourGroupBookings, getTourGroupChatSettings, updateTourGroupChatSettings, listTourGroupChatMessages, sendTourGroupChatMessage, listTourGroupDirectConversations, listTourGroupConversations, searchTourGroupConversations, searchTourGroupMessages, getOrCreateTourGroupDirectConversation, listDirectConversationMessages, listConversationMessages, markConversationRead, uploadConversationAttachment, sendConversationMessage, sendDirectConversationMessage, editConversationMessage, deleteConversationMessage, recallConversationMessage, addConversationReaction, removeConversationReaction, updateDirectConversationMuteState, updateDirectConversationArchiveState } from '@/microservices/tour-group/api/TourGroupPlannerSupport'
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
  createUser,
  loginUser,
  getUser,
  uploadUserAvatar,
  updateUserProfile,
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
  createOrder,
  addTrainItemToOrder,
  addAttractionItemToOrder,
  bookHotelPlanner,
  getOrder,
  listOrders,
  payOrder,
  cancelOrder,
  requestRefund,
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






