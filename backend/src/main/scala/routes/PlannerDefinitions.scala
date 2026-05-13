package com.typesafe.travel.api.routes

import com.typesafe.travel.advertising.domain.*
import com.typesafe.travel.api.{ExploreSearchPlanner, ExploreSuggestionsPlanner}
import com.typesafe.travel.attraction.domain.{AttractionSuggestionsPlanner, CreateAttractionPlanner, GetAttractionDetailsPlanner, ListAttractionsPlanner, ListManagedAttractionsPlanner}
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.flight.domain.{BookFlightPlanner, FlightSuggestionsPlanner, GetFlightDetailsPlanner, SearchFlightsPlanner}
import com.typesafe.travel.hotel.domain.{BookHotelPlanner, GetHotelDetailsPlanner, HotelSuggestionsPlanner, SearchHotelsPlanner}
import com.typesafe.travel.identity.domain.{CreateUserPlanner, GetUserPlanner, LoginUserPlanner, UploadUserAvatarPlanner}
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.tourgroup.domain.*
import com.typesafe.travel.traveler.domain.*
import com.typesafe.travel.train.domain.*

object PlannerDefinitions:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val orderPlanners: PlannerRegistry =
    val registeredPlanners: List[PlannerRegistry.RegisteredPlan] =
      List(
        WithConnection(ListAdvertisementsPlanner),
        WithConnection(CreateAdvertisementPlanner),
        WithConnection(UpdateAdvertisementPlanner),
        WithConnection(SubmitAdvertisementForReviewPlanner),
        WithConnection(PauseAdvertisementPlanner),
        WithConnection(ApproveAdvertisementPlanner),
        WithConnection(RejectAdvertisementPlanner),
        WithConnection(AssignAdvertisementSlotPlanner),
        WithConnection(UploadAdvertisementImagePlanner),
        WithConnection(FlightSuggestionsPlanner),
        WithConnection(SearchFlightsPlanner),
        WithConnection(GetFlightDetailsPlanner),
        WithConnection(BookFlightPlanner),
        WithConnection(HotelSuggestionsPlanner),
        WithConnection(SearchHotelsPlanner),
        WithConnection(GetHotelDetailsPlanner),
        WithConnection(BookHotelPlanner),
        WithConnection(AttractionSuggestionsPlanner),
        WithConnection(ListAttractionsPlanner),
        WithConnection(GetAttractionDetailsPlanner),
        WithConnection(ListManagedAttractionsPlanner),
        WithConnection(CreateAttractionPlanner),
        WithConnection(SignupPlanner),
        WithConnection(LoginPlanner),
        WithConnection(CurrentUserPlanner),
        WithConnection(LogoutPlanner),
        WithConnection(LogoutOtherSessionsPlanner),
        WithConnection(ListAuthSessionsPlanner),
        WithConnection(ChangePasswordPlanner),
        WithConnection(BlogSuggestionsPlanner),
        WithConnection(ListBlogPostsPlanner),
        WithConnection(GetBlogPostPlanner),
        WithConnection(CreateBlogPostPlanner),
        WithConnection(UpdateBlogPostPlanner),
        WithConnection(ApproveBlogPostPlanner),
        WithConnection(RejectBlogPostPlanner),
        WithConnection(ArchiveBlogPostPlanner),
        WithConnection(AddBlogCommentPlanner),
        WithConnection(DeleteBlogCommentPlanner),
        WithConnection(LikeBlogPostPlanner),
        WithConnection(UnlikeBlogPostPlanner),
        WithConnection(ListMyReviewsPlanner),
        WithConnection(ListReviewsByResourcePlanner),
        WithConnection(GetReviewSummaryPlanner),
        WithConnection(CheckReviewEligibilityPlanner),
        WithConnection(CreateReviewPlanner),
        WithConnection(UpdateReviewPlanner),
        WithConnection(DeleteReviewPlanner),
        WithConnection(UploadReviewImagePlanner),
        WithConnection(ListFeedbackThreadsPlanner),
        WithConnection(EnsureReviewFeedbackThreadPlanner),
        WithConnection(SendFeedbackMessagePlanner),
        WithConnection(MarkFeedbackThreadReadPlanner),
        WithConnection(EscalateFeedbackThreadPlanner),
        WithConnection(ExploreSuggestionsPlanner),
        WithConnection(ExploreSearchPlanner),
        WithConnection(CreateUserPlanner),
        WithConnection(LoginUserPlanner),
        WithConnection(GetUserPlanner),
        WithConnection(UploadUserAvatarPlanner),
        WithConnection(ManagerLoginPlanner),
        WithConnection(CurrentManagerPlanner),
        WithConnection(ManagerLogoutPlanner),
        WithConnection(ManagerLogoutOtherSessionsPlanner),
        WithConnection(ListManagerSessionsPlanner),
        WithConnection(ChangeManagerPasswordPlanner),
        WithConnection(RegisterAirlineManagerPlanner),
        WithConnection(RegisterHotelManagerPlanner),
        WithConnection(RegisterSiteAdminPlanner),
        WithConnection(ListManagerTasksPlanner),
        WithConnection(BatchConfirmManagerTasksPlanner),
        WithConnection(BatchRejectManagerTasksPlanner),
        WithConnection(ListManagerFlightsPlanner),
        WithConnection(ListManagerHotelsPlanner),
        WithConnection(ListManagerRefundTasksPlanner),
        WithConnection(CreateManagerFlightPlanner),
        WithConnection(CreateManagerRoomTypePlanner),
        WithConnection(ConfirmManagerBookingItemPlanner),
        WithConnection(RejectManagerBookingItemPlanner),
        WithConnection(ApproveManagerRefundPlanner),
        WithConnection(RejectManagerRefundPlanner),
        WithConnection(ListOrdersPlanner),
        WithConnection(CreateOrderPlanner),
        WithConnection(GetOrderPlanner),
        WithConnection(CreatePaymentLinkPlanner),
        WithConnection(SubmitOrderPlanner),
        WithConnection(PayOrderPlanner),
        WithConnection(CancelOrderPlanner),
        WithConnection(RequestRefundPlanner),
        WithConnection(ApproveRefundPlanner),
        WithConnection(SettleRefundPlanner),
        WithConnection(FindOrderPaymentPlanner),
        WithConnection(CreateTravelerPlanner),
        WithConnection(UpdateTravelerPlanner),
        WithConnection(ListTravelersPlanner),
        WithConnection(DeleteTravelerPlanner),
        WithConnection(TrainSuggestionsPlanner),
        WithConnection(RegisterRailwayManagerPlanner),
        WithConnection(ListManagedTrainsPlanner),
        WithConnection(CreateTrainJourneyPlanner),
        WithConnection(SearchTrainsPlanner),
        WithConnection(GetTrainDetailsPlanner),
        WithConnection(BookTrainItemPlanner),
        WithConnection(CreateTourGroupPlanner),
        WithConnection(ListTourGroupsPlanner),
        WithConnection(GetTourGroupDetailsPlanner),
        WithConnection(JoinTourGroupPlanner),
        WithConnection(AddMembershipTravelerPlanner)
      )

    PlannerRegistry(
      registeredPlanners.map(planner => planner.name -> planner).toMap
    )
