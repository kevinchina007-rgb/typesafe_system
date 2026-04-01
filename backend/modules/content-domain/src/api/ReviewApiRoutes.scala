package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.application.ReviewImageUploadError
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.content.domain.{ReviewImageRef, ReviewResourceType}
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers
import org.http4s.multipart.Multipart

import java.time.Instant

trait ReviewApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def reviewRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root / "api" / "reviews" / "mine" =>
      for
        userIdValue <- fromEither(request.params.get("userId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("userId")))
        reviews <- reviewApplicationService.listMyReviews(UserId(userIdValue))
        response <- Ok(ReviewListResponseDto(reviews.map(ReviewResponseDto.fromView)).asJson)
      yield response

    case request @ GET -> Root / "api" / "reviews" =>
      for
        userIdValue <- fromEither(request.params.get("userId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("userId")))
        resourceTypeValue <- fromEither(request.params.get("resourceType").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("resourceType")))
        resourceIdValue <- fromEither(request.params.get("resourceId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("resourceId")))
        resourceType <- fromEither(parseReviewResourceType(resourceTypeValue))
        reviews <- reviewApplicationService.listReviewsByResource(UserId(userIdValue), resourceType, resourceIdValue.trim)
        response <- Ok(ReviewListResponseDto(reviews.map(ReviewResponseDto.fromView)).asJson)
      yield response

    case request @ GET -> Root / "api" / "reviews" / "summary" =>
      for
        userIdValue <- fromEither(request.params.get("userId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("userId")))
        resourceTypeValue <- fromEither(request.params.get("resourceType").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("resourceType")))
        resourceIdValue <- fromEither(request.params.get("resourceId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("resourceId")))
        resourceType <- fromEither(parseReviewResourceType(resourceTypeValue))
        summary <- reviewApplicationService.getResourceSummary(UserId(userIdValue), resourceType, resourceIdValue.trim)
        response <- Ok(ResourceReviewSummaryResponseDto.fromView(summary).asJson)
      yield response

    case request @ GET -> Root / "api" / "reviews" / "eligibility" =>
      for
        userIdValue <- fromEither(request.params.get("userId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("userId")))
        orderItemIdValue <- fromEither(request.params.get("orderItemId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("orderItemId")))
        eligibility <- reviewApplicationService.checkEligibility(UserId(userIdValue), OrderItemId(orderItemIdValue))
        response <- Ok(ReviewEligibilityResponseDto.fromView(eligibility).asJson)
      yield response

    case request @ POST -> Root / "api" / "reviews" / "images" =>
      for
        multipartPayload <- request.as[Multipart[F]]
        userIdValue <- fromEither(request.params.get("userId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("userId")))
        imagePart <- multipartPayload.parts.find(_.name.contains("image")).liftTo[F](ReviewImageUploadError.ImageWasMissing)
        imageFileName <- imagePart.filename.liftTo[F](ReviewImageUploadError.ImageWasMissing)
        imageContentType =
          imagePart.headers
            .get[headers.`Content-Type`]
            .map(contentTypeHeader => s"${contentTypeHeader.mediaType.mainType}/${contentTypeHeader.mediaType.subType}")
            .getOrElse("")
        imageBytes <- imagePart.body.compile.to(Array)
        now <- currentInstantF
        imageRef <- reviewApplicationService.uploadImage(UserId(userIdValue), imageFileName, imageContentType, imageBytes, now)
        response <- Created(ContentImageResponseDto.fromReviewImageRef(imageRef).asJson)
      yield response

    case request @ POST -> Root / "api" / "reviews" =>
      for
        createRequest <- request.as[CreateReviewRequestDto]
        now <- currentInstantF
        review <- reviewApplicationService.createReview(
          authorUserId = UserId(createRequest.userId),
          orderId = OrderId(createRequest.orderId),
          orderItemId = OrderItemId(createRequest.orderItemId),
          ratingValue = createRequest.rating,
          title = createRequest.title,
          content = createRequest.content,
          imageRefs = parseReviewImages(createRequest.images),
          now = now
        )
        response <- Created(ReviewResponseDto.fromView(review).asJson)
      yield response

    case request @ PATCH -> Root / "api" / "reviews" / reviewIdValue =>
      for
        updateRequest <- request.as[UpdateReviewRequestDto]
        now <- currentInstantF
        review <- reviewApplicationService.updateReview(
          reviewId = ReviewId(reviewIdValue),
          authorUserId = UserId(updateRequest.userId),
          ratingValue = updateRequest.rating,
          title = updateRequest.title,
          content = updateRequest.content,
          imageRefs = parseReviewImages(updateRequest.images),
          now = now
        )
        response <- Ok(ReviewResponseDto.fromView(review).asJson)
      yield response

    case request @ DELETE -> Root / "api" / "reviews" / reviewIdValue =>
      for
        deleteRequest <- request.as[DeleteReviewRequestDto]
        now <- currentInstantF
        _ <- reviewApplicationService.deleteReview(ReviewId(reviewIdValue), UserId(deleteRequest.userId), now)
        response <- NoContent()
      yield response
  }

  private def parseReviewResourceType(value: String): Either[SharedValidationError, ReviewResourceType] =
    value.trim.toLowerCase match
      case "flight"     => Right(ReviewResourceType.Flight)
      case "hotel"      => Right(ReviewResourceType.Hotel)
      case "train"      => Right(ReviewResourceType.Train)
      case "attraction" => Right(ReviewResourceType.Attraction)
      case _            => Left(SharedValidationError.RequiredFieldWasEmpty("resourceType"))

  private def parseReviewImages(images: List[ContentImageResponseDto]): List[ReviewImageRef] =
    images.zipWithIndex.map { case (image, index) =>
      ReviewImageRef(
        imageId = ReviewImageId(image.imageId),
        publicUrl = image.publicUrl,
        originalFileName = image.originalFileName,
        sortOrder = index,
        createdAt = Instant.parse(image.createdAt)
      )
    }
