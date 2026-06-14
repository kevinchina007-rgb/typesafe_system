package com.typesafe.travel.content.api

import cats.effect.IO
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, ResultSet}
import java.time.Instant

private val reviewSelectSql =
  """
    select r.review_id, r.author_user_id, u.nickname as author_display_name, u.avatar_url as author_avatar_url,
           r.resource_type, r.resource_id, r.order_id, r.order_item_id, r.rating, r.title, r.content,
           r.status, r.created_at, r.updated_at
    from reviews r
    join users u on u.user_id = r.author_user_id
  """

def listMyReviews(connection: Connection, userId: String): IO[ReviewListPlannerResponse] =
  queryReviews(
    connection,
    reviewSelectSql + " where r.author_user_id = ? order by r.created_at desc",
    List(userId),
    Some(userId)
  ).map(ReviewListPlannerResponse.apply)

def listReviewsByResource(connection: Connection, input: ListReviewsByResourcePlannerRequest): IO[ReviewListPlannerResponse] =
  val resourceType = ReviewResourceType.fromText(input.resourceType).toString
  queryReviews(
    connection,
    reviewSelectSql + " where r.resource_type = ? and r.resource_id = ? and r.status = ? order by r.created_at desc",
    List(resourceType, input.resourceId.trim, ReviewStatus.Published.toString),
    Some(input.userId)
  ).map(ReviewListPlannerResponse.apply)

def summary(connection: Connection, input: GetReviewSummaryPlannerRequest): IO[ResourceReviewSummaryPlannerResponse] =
  IO.blocking {
    val resourceType = ReviewResourceType.fromText(input.resourceType).toString
    PlainSqlSupport.withStatement(
      connection,
      "select count(*) as review_count, coalesce(avg(rating), 0) as average_rating from reviews where resource_type = ? and resource_id = ? and status = ?"
    ) { statement =>
      statement.setString(1, resourceType)
      statement.setString(2, input.resourceId.trim)
      statement.setString(3, ReviewStatus.Published.toString)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          ResourceReviewSummaryPlannerResponse(
            resourceType = resourceType,
            resourceId = input.resourceId.trim,
            averageRating = BigDecimal(resultSet.getBigDecimal("average_rating")).setScale(1, BigDecimal.RoundingMode.HALF_UP).toString,
            reviewCount = resultSet.getInt("review_count")
          )
        else ResourceReviewSummaryPlannerResponse(resourceType, input.resourceId.trim, "0.0", 0)
      finally resultSet.close()
    }
  }

def eligibility(connection: Connection, input: CheckReviewEligibilityPlannerRequest): IO[ReviewEligibilityPlannerResponse] =
  IO.blocking {
    val binding = findOrderItemBinding(connection, input.orderItemId)
    val existingReview = findReviewByAuthorAndOrderItem(connection, input.userId, input.orderItemId)
    val reason =
      if binding.buyerUserId != input.userId then Some("Only the buyer can write a review for this booking.")
      else if existingReview then Some("You have already reviewed this booking.")
      else if !isPurchasedOrder(binding.orderStatus, binding.itemStatus) then Some("This booking is not reviewable yet.")
      else None
    ReviewEligibilityPlannerResponse(
      orderId = binding.orderId,
      orderItemId = input.orderItemId,
      canReview = reason.isEmpty,
      alreadyReviewed = existingReview,
      reason = reason,
      resourceSummaryTitle = binding.summaryTitle
    )
  }

private def queryReviews(connection: Connection, sql: String, values: List[String], currentUserId: Option[String]): IO[List[ReviewPlannerResponse]] =
  IO.blocking {
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
      PlainSqlSupport.queryList(statement)(readReview(connection, currentUserId))
    }
  }

private def findReviewRequired(connection: Connection, reviewId: String, currentUserId: Option[String]): ReviewPlannerResponse =
  PlainSqlSupport.withStatement(connection, reviewSelectSql + " where r.review_id = ?") { statement =>
    statement.setString(1, reviewId)
    val resultSet = statement.executeQuery()
    try
      if resultSet.next() then readReview(connection, currentUserId)(resultSet)
      else throw new IllegalArgumentException(s"Review '$reviewId' was not found")
    finally resultSet.close()
  }

private def readReview(connection: Connection, currentUserId: Option[String])(resultSet: ResultSet): ReviewPlannerResponse =
  val reviewId = resultSet.getString("review_id")
  val authorUserId = resultSet.getString("author_user_id")
  val orderItemId = resultSet.getString("order_item_id")
  val binding = findOrderItemBinding(connection, orderItemId)
  val published = resultSet.getString("status") == ReviewStatus.Published.toString
  ReviewPlannerResponse(
    reviewId = reviewId,
    authorUserId = authorUserId,
    authorDisplayName = resultSet.getString("author_display_name"),
    authorAvatarUrl = Option(resultSet.getString("author_avatar_url")),
    resourceType = resultSet.getString("resource_type"),
    resourceId = resultSet.getString("resource_id"),
    resourceSummaryTitle = binding.summaryTitle,
    resourceSummarySubtitle = binding.summarySubtitle,
    orderId = resultSet.getString("order_id"),
    orderItemId = orderItemId,
    rating = resultSet.getInt("rating"),
    title = resultSet.getString("title"),
    content = resultSet.getString("content"),
    status = resultSet.getString("status"),
    createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
    updatedAt = resultSet.getTimestamp("updated_at").toInstant.toString,
    isMyReview = currentUserId.contains(authorUserId),
    canEdit = currentUserId.contains(authorUserId) && published,
    canDelete = currentUserId.contains(authorUserId) && published,
    images = readImages(connection, reviewId)
  )

private def readImages(connection: Connection, reviewId: String): List[ContentImagePlannerResponse] =
  PlainSqlSupport.withStatement(connection, "select image_id, public_url, original_file_name, sort_order, created_at from review_images where review_id = ? order by sort_order asc, created_at asc") { statement =>
    statement.setString(1, reviewId)
    PlainSqlSupport.queryList(statement) { resultSet =>
      ContentImagePlannerResponse(
        imageId = resultSet.getString("image_id"),
        publicUrl = resultSet.getString("public_url"),
        originalFileName = resultSet.getString("original_file_name"),
        sortOrder = resultSet.getInt("sort_order"),
        createdAt = resultSet.getTimestamp("created_at").toInstant.toString
      )
    }
  }

