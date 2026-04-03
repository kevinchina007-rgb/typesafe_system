package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.Instant

final class ReviewSpec extends FunSuite:
  private val createdAt = Instant.parse("2026-04-01T00:00:00Z")

  test("review rating must be between one and five") {
    val result =
      for
        rating <- Rating.create(6)
        review <- createPublishedReview(
          reviewId = ReviewId("review-1"),
          authorUserId = UserId("user-1"),
          resourceType = ReviewResourceType.Flight,
          resourceId = "flight-1",
          orderId = OrderId("order-1"),
          orderItemId = OrderItemId("order-item-1"),
          rating = rating,
          title = "Great flight",
          content = "Smooth boarding",
          imageRefs = Nil,
          createdAt = createdAt
        )
      yield review

    assert(result.isLeft)
  }

  test("review stores backend-derived resource binding") {
    val review =
      createPublishedReview(
        reviewId = ReviewId("review-2"),
        authorUserId = UserId("user-1"),
        resourceType = ReviewResourceType.Train,
        resourceId = "train-1",
        orderId = OrderId("order-1"),
        orderItemId = OrderItemId("order-item-1"),
        rating = Rating.unsafe(5),
        title = "Fast trip",
        content = "Clean carriage and on time.",
        imageRefs = Nil,
        createdAt = createdAt
      ).fold(throw _, identity)

    assertEquals(review.resourceType, ReviewResourceType.Train)
    assertEquals(review.resourceId, "train-1")
    assertEquals(review.status, ReviewStatus.Published)
  }

  test("author can update and delete own review") {
    val review =
      createPublishedReview(
        reviewId = ReviewId("review-3"),
        authorUserId = UserId("user-1"),
        resourceType = ReviewResourceType.Hotel,
        resourceId = "hotel-1",
        orderId = OrderId("order-2"),
        orderItemId = OrderItemId("order-item-2"),
        rating = Rating.unsafe(4),
        title = "Good stay",
        content = "Comfortable room",
        imageRefs = Nil,
        createdAt = createdAt
      ).fold(throw _, identity)

    val updatedReview =
      updateReview(
        review = review,
        editorUserId = UserId("user-1"),
        rating = Rating.unsafe(5),
        title = "Great stay",
        content = "Comfortable room and good service",
        imageRefs = Nil,
        updatedAt = createdAt.plusSeconds(30)
      ).fold(throw _, identity)

    val deletedReview =
      deleteReview(
        review = updatedReview,
        editorUserId = UserId("user-1"),
        updatedAt = createdAt.plusSeconds(60)
      ).fold(throw _, identity)

    assertEquals(updatedReview.rating.value, 5)
    assertEquals(deletedReview.status, ReviewStatus.Deleted)
  }

  test("non-author cannot update review") {
    val review =
      createPublishedReview(
        reviewId = ReviewId("review-4"),
        authorUserId = UserId("user-1"),
        resourceType = ReviewResourceType.Attraction,
        resourceId = "attraction-1",
        orderId = OrderId("order-3"),
        orderItemId = OrderItemId("order-item-3"),
        rating = Rating.unsafe(3),
        title = "Fine",
        content = "Crowded but okay",
        imageRefs = Nil,
        createdAt = createdAt
      ).fold(throw _, identity)

    val result =
      updateReview(
        review = review,
        editorUserId = UserId("user-2"),
        rating = Rating.unsafe(4),
        title = "Changed",
        content = "Changed",
        imageRefs = Nil,
        updatedAt = createdAt.plusSeconds(10)
      )

    assert(result.left.exists(_.isInstanceOf[ReviewError.ReviewAuthorMismatch]))
  }
