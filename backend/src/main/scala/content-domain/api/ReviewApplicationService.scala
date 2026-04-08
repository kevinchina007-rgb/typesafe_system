package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.api.storage.{ContentImageCollection, ContentImageStorage}
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.identity.domain.{User, UserError, UserRepository}
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.Instant

// Review application service 负责把“严格绑定订单项”的 review core data
// 组装成资源摘要、资格判断和可展示的 review 视图。
enum ReviewImageUploadError(val message: String) extends DomainError:
  case ImageWasMissing
      extends ReviewImageUploadError("Review image file was missing")
  case ImageFileTypeWasInvalid(contentTypeValue: String)
      extends ReviewImageUploadError(s"Review image content type '$contentTypeValue' is not supported")
  case ImageFileExtensionWasInvalid(fileNameValue: String)
      extends ReviewImageUploadError(s"Review image file '$fileNameValue' is not supported")
  case ImageFileWasTooLarge(maximumBytes: Long, actualBytes: Long)
      extends ReviewImageUploadError(s"Review image exceeded the maximum size of $maximumBytes bytes with $actualBytes bytes")

final case class ResourceReviewSummaryView(
    resourceType: ReviewResourceType,
    resourceId: String,
    averageRating: BigDecimal,
    reviewCount: Int
)

final case class ReviewView(
    reviewId: ReviewId,
    authorUserId: UserId,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    resourceType: String,
    resourceId: String,
    resourceSummaryTitle: String,
    resourceSummarySubtitle: String,
    orderId: OrderId,
    orderItemId: OrderItemId,
    rating: Int,
    title: String,
    content: String,
    status: String,
    createdAt: Instant,
    updatedAt: Instant,
    isMyReview: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    imageRefs: List[ReviewImageRef]
)

// 是否可评价最终以后端裁决为准，前端只消费这个结果。
final case class ReviewEligibilityView(
    orderId: OrderId,
    orderItemId: OrderItemId,
    canReview: Boolean,
    alreadyReviewed: Boolean,
    reason: Option[String],
    resourceSummaryTitle: String
)

// 这是 application 层内部的桥接模型：
// 从 order item snapshot 解析出 review 需要的资源主键和展示摘要。
private final case class ReviewBinding(
    resourceType: ReviewResourceType,
    resourceId: String,
    resourceSummaryTitle: String,
    resourceSummarySubtitle: String
)

trait ReviewApplicationService[F[_]]:
  def listMyReviews(authorUserId: UserId): F[List[ReviewView]]
  def listReviewsByResource(viewerUserId: UserId, resourceType: ReviewResourceType, resourceId: String): F[List[ReviewView]]
  def getResourceSummary(viewerUserId: UserId, resourceType: ReviewResourceType, resourceId: String): F[ResourceReviewSummaryView]
  def checkEligibility(authorUserId: UserId, orderItemId: OrderItemId): F[ReviewEligibilityView]
  def createReview(authorUserId: UserId, orderId: OrderId, orderItemId: OrderItemId, ratingValue: Int, title: String, content: String, imageRefs: List[ReviewImageRef], now: Instant): F[ReviewView]
  def updateReview(reviewId: ReviewId, authorUserId: UserId, ratingValue: Int, title: String, content: String, imageRefs: List[ReviewImageRef], now: Instant): F[ReviewView]
  def deleteReview(reviewId: ReviewId, authorUserId: UserId, now: Instant): F[Unit]
  def uploadImage(authorUserId: UserId, originalFileName: String, contentTypeValue: String, fileBytes: Array[Byte], now: Instant): F[ReviewImageRef]

final class LiveReviewApplicationService[F[_]: MonadThrow](
    reviewRepository: ReviewRepository[F],
    orderRepository: OrderRepository[F],
    userRepository: UserRepository[F],
    contentImageStorage: ContentImageStorage[F]
) extends ReviewApplicationService[F]:
  private val allowedContentTypes =
    Set("image/png", "image/jpeg", "image/jpg", "image/webp")

  private val allowedFileExtensions =
    Set("png", "jpg", "jpeg", "webp")

  private val maximumImageBytes: Long = 5L * 1024L * 1024L

  override def listMyReviews(authorUserId: UserId): F[List[ReviewView]] =
    for
      _ <- requireUser(authorUserId)
      reviews <- reviewRepository.listReviewsByAuthorUserId(authorUserId)
      views <- reviews.traverse(toReviewView(_, Some(authorUserId)))
    yield views

  override def listReviewsByResource(viewerUserId: UserId, resourceType: ReviewResourceType, resourceId: String): F[List[ReviewView]] =
    for
      _ <- requireUser(viewerUserId)
      reviews <- reviewRepository.listPublishedReviewsByResource(resourceType, resourceId)
      views <- reviews.traverse(toReviewView(_, Some(viewerUserId)))
    yield views

  override def getResourceSummary(viewerUserId: UserId, resourceType: ReviewResourceType, resourceId: String): F[ResourceReviewSummaryView] =
    for
      _ <- requireUser(viewerUserId)
      reviews <- reviewRepository.listPublishedReviewsByResource(resourceType, resourceId)
      // averageRating / reviewCount 是查询时聚合值，不是 review 表里的核心字段。
      reviewCount = reviews.size
      averageRating =
        if reviewCount == 0 then BigDecimal(0)
        else BigDecimal(reviews.map(_.rating.value).sum) / BigDecimal(reviewCount)
    yield ResourceReviewSummaryView(resourceType, resourceId, averageRating.setScale(1, BigDecimal.RoundingMode.HALF_UP), reviewCount)

  override def checkEligibility(authorUserId: UserId, orderItemId: OrderItemId): F[ReviewEligibilityView] =
    for
      _ <- requireUser(authorUserId)
      // 资格校验放在后端，避免前端仅凭订单摘要猜测是否可写评价。
      order <- orderRepository.findOrderByOrderItemId(orderItemId).flatMap(_.liftTo[F](ReviewError.ReviewWasNotAllowed(authorUserId, orderItemId, "order item not found")))
      orderItem <- MonadThrow[F].fromEither(findOrderItem(order, orderItemId, authorUserId))
      binding = toReviewBinding(orderItem)
      existingReview <- reviewRepository.findReviewByAuthorAndOrderItem(authorUserId, orderItemId)
      reason =
        if order.ownerUserId != authorUserId then Some("Only the buyer can write a review for this booking.")
        else if existingReview.nonEmpty then Some("You have already reviewed this booking.")
        else if !isPurchasedOrder(order, orderItem) then Some("This booking is not reviewable yet.")
        else None
    yield ReviewEligibilityView(order.orderId, orderItemId, reason.isEmpty, existingReview.nonEmpty, reason, binding.resourceSummaryTitle)

  override def createReview(authorUserId: UserId, orderId: OrderId, orderItemId: OrderItemId, ratingValue: Int, title: String, content: String, imageRefs: List[ReviewImageRef], now: Instant): F[ReviewView] =
    for
      _ <- requireUser(authorUserId)
      existingReview <- reviewRepository.findReviewByAuthorAndOrderItem(authorUserId, orderItemId)
      _ <- existingReview match
        case Some(_) => MonadThrow[F].raiseError(ReviewError.ReviewAlreadyExistsForOrderItem(authorUserId, orderItemId))
        case None    => MonadThrow[F].unit
      order <- orderRepository.findOrderById(orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(orderId)))
      _ <- if order.ownerUserId == authorUserId then MonadThrow[F].unit
      else MonadThrow[F].raiseError(ReviewError.ReviewWasNotAllowed(authorUserId, orderItemId, "order owner did not match"))
      orderItem <- MonadThrow[F].fromEither(findOrderItem(order, orderItemId, authorUserId))
      _ <- if isPurchasedOrder(order, orderItem) then MonadThrow[F].unit
      else MonadThrow[F].raiseError(ReviewError.ReviewWasNotAllowed(authorUserId, orderItemId, "booking is not reviewable yet"))
      rating <- MonadThrow[F].fromEither(Rating.create(ratingValue))
      binding = toReviewBinding(orderItem)
      reviewId <- reviewRepository.nextReviewId
      review <- MonadThrow[F].fromEither(createPublishedReview(reviewId, authorUserId, binding.resourceType, binding.resourceId, orderId, orderItemId, rating, title, content, imageRefs, now))
      _ <- reviewRepository.saveReview(review)
      view <- toReviewView(review, Some(authorUserId))
    yield view

  override def updateReview(reviewId: ReviewId, authorUserId: UserId, ratingValue: Int, title: String, content: String, imageRefs: List[ReviewImageRef], now: Instant): F[ReviewView] =
    for
      _ <- requireUser(authorUserId)
      existingReview <- reviewRepository.findReviewById(reviewId).flatMap(_.liftTo[F](ReviewError.ReviewWasNotFound(reviewId)))
      rating <- MonadThrow[F].fromEither(Rating.create(ratingValue))
      updatedReview <- MonadThrow[F].fromEither(com.typesafe.travel.content.domain.updateReview(existingReview, authorUserId, rating, title, content, imageRefs, now))
      _ <- reviewRepository.saveReview(updatedReview)
      view <- toReviewView(updatedReview, Some(authorUserId))
    yield view

  override def deleteReview(reviewId: ReviewId, authorUserId: UserId, now: Instant): F[Unit] =
    for
      _ <- requireUser(authorUserId)
      existingReview <- reviewRepository.findReviewById(reviewId).flatMap(_.liftTo[F](ReviewError.ReviewWasNotFound(reviewId)))
      deletedReview <- MonadThrow[F].fromEither(com.typesafe.travel.content.domain.deleteReview(existingReview, authorUserId, now))
      _ <- reviewRepository.saveReview(deletedReview)
    yield ()

  override def uploadImage(authorUserId: UserId, originalFileName: String, contentTypeValue: String, fileBytes: Array[Byte], now: Instant): F[ReviewImageRef] =
    for
      _ <- requireUser(authorUserId)
      _ <- validateImagePresence(originalFileName, fileBytes)
      _ <- validateContentType(contentTypeValue)
      fileExtension <- validateFileExtension(originalFileName)
      _ <- validateFileSize(fileBytes)
      storedFile <- contentImageStorage.storeImage(ContentImageCollection.Review, authorUserId, originalFileName, fileExtension, fileBytes)
      imageId <- reviewRepository.nextReviewImageId
    yield ReviewImageRef(
      imageId = imageId,
      publicUrl = storedFile.publicUrl,
      originalFileName = storedFile.originalFileName,
      sortOrder = 0,
      createdAt = now
    )

  private def validateImagePresence(originalFileName: String, fileBytes: Array[Byte]): F[Unit] =
    if originalFileName.trim.nonEmpty && fileBytes.nonEmpty then MonadThrow[F].unit
    else MonadThrow[F].raiseError(ReviewImageUploadError.ImageWasMissing)

  private def validateContentType(contentTypeValue: String): F[String] =
    val normalizedContentType = contentTypeValue.trim.toLowerCase
    if allowedContentTypes.contains(normalizedContentType) then MonadThrow[F].pure(normalizedContentType)
    else MonadThrow[F].raiseError(ReviewImageUploadError.ImageFileTypeWasInvalid(contentTypeValue))

  private def validateFileExtension(originalFileName: String): F[String] =
    val normalizedFileName = originalFileName.trim.toLowerCase
    val extensionValue = normalizedFileName.split('.').lastOption.getOrElse("")
    if allowedFileExtensions.contains(extensionValue) then MonadThrow[F].pure(extensionValue)
    else MonadThrow[F].raiseError(ReviewImageUploadError.ImageFileExtensionWasInvalid(originalFileName))

  private def validateFileSize(fileBytes: Array[Byte]): F[Unit] =
    if fileBytes.length.toLong <= maximumImageBytes then MonadThrow[F].unit
    else MonadThrow[F].raiseError(ReviewImageUploadError.ImageFileWasTooLarge(maximumImageBytes, fileBytes.length.toLong))

  private def requireUser(userId: UserId): F[User] =
    userRepository.findByUserId(userId).flatMap(_.liftTo[F](UserError.UserWasNotFound(userId)))

  private def toReviewView(review: Review, currentUserId: Option[UserId]): F[ReviewView] =
    for
      author <- requireUser(review.authorUserId)
      order <- orderRepository.findOrderById(review.orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(review.orderId)))
      orderItem <- MonadThrow[F].fromEither(findOrderItem(order, review.orderItemId, review.authorUserId))
      // Review 核心只保存 resourceType/resourceId。
      // 更适合阅读的标题、副标题来自订单快照的运行时绑定。
      binding = toReviewBinding(orderItem)
    yield ReviewView(
      review.reviewId,
      review.authorUserId,
      author.userDisplayName.value,
      author.avatarUrl.map(_.value),
      review.resourceType.toString,
      review.resourceId,
      binding.resourceSummaryTitle,
      binding.resourceSummarySubtitle,
      review.orderId,
      review.orderItemId,
      review.rating.value,
      review.title,
      review.content,
      review.status.toString,
      review.createdAt,
      review.updatedAt,
      currentUserId.contains(review.authorUserId),
      currentUserId.contains(review.authorUserId) && review.status == ReviewStatus.Published,
      currentUserId.contains(review.authorUserId) && review.status == ReviewStatus.Published,
      review.imageRefs
    )

  private def findOrderItem(order: Order, orderItemId: OrderItemId, authorUserId: UserId): Either[ReviewError, OrderLineItem] =
    order.orderLineItems.find(_.orderItemId == orderItemId).toRight(ReviewError.ReviewWasNotAllowed(authorUserId, orderItemId, "order item not found in order"))

  private def isPurchasedOrder(order: Order, orderItem: OrderLineItem): Boolean =
    Set(OrderStatus.Confirmed, OrderStatus.PartiallyRefunded, OrderStatus.Refunded).contains(order.orderStatus) &&
      orderItem.orderItemStatus != OrderItemStatus.Cancelled

  private def toReviewBinding(orderItem: OrderLineItem): ReviewBinding =
    // 不同资源类型在这里被统一成 review 展示上下文。
    orderItem match
      case flightOrderItem: FlightOrderItem =>
        ReviewBinding(
          ReviewResourceType.Flight,
          flightOrderItem.flightBookingSnapshot.flightId.value,
          s"${flightOrderItem.flightBookingSnapshot.airlineName.value} ${flightOrderItem.flightBookingSnapshot.flightNumber.value}",
          s"${flightOrderItem.flightBookingSnapshot.departureAirportCode.value} -> ${flightOrderItem.flightBookingSnapshot.arrivalAirportCode.value}"
        )
      case hotelOrderItem: HotelOrderItem =>
        ReviewBinding(
          ReviewResourceType.Hotel,
          hotelOrderItem.hotelBookingSnapshot.hotelId.value,
          s"${hotelOrderItem.hotelBookingSnapshot.hotelName.value} ${hotelOrderItem.hotelBookingSnapshot.roomTypeName.value}",
          s"${hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkIn} -> ${hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkOut}"
        )
      case trainOrderItem: TrainOrderItem =>
        ReviewBinding(
          ReviewResourceType.Train,
          trainOrderItem.trainBookingSnapshot.trainId.value,
          s"${trainOrderItem.trainBookingSnapshot.trainNumber.value} ${trainOrderItem.trainBookingSnapshot.fromStationName.value}-${trainOrderItem.trainBookingSnapshot.toStationName.value}",
          trainOrderItem.trainBookingSnapshot.seatClass.toString
        )
      case attractionOrderItem: AttractionOrderItem =>
        ReviewBinding(
          ReviewResourceType.Attraction,
          attractionOrderItem.attractionTicketSnapshot.attractionId.value,
          s"${attractionOrderItem.attractionTicketSnapshot.attractionName} ${attractionOrderItem.attractionTicketSnapshot.ticketTypeName}",
          attractionOrderItem.attractionTicketSnapshot.useDate.toString
        )
