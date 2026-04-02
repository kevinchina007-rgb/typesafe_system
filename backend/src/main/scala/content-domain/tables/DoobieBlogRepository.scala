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

final class DoobieBlogRepository[F[_]: Async](transactor: Transactor[F]) extends BlogRepository[F]:
  override def nextPostId: F[BlogId] =
    Sync[F].delay(BlogId(s"post-${UUID.randomUUID().toString.take(12)}"))

  override def nextCommentId: F[BlogCommentId] =
    Sync[F].delay(BlogCommentId(s"comment-${UUID.randomUUID().toString.take(12)}"))

  override def nextLikeId: F[BlogLikeId] =
    Sync[F].delay(BlogLikeId(s"like-${UUID.randomUUID().toString.take(12)}"))

  override def nextPostImageId: F[BlogImageId] =
    Sync[F].delay(BlogImageId(s"blog-image-${UUID.randomUUID().toString.take(12)}"))

  override def findPostById(postId: BlogId): F[Option[BlogPost]] =
    (for
      postRow <- selectPostById(postId).option
      imagesByPostId <- loadImagesByPostIds(postRow.toList.map(row => BlogId(row._1)))
    yield postRow.map(row => toBlogPost(row, imagesByPostId.getOrElse(BlogId(row._1), Nil)))).transact(transactor)

  override def findCommentById(commentId: BlogCommentId): F[Option[BlogComment]] =
    sql"""
      select comment_id, post_id, author_user_id, content, status, created_at
      from blog_comments
      where comment_id = ${commentId.value}
    """.query[(String, String, String, String, String, Instant)]
      .option
      .transact(transactor)
      .map(_.map(toBlogComment))

  override def listPublishedPosts: F[List[BlogPost]] =
    selectPublishedPosts.transact(transactor).flatMap(buildPosts)

  override def searchPublishedPosts(query: String): F[List[BlogPost]] =
    selectPublishedPostsByQuery(query).transact(transactor).flatMap(buildPosts)

  override def listPostsByAuthorUserId(authorUserId: UserId): F[List[BlogPost]] =
    selectPostsByAuthor(authorUserId).transact(transactor).flatMap(buildPosts)

  override def searchPostsByAuthorUserId(authorUserId: UserId, query: String): F[List[BlogPost]] =
    selectPostsByAuthorAndQuery(authorUserId, query).transact(transactor).flatMap(buildPosts)

  override def listCommentsByPostId(postId: BlogId): F[List[BlogComment]] =
    sql"""
      select comment_id, post_id, author_user_id, content, status, created_at
      from blog_comments
      where post_id = ${postId.value}
      order by created_at asc
    """.query[(String, String, String, String, String, Instant)]
      .to[List]
      .transact(transactor)
      .map(_.map(toBlogComment))

  override def listLikesByPostId(postId: BlogId): F[List[BlogLike]] =
    sql"""
      select like_id, post_id, user_id, created_at
      from blog_likes
      where post_id = ${postId.value}
      order by created_at asc
    """.query[(String, String, String, Instant)]
      .to[List]
      .transact(transactor)
      .map(_.map(toBlogLike))

  override def findLikeByPostIdAndUserId(postId: BlogId, userId: UserId): F[Option[BlogLike]] =
    sql"""
      select like_id, post_id, user_id, created_at
      from blog_likes
      where post_id = ${postId.value} and user_id = ${userId.value}
    """.query[(String, String, String, Instant)]
      .option
      .transact(transactor)
      .map(_.map(toBlogLike))

  override def savePost(post: BlogPost): F[BlogPost] =
    (
      for
        updatedRowCount <- sql"""
          update blog_posts
          set author_user_id = ${post.authorUserId.value},
              title = ${post.title},
              summary = ${post.summary},
              content = ${post.content},
              status = ${post.status.toString},
              created_at = ${post.createdAt},
              updated_at = ${post.updatedAt},
              published_at = ${post.publishedAt}
          where post_id = ${post.postId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then FC.unit
        else
          sql"""
            insert into blog_posts(post_id, author_user_id, title, summary, content, status, created_at, updated_at, published_at)
            values (
              ${post.postId.value},
              ${post.authorUserId.value},
              ${post.title},
              ${post.summary},
              ${post.content},
              ${post.status.toString},
              ${post.createdAt},
              ${post.updatedAt},
              ${post.publishedAt}
            )
          """.update.run.void
        _ <- sql"delete from blog_post_images where post_id = ${post.postId.value}".update.run
        _ <- insertPostImages(post)
      yield ()
    ).transact(transactor).as(post)

  override def saveComment(comment: BlogComment): F[BlogComment] =
    (
      for
        updatedRowCount <- sql"""
          update blog_comments
          set post_id = ${comment.postId.value},
              author_user_id = ${comment.authorUserId.value},
              content = ${comment.content},
              status = ${comment.status.toString},
              created_at = ${comment.createdAt}
          where comment_id = ${comment.commentId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then FC.unit
        else
          sql"""
            insert into blog_comments(comment_id, post_id, author_user_id, content, status, created_at)
            values (
              ${comment.commentId.value},
              ${comment.postId.value},
              ${comment.authorUserId.value},
              ${comment.content},
              ${comment.status.toString},
              ${comment.createdAt}
            )
          """.update.run.void
      yield ()
    ).transact(transactor).as(comment)

  override def saveLike(like: BlogLike): F[BlogLike] =
    (
      for
        existing <- sql"""
          select like_id, post_id, user_id, created_at
          from blog_likes
          where post_id = ${like.postId.value} and user_id = ${like.userId.value}
        """.query[(String, String, String, Instant)].option
        persisted <- existing match
          case Some(existingRow) => toBlogLike(existingRow).pure[ConnectionIO]
          case None =>
            sql"""
              insert into blog_likes(like_id, post_id, user_id, created_at)
              values (${like.likeId.value}, ${like.postId.value}, ${like.userId.value}, ${like.createdAt})
            """.update.run.as(like)
      yield persisted
    ).transact(transactor)

  override def deleteLike(postId: BlogId, userId: UserId): F[Unit] =
    sql"delete from blog_likes where post_id = ${postId.value} and user_id = ${userId.value}".update.run.transact(transactor).void

  private type BlogPostRow = (String, String, String, String, String, String, Instant, Instant, Option[Instant])
  private type BlogImageRow = (String, String, String, String, Int, Instant)

  private def buildPosts(rows: List[BlogPostRow]): F[List[BlogPost]] =
    loadImagesByPostIds(rows.map(row => BlogId(row._1))).transact(transactor).map { imagesByPostId =>
      rows.map(row => toBlogPost(row, imagesByPostId.getOrElse(BlogId(row._1), Nil)))
    }

  private def selectPostById(postId: BlogId): Query0[BlogPostRow] =
    sql"""
      select post_id, author_user_id, title, summary, content, status, created_at, updated_at, published_at
      from blog_posts
      where post_id = ${postId.value}
    """.query[BlogPostRow]

  private def selectPublishedPosts: ConnectionIO[List[BlogPostRow]] =
    sql"""
      select post_id, author_user_id, title, summary, content, status, created_at, updated_at, published_at
      from blog_posts
      where status = ${BlogPostStatus.Published.toString}
      order by published_at desc nulls last, created_at desc
    """.query[BlogPostRow].to[List]

  private def selectPublishedPostsByQuery(query: String): ConnectionIO[List[BlogPostRow]] =
    val pattern = wildcard(query)
    sql"""
      select post_id, author_user_id, title, summary, content, status, created_at, updated_at, published_at
      from blog_posts
      where status = ${BlogPostStatus.Published.toString}
        and (title ilike $pattern or summary ilike $pattern or content ilike $pattern)
      order by published_at desc nulls last, created_at desc
    """.query[BlogPostRow].to[List]

  private def selectPostsByAuthor(authorUserId: UserId): ConnectionIO[List[BlogPostRow]] =
    sql"""
      select post_id, author_user_id, title, summary, content, status, created_at, updated_at, published_at
      from blog_posts
      where author_user_id = ${authorUserId.value}
      order by updated_at desc, created_at desc
    """.query[BlogPostRow].to[List]

  private def selectPostsByAuthorAndQuery(authorUserId: UserId, query: String): ConnectionIO[List[BlogPostRow]] =
    val pattern = wildcard(query)
    sql"""
      select post_id, author_user_id, title, summary, content, status, created_at, updated_at, published_at
      from blog_posts
      where author_user_id = ${authorUserId.value}
        and (title ilike $pattern or summary ilike $pattern or content ilike $pattern)
      order by updated_at desc, created_at desc
    """.query[BlogPostRow].to[List]

  private def loadImagesByPostIds(postIds: List[BlogId]): ConnectionIO[Map[BlogId, List[BlogImageRef]]] =
    postIds.distinct match
      case Nil => FC.pure(Map.empty)
      case distinctPostIds =>
        (
          fr"""
            select image_id, post_id, public_url, original_file_name, sort_order, created_at
            from blog_post_images
            where
          """ ++ fragments.in(fr"post_id", NonEmptyList.fromListUnsafe(distinctPostIds.map(_.value))) ++ fr"order by post_id, sort_order asc, created_at asc"
        ).query[BlogImageRow].to[List].map(
          _.groupBy(row => BlogId(row._2)).view.mapValues(_.map(toBlogImageRef).sortBy(_.sortOrder)).toMap
        )

  private def insertPostImages(post: BlogPost): ConnectionIO[Unit] =
    Update[(String, String, String, String, Int, Instant)](
      """
        insert into blog_post_images(image_id, post_id, public_url, original_file_name, sort_order, created_at)
        values (?, ?, ?, ?, ?, ?)
      """
    ).updateMany(post.imageRefs.sortBy(_.sortOrder).map(imageRef =>
      (imageRef.imageId.value, post.postId.value, imageRef.publicUrl, imageRef.originalFileName, imageRef.sortOrder, imageRef.createdAt)
    )).void

  private def toBlogPost(row: BlogPostRow, imageRefs: List[BlogImageRef]): BlogPost =
    restorePersistedBlogPost(
      postId = BlogId(row._1),
      authorUserId = UserId(row._2),
      title = row._3,
      summary = row._4,
      content = row._5,
      imageRefs = imageRefs,
      status = BlogPostStatus.valueOf(row._6),
      createdAt = row._7,
      updatedAt = row._8,
      publishedAt = row._9
    )

  private def toBlogComment(row: (String, String, String, String, String, Instant)): BlogComment =
    restorePersistedBlogComment(
      commentId = BlogCommentId(row._1),
      postId = BlogId(row._2),
      authorUserId = UserId(row._3),
      content = row._4,
      status = BlogCommentStatus.valueOf(row._5),
      createdAt = row._6
    )

  private def toBlogLike(row: (String, String, String, Instant)): BlogLike =
    createBlogLike(BlogLikeId(row._1), BlogId(row._2), UserId(row._3), row._4)

  private def toBlogImageRef(row: BlogImageRow): BlogImageRef =
    BlogImageRef(
      imageId = BlogImageId(row._1),
      publicUrl = row._3,
      originalFileName = row._4,
      sortOrder = row._5,
      createdAt = row._6
    )

  private def wildcard(query: String): String =
    s"%${query.trim}%"

object DoobieBlogRepository:
  def apply[F[_]: Async](transactor: Transactor[F]): DoobieBlogRepository[F] =
    new DoobieBlogRepository[F](transactor)
