package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class ReviewResourceType(value: String):
  override def toString: String = value

object ReviewResourceType:
  val Flight: ReviewResourceType = ReviewResourceType("Flight")
  val Hotel: ReviewResourceType = ReviewResourceType("Hotel")
  val Train: ReviewResourceType = ReviewResourceType("Train")
  val Attraction: ReviewResourceType = ReviewResourceType("Attraction")
  given sourceEncoder: Encoder[ReviewResourceType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[ReviewResourceType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): ReviewResourceType =
    value.trim.toLowerCase match
      case "hotel" => Hotel
      case "train" => Train
      case "attraction" => Attraction
      case _ => Flight

final case class ReviewStatus(value: String):
  override def toString: String = value

object ReviewStatus:
  val Published: ReviewStatus = ReviewStatus("Published")
  val Deleted: ReviewStatus = ReviewStatus("Deleted")
  given sourceEncoder: Encoder[ReviewStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[ReviewStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): ReviewStatus =
    value.trim.toLowerCase match
      case "deleted" => Deleted
      case _ => Published

sealed trait ReviewError extends DomainError:
  def message: String

object ReviewError:
  final case class ReviewWasNotFound(reviewId: ReviewId) extends ReviewError:
    override val message: String = s"Review '${reviewId.value}' was not found"

  final case class ReviewTitleWasInvalid(reviewId: ReviewId) extends ReviewError:
    override val message: String = s"Review '${reviewId.value}' must have a title"

  final case class ReviewContentWasInvalid(reviewId: ReviewId) extends ReviewError:
    override val message: String = s"Review '${reviewId.value}' must have content"

  final case class ReviewAlreadyExistsForOrderItem(authorUserId: UserId, orderItemId: OrderItemId) extends ReviewError:
    override val message: String = s"User '${authorUserId.value}' has already reviewed order item '${orderItemId.value}'"

  final case class ReviewWasNotAllowed(authorUserId: UserId, orderItemId: OrderItemId, reason: String) extends ReviewError:
    override val message: String = s"User '${authorUserId.value}' cannot review order item '${orderItemId.value}': $reason"

  final case class ReviewAuthorMismatch(reviewId: ReviewId, userId: UserId) extends ReviewError:
    override val message: String = s"User '${userId.value}' cannot manage review '${reviewId.value}'"

  final case class ReviewImageCountWasInvalid(reviewId: ReviewId, maximumImageCount: Int) extends ReviewError:
    override val message: String = s"Review '${reviewId.value}' exceeded the maximum of $maximumImageCount images"

final case class ReviewImageRef(
    imageId: ReviewImageId,
    publicUrl: String,
    originalFileName: String,
    sortOrder: Int,
    createdAt: Instant
)
object ReviewImageRef:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[ReviewImageRef] = deriveEncoder
  given sourceDecoder: Decoder[ReviewImageRef] = deriveDecoder

final case class Review(
    reviewId: ReviewId,
    authorUserId: UserId,
    resourceType: ReviewResourceType,
    resourceId: String,
    orderId: OrderId,
    orderItemId: OrderItemId,
    rating: Rating,
    title: String,
    content: String,
    imageRefs: List[ReviewImageRef],
    status: ReviewStatus,
    createdAt: Instant,
    updatedAt: Instant
)
object Review:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[Review] = deriveEncoder
  given sourceDecoder: Decoder[Review] = deriveDecoder
