package com.typesafe.travel.persistence.content

import cats.effect.kernel.{Async, Sync}
import cats.data.NonEmptyList
import cats.syntax.all.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.free.connection as FC
import doobie.implicits.*
import doobie.util.fragments

import java.time.Instant
import java.util.UUID

final class DoobieReviewRepository[F[_]: Async](transactor: Transactor[F]) extends ReviewRepository[F]:
  override def nextReviewId: F[ReviewId] =
    Sync[F].delay(ReviewId(s"review-${UUID.randomUUID().toString.take(12)}"))

  override def nextReviewImageId: F[ReviewImageId] =
    Sync[F].delay(ReviewImageId(s"review-image-${UUID.randomUUID().toString.take(12)}"))

  override def findReviewById(reviewId: ReviewId): F[Option[Review]] =
    (for
      reviewRow <- select(whereFragment = fr"review_id = ${reviewId.value}").option
      imagesByReviewId <- loadImagesByReviewIds(reviewRow.toList.map(row => ReviewId(row._1)))
    yield reviewRow.map(row => toReview(row, imagesByReviewId.getOrElse(ReviewId(row._1), Nil)))).transact(transactor)

  override def findReviewByAuthorAndOrderItem(authorUserId: UserId, orderItemId: OrderItemId): F[Option[Review]] =
    (for
      reviewRow <- select(whereFragment = fr"author_user_id = ${authorUserId.value} and order_item_id = ${orderItemId.value}").option
      imagesByReviewId <- loadImagesByReviewIds(reviewRow.toList.map(row => ReviewId(row._1)))
    yield reviewRow.map(row => toReview(row, imagesByReviewId.getOrElse(ReviewId(row._1), Nil)))).transact(transactor)

  override def listReviewsByAuthorUserId(authorUserId: UserId): F[List[Review]] =
    select(whereFragment = fr"author_user_id = ${authorUserId.value}").to[List].transact(transactor).flatMap(buildReviews)

  override def listReviewsByResource(resourceType: ReviewResourceType, resourceId: String): F[List[Review]] =
    select(whereFragment = fr"resource_type = ${resourceType.toString} and resource_id = ${resourceId}").to[List].transact(transactor).flatMap(buildReviews)

  override def listPublishedReviewsByResource(resourceType: ReviewResourceType, resourceId: String): F[List[Review]] =
    select(whereFragment = fr"resource_type = ${resourceType.toString} and resource_id = ${resourceId} and status = ${ReviewStatus.Published.toString}")
      .to[List]
      .transact(transactor)
      .flatMap(buildReviews)

  override def saveReview(review: Review): F[Review] =
    (
      for
        updatedRowCount <- sql"""
          update reviews
          set author_user_id = ${review.authorUserId.value},
              resource_type = ${review.resourceType.toString},
              resource_id = ${review.resourceId},
              order_id = ${review.orderId.value},
              order_item_id = ${review.orderItemId.value},
              rating = ${review.rating.value},
              title = ${review.title},
              content = ${review.content},
              status = ${review.status.toString},
              created_at = ${review.createdAt},
              updated_at = ${review.updatedAt}
          where review_id = ${review.reviewId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then FC.unit
        else
          sql"""
            insert into reviews(review_id, author_user_id, resource_type, resource_id, order_id, order_item_id, rating, title, content, status, created_at, updated_at)
            values (
              ${review.reviewId.value},
              ${review.authorUserId.value},
              ${review.resourceType.toString},
              ${review.resourceId},
              ${review.orderId.value},
              ${review.orderItemId.value},
              ${review.rating.value},
              ${review.title},
              ${review.content},
              ${review.status.toString},
              ${review.createdAt},
              ${review.updatedAt}
            )
          """.update.run.void
        _ <- sql"delete from review_images where review_id = ${review.reviewId.value}".update.run
        _ <- insertReviewImages(review)
      yield ()
    ).transact(transactor).as(review)

  private type ReviewRow = (String, String, String, String, String, String, Int, String, String, String, Instant, Instant)
  private type ReviewImageRow = (String, String, String, String, Int, Instant)

  private def buildReviews(rows: List[ReviewRow]): F[List[Review]] =
    loadImagesByReviewIds(rows.map(row => ReviewId(row._1))).transact(transactor).map { imagesByReviewId =>
      rows.map(row => toReview(row, imagesByReviewId.getOrElse(ReviewId(row._1), Nil)))
    }

  private def select(whereFragment: Fragment): Query0[ReviewRow] =
    (fr"""
      select review_id, author_user_id, resource_type, resource_id, order_id, order_item_id, rating, title, content, status, created_at, updated_at
      from reviews
      where
    """ ++ whereFragment ++ fr"order by created_at desc").query[ReviewRow]

  private def loadImagesByReviewIds(reviewIds: List[ReviewId]): ConnectionIO[Map[ReviewId, List[ReviewImageRef]]] =
    reviewIds.distinct match
      case Nil => FC.pure(Map.empty)
      case distinctReviewIds =>
        (
          fr"""
            select image_id, review_id, public_url, original_file_name, sort_order, created_at
            from review_images
            where
          """ ++ fragments.in(fr"review_id", NonEmptyList.fromListUnsafe(distinctReviewIds.map(_.value))) ++ fr"order by review_id, sort_order asc, created_at asc"
        ).query[ReviewImageRow].to[List].map(
          _.groupBy(row => ReviewId(row._2)).view.mapValues(_.map(toReviewImageRef).sortBy(_.sortOrder)).toMap
        )

  private def insertReviewImages(review: Review): ConnectionIO[Unit] =
    Update[(String, String, String, String, Int, Instant)](
      """
        insert into review_images(image_id, review_id, public_url, original_file_name, sort_order, created_at)
        values (?, ?, ?, ?, ?, ?)
      """
    ).updateMany(review.imageRefs.sortBy(_.sortOrder).map(imageRef =>
      (imageRef.imageId.value, review.reviewId.value, imageRef.publicUrl, imageRef.originalFileName, imageRef.sortOrder, imageRef.createdAt)
    )).void

  private def toReview(row: ReviewRow, imageRefs: List[ReviewImageRef]): Review =
    restorePersistedReview(
      reviewId = ReviewId(row._1),
      authorUserId = UserId(row._2),
      resourceType = ReviewResourceType.valueOf(row._3),
      resourceId = row._4,
      orderId = OrderId(row._5),
      orderItemId = OrderItemId(row._6),
      rating = Rating.unsafe(row._7),
      title = row._8,
      content = row._9,
      imageRefs = imageRefs,
      status = ReviewStatus.valueOf(row._10),
      createdAt = row._11,
      updatedAt = row._12
    )

  private def toReviewImageRef(row: ReviewImageRow): ReviewImageRef =
    ReviewImageRef(
      imageId = ReviewImageId(row._1),
      publicUrl = row._3,
      originalFileName = row._4,
      sortOrder = row._5,
      createdAt = row._6
    )

object DoobieReviewRepository:
  def apply[F[_]: Async](transactor: Transactor[F]): DoobieReviewRepository[F] =
    new DoobieReviewRepository[F](transactor)
