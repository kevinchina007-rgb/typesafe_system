package com.typesafe.travel.content.api

import cats.effect.IO
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, Timestamp}
import java.time.Instant
import java.util.{Base64, UUID}

def create(connection: Connection, input: CreateReviewPlannerRequest, now: Instant): IO[ReviewPlannerResponse] =
  IO.blocking {
    if findReviewByAuthorAndOrderItem(connection, input.userId, input.orderItemId) then
      throw ReviewError.ReviewAlreadyExistsForOrderItem(com.typesafe.travel.shared.kernel.UserId(input.userId), com.typesafe.travel.shared.kernel.OrderItemId(input.orderItemId))
    val binding = findOrderItemBinding(connection, input.orderItemId)
    if binding.orderId != input.orderId then throw new IllegalArgumentException("order item did not belong to order")
    if binding.buyerUserId != input.userId then throw new IllegalArgumentException("order owner did not match")
    if !isPurchasedOrder(binding.orderStatus, binding.itemStatus) then throw new IllegalArgumentException("booking is not reviewable yet")
    validateReviewText(input.title, input.content)
    validateRating(input.rating)
    val reviewId = s"review-${UUID.randomUUID().toString.take(12)}"
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into reviews(review_id, author_user_id, resource_type, resource_id, order_id, order_item_id, rating, title, content, status, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """
    ) { statement =>
      statement.setString(1, reviewId)
      statement.setString(2, input.userId)
      statement.setString(3, binding.resourceType)
      statement.setString(4, binding.resourceId)
      statement.setString(5, input.orderId)
      statement.setString(6, input.orderItemId)
      statement.setInt(7, input.rating)
      statement.setString(8, input.title.trim)
      statement.setString(9, input.content.trim)
      statement.setString(10, ReviewStatus.Published.toString)
      statement.setTimestamp(11, Timestamp.from(now))
      statement.setTimestamp(12, Timestamp.from(now))
      statement.executeUpdate()
    }
    replaceImages(connection, reviewId, input.images, now)
    findReviewRequired(connection, reviewId, Some(input.userId))
  }

def update(connection: Connection, input: UpdateReviewPlannerRequest, now: Instant): IO[ReviewPlannerResponse] =
  IO.blocking {
    val existing = findReviewRequired(connection, input.reviewId, Some(input.userId))
    if existing.authorUserId != input.userId then throw new IllegalArgumentException("review author did not match")
    validateReviewText(input.title, input.content)
    validateRating(input.rating)
    PlainSqlSupport.withStatement(
      connection,
      "update reviews set rating = ?, title = ?, content = ?, updated_at = ? where review_id = ?"
    ) { statement =>
      statement.setInt(1, input.rating)
      statement.setString(2, input.title.trim)
      statement.setString(3, input.content.trim)
      statement.setTimestamp(4, Timestamp.from(now))
      statement.setString(5, input.reviewId)
      statement.executeUpdate()
    }
    replaceImages(connection, input.reviewId, input.images, now)
    findReviewRequired(connection, input.reviewId, Some(input.userId))
  }

def delete(connection: Connection, input: DeleteReviewPlannerRequest, now: Instant): IO[ReviewDeletedPlannerResponse] =
  IO.blocking {
    val existing = findReviewRequired(connection, input.reviewId, Some(input.userId))
    if existing.authorUserId != input.userId then throw new IllegalArgumentException("review author did not match")
    PlainSqlSupport.withStatement(connection, "update reviews set status = ?, updated_at = ? where review_id = ?") { statement =>
      statement.setString(1, ReviewStatus.Deleted.toString)
      statement.setTimestamp(2, Timestamp.from(now))
      statement.setString(3, input.reviewId)
      statement.executeUpdate()
    }
    ReviewDeletedPlannerResponse(deleted = true)
  }

def uploadImage(connection: Connection, input: UploadReviewImagePlannerRequest, now: Instant): IO[ContentImagePlannerResponse] =
  IO.blocking {
    val fileBytes = Base64.getDecoder.decode(input.base64Content)
    val extension = fileExtension(input.originalFileName)
    val assetId = s"content-${UUID.randomUUID().toString.replace("-", "").take(20)}"
    val imageId = s"review-image-${UUID.randomUUID().toString.take(12)}"
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into uploaded_binary_assets(asset_id, owner_user_id, asset_category, original_file_name, file_extension, mime_type, file_size, binary_content, created_at)
        values (?, ?, ?, ?, ?, ?, ?, ?, ?)
      """
    ) { statement =>
      statement.setString(1, assetId)
      statement.setString(2, input.userId)
      statement.setString(3, "review-image")
      statement.setString(4, input.originalFileName)
      statement.setString(5, extension)
      statement.setString(6, input.contentType)
      statement.setLong(7, fileBytes.length.toLong)
      statement.setBytes(8, fileBytes)
      statement.setTimestamp(9, Timestamp.from(now))
      statement.executeUpdate()
    }
    ContentImagePlannerResponse(
      imageId = imageId,
      publicUrl = buildAssetUrl(assetId, input.originalFileName, extension),
      originalFileName = input.originalFileName,
      sortOrder = 0,
      createdAt = now.toString
    )
  }

private def replaceImages(connection: Connection, reviewId: String, images: List[ContentImagePlannerResponse], now: Instant): Unit =
  if images.lengthCompare(6) > 0 then throw new IllegalArgumentException("Review exceeded maximum image count")
  PlainSqlSupport.withStatement(connection, "delete from review_images where review_id = ?") { statement =>
    statement.setString(1, reviewId)
    statement.executeUpdate()
  }
  images.zipWithIndex.foreach { case (image, index) =>
    PlainSqlSupport.withStatement(connection, "insert into review_images(image_id, review_id, public_url, original_file_name, sort_order, created_at) values (?, ?, ?, ?, ?, ?)") { statement =>
      statement.setString(1, image.imageId)
      statement.setString(2, reviewId)
      statement.setString(3, image.publicUrl)
      statement.setString(4, image.originalFileName)
      statement.setInt(5, index)
      statement.setTimestamp(6, Timestamp.from(Instant.parse(image.createdAt)))
      statement.executeUpdate()
    }
  }

private def findReviewByAuthorAndOrderItem(connection: Connection, authorUserId: String, orderItemId: String): Boolean =
  PlainSqlSupport.withStatement(connection, "select 1 from reviews where author_user_id = ? and order_item_id = ? limit 1") { statement =>
    statement.setString(1, authorUserId)
    statement.setString(2, orderItemId)
    val resultSet = statement.executeQuery()
    try resultSet.next()
    finally resultSet.close()
  }

private def findOrderItemBinding(connection: Connection, orderItemId: String): OrderItemBinding =
  PlainSqlSupport.withStatement(
    connection,
    """
      select o.order_id, o.buyer_user_id, o.status as order_status,
             li.item_kind, li.item_status, li.snapshot_json
      from order_line_items li
      join orders o on o.order_id = li.order_id
      where li.order_item_id = ?
    """
  ) { statement =>
    statement.setString(1, orderItemId)
    val resultSet = statement.executeQuery()
    try
      if resultSet.next() then
        val itemKind = resultSet.getString("item_kind")
        val snapshot = Option(resultSet.getString("snapshot_json")).getOrElse(itemKind)
        OrderItemBinding(
          orderId = resultSet.getString("order_id"),
          buyerUserId = resultSet.getString("buyer_user_id"),
          orderStatus = resultSet.getString("order_status"),
          itemStatus = resultSet.getString("item_status"),
          resourceType = resourceTypeFor(itemKind),
          resourceId = orderItemId,
          summaryTitle = itemKind,
          summarySubtitle = snapshot.take(160)
        )
      else throw new IllegalArgumentException(s"Order item '$orderItemId' was not found")
    finally resultSet.close()
  }

private def validateReviewText(title: String, content: String): Unit =
  if title.trim.isEmpty then throw new IllegalArgumentException("Review title must not be empty")
  if content.trim.isEmpty then throw new IllegalArgumentException("Review content must not be empty")

private def validateRating(rating: Int): Unit =
  if rating < 1 || rating > 5 then throw new IllegalArgumentException("Rating must be between 1 and 5")

private def isPurchasedOrder(orderStatus: String, itemStatus: String): Boolean =
  Set("Confirmed", "PartiallyRefunded", "Refunded").contains(orderStatus) && itemStatus != "Cancelled"

private def resourceTypeFor(itemKind: String): String =
  itemKind.trim.toLowerCase match
    case "hotel" | "hotelitem" | "hotelorderitem" => ReviewResourceType.Hotel.toString
    case "train" | "trainitem" | "trainorderitem" => ReviewResourceType.Train.toString
    case "attraction" | "attractionitem" | "attractionorderitem" => ReviewResourceType.Attraction.toString
    case _ => ReviewResourceType.Flight.toString

private def fileExtension(fileName: String): String =
  fileName.trim.toLowerCase.split('.').lastOption.filter(_.nonEmpty).getOrElse("bin")

private def buildAssetUrl(assetId: String, originalFileName: String, fallbackExtension: String): String =
  val safeName = originalFileName.trim.toLowerCase.replaceAll("[^a-z0-9._-]+", "-").stripPrefix("-").stripSuffix("-")
  val fileName = if safeName.nonEmpty then safeName else s"$assetId.$fallbackExtension"
  s"/uploads/assets/$assetId/$fileName"

private final case class OrderItemBinding(
    orderId: String,
    buyerUserId: String,
    orderStatus: String,
    itemStatus: String,
    resourceType: String,
    resourceId: String,
    summaryTitle: String,
    summarySubtitle: String
)

