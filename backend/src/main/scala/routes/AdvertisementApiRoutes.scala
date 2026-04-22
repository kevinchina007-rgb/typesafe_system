package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.advertising.domain.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.application.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.api.storage.UploadedBinaryAssetUrl
import com.typesafe.travel.auth.domain.AuthManagerType
import com.typesafe.travel.persistence.UploadedBinaryAsset
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.Multipart

import java.time.Instant
import java.util.UUID

trait AdvertisementApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def advertisementRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "advertisements" / "images" =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureAdvertisingOwner(currentManager.managerType)
        multipartPayload <- request.as[Multipart[F]]
        imagePart <- requireMultipartPart(multipartPayload, "image", com.typesafe.travel.shared.kernel.SharedValidationError.RequiredFieldWasEmpty("image"))
        uploadedImage <- readUploadedBinary(imagePart, com.typesafe.travel.shared.kernel.SharedValidationError.RequiredFieldWasEmpty("image"))
        uploadedImageResponse <- uploadAdvertisementImage(uploadedImage, currentInstant)
        response <- Created(uploadedImageResponse.asJson)
      yield response

    case request @ POST -> Root / "api" / "advertisements" =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureAdvertisingOwner(currentManager.managerType)
        createRequest <- request.as[CreateAdvertisementRequestDto]
        advertisementView <- advertisementApplicationService.createAdvertisement(
          ownerManagerId = currentManager.managerId,
          ownerManagerType = currentManager.managerType.toString,
          ownerDisplayName = currentManager.displayName.value,
          command = CreateAdvertisementCommand(
            title = createRequest.title,
            subtitle = createRequest.subtitle,
            description = createRequest.description,
            imageUrl = createRequest.imageUrl.map(_.trim).filter(_.nonEmpty),
            ctaLabel = createRequest.ctaLabel,
            targetResourceType = createRequest.targetResourceType,
            targetResourceId = createRequest.targetResourceId,
            placement = createRequest.placement,
            priority = createRequest.priority,
            startAt = Instant.parse(createRequest.startAt),
            endAt = Instant.parse(createRequest.endAt)
          ),
          now = currentInstant
        )
        response <- Created(AdvertisementResponseDto.fromView(advertisementView).asJson)
      yield response

    case request @ GET -> Root / "api" / "advertisements" / "mine" =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureAdvertisingOwner(currentManager.managerType)
        advertisements <- advertisementApplicationService.listAdvertisementsForOwner(currentManager.managerId, currentManager.managerType.toString)
        response <- Ok(AdvertisementListResponseDto(advertisements.map(AdvertisementResponseDto.fromView)).asJson)
      yield response

    case request @ PUT -> Root / "api" / "advertisements" / advertisementIdValue =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureAdvertisingOwner(currentManager.managerType)
        updateRequest <- request.as[UpdateAdvertisementRequestDto]
        advertisementView <- advertisementApplicationService.updateAdvertisement(
          ownerManagerId = currentManager.managerId,
          ownerManagerType = currentManager.managerType.toString,
          advertisementId = AdvertisementId(advertisementIdValue),
          command = UpdateAdvertisementCommand(
            title = updateRequest.title,
            subtitle = updateRequest.subtitle,
            description = updateRequest.description,
            imageUrl = updateRequest.imageUrl.map(_.trim).filter(_.nonEmpty),
            ctaLabel = updateRequest.ctaLabel,
            targetResourceId = updateRequest.targetResourceId,
            placement = updateRequest.placement,
            priority = updateRequest.priority,
            startAt = Instant.parse(updateRequest.startAt),
            endAt = Instant.parse(updateRequest.endAt)
          ),
          now = currentInstant
        )
        response <- Ok(AdvertisementResponseDto.fromView(advertisementView).asJson)
      yield response

    case request @ POST -> Root / "api" / "advertisements" / advertisementIdValue / "submit-review" =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureAdvertisingOwner(currentManager.managerType)
        advertisementView <- advertisementApplicationService.submitAdvertisementForReview(
          ownerManagerId = currentManager.managerId,
          ownerManagerType = currentManager.managerType.toString,
          advertisementId = AdvertisementId(advertisementIdValue),
          now = currentInstant
        )
        response <- Ok(AdvertisementResponseDto.fromView(advertisementView).asJson)
      yield response

    case request @ POST -> Root / "api" / "advertisements" / advertisementIdValue / "pause" =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureAdvertisingOwner(currentManager.managerType)
        advertisementView <- advertisementApplicationService.pauseAdvertisement(
          ownerManagerId = currentManager.managerId,
          ownerManagerType = currentManager.managerType.toString,
          advertisementId = AdvertisementId(advertisementIdValue),
          now = currentInstant
        )
        response <- Ok(AdvertisementResponseDto.fromView(advertisementView).asJson)
      yield response

    case request @ GET -> Root / "api" / "advertisements" / "review" / "pending" =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureSiteAdmin(currentManager.managerType)
        advertisements <- advertisementApplicationService.listPendingAdvertisements
        response <- Ok(AdvertisementListResponseDto(advertisements.map(AdvertisementResponseDto.fromView)).asJson)
      yield response

    case request @ GET -> Root / "api" / "advertisements" / "review" / "history" =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureSiteAdmin(currentManager.managerType)
        advertisements <- advertisementApplicationService.listReviewedAdvertisements
        response <- Ok(AdvertisementListResponseDto(advertisements.map(AdvertisementResponseDto.fromView)).asJson)
      yield response

    case request @ POST -> Root / "api" / "advertisements" / advertisementIdValue / "approve" =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureSiteAdmin(currentManager.managerType)
        reviewRequest <- request.as[AdvertisementReviewDecisionRequestDto]
        advertisementView <- advertisementApplicationService.approveAdvertisement(currentManager.managerId, AdvertisementId(advertisementIdValue), reviewRequest.reviewNote, currentInstant)
        response <- Ok(AdvertisementResponseDto.fromView(advertisementView).asJson)
      yield response

    case request @ POST -> Root / "api" / "advertisements" / advertisementIdValue / "reject" =>
      for
        currentManager <- requireCurrentManager(request)
        _ <- ensureSiteAdmin(currentManager.managerType)
        reviewRequest <- request.as[AdvertisementReviewDecisionRequestDto]
        advertisementView <- advertisementApplicationService.rejectAdvertisement(currentManager.managerId, AdvertisementId(advertisementIdValue), reviewRequest.reviewNote, currentInstant)
        response <- Ok(AdvertisementResponseDto.fromView(advertisementView).asJson)
      yield response

    case GET -> Root / "api" / "advertisements" / "delivery" :? PlacementQueryParamMatcher(placementValue) =>
      for
        placement <- fromEither(placementValue.map(AdvertisementPlacement.fromText).toRight(com.typesafe.travel.shared.kernel.SharedValidationError.RequiredFieldWasEmpty("placement")))
        advertisements <- advertisementApplicationService.listDeliverableAdvertisements(placement, currentInstant)
        response <- Ok(AdvertisementListResponseDto(advertisements.map(AdvertisementResponseDto.fromView)).asJson)
      yield response
  }

  private object PlacementQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("placement")

  private def ensureAdvertisingOwner(managerType: AuthManagerType): F[Unit] =
    if managerType == AuthManagerType.Hotel || managerType == AuthManagerType.Attraction then Async[F].unit
    else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)

  private def ensureSiteAdmin(managerType: AuthManagerType): F[Unit] =
    if managerType == AuthManagerType.SiteAdmin then Async[F].unit
    else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)

  private def currentInstant: Instant = java.time.Instant.now()

  private def uploadAdvertisementImage(uploadedImage: UploadedBinaryFile, now: Instant): F[AdvertisementImageUploadResponseDto] =
    uploadedBinaryAssetRepository match
      case None =>
        Async[F].raiseError(new IllegalStateException("Advertisement image storage is not configured"))
      case Some(binaryAssetRepository) =>
        val normalizedFileName = uploadedImage.originalFileName.trim
        val fileExtension =
          normalizedFileName.lastIndexOf('.') match
            case index if index >= 0 && index < normalizedFileName.length - 1 =>
              normalizedFileName.substring(index + 1).trim.toLowerCase
            case _ => "bin"
        val assetId = s"advertising-${UUID.randomUUID().toString.replace("-", "").take(20)}"
        binaryAssetRepository
          .saveAsset(
            UploadedBinaryAsset(
              assetId = assetId,
              ownerUserId = None,
              assetCategory = "advertisement-image",
              originalFileName = normalizedFileName,
              fileExtension = fileExtension,
              mimeType = uploadedImage.contentTypeValue,
              fileSize = uploadedImage.fileBytes.length.toLong,
              binaryContent = uploadedImage.fileBytes,
              createdAt = now
            )
          )
          .map { storedAsset =>
            AdvertisementImageUploadResponseDto(
              assetId = storedAsset.assetId,
              publicUrl = UploadedBinaryAssetUrl.build(storedAsset.assetId, storedAsset.originalFileName, storedAsset.fileExtension),
              originalFileName = storedAsset.originalFileName,
              mimeType = storedAsset.mimeType,
              fileSize = storedAsset.fileSize
            )
          }
