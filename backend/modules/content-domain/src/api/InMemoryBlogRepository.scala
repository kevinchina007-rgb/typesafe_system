package com.typesafe.travel.api.memory

import cats.effect.kernel.{Ref, Sync}
import cats.syntax.all.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.shared.kernel.*

final class InMemoryBlogRepository[F[_]: Sync] private (
    postState: Ref[F, Map[BlogId, BlogPost]],
    commentState: Ref[F, Map[BlogCommentId, BlogComment]],
    likeState: Ref[F, Map[(BlogId, UserId), BlogLike]],
    postImageSequence: Ref[F, Long],
    postSequence: Ref[F, Long],
    commentSequence: Ref[F, Long],
    likeSequence: Ref[F, Long]
) extends BlogRepository[F]:
  override def nextPostId: F[BlogId] =
    postSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> BlogId(s"post-$nextValue")
    }

  override def nextCommentId: F[BlogCommentId] =
    commentSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> BlogCommentId(s"comment-$nextValue")
    }

  override def nextLikeId: F[BlogLikeId] =
    likeSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> BlogLikeId(s"like-$nextValue")
    }

  override def nextPostImageId: F[BlogImageId] =
    postImageSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> BlogImageId(s"blog-image-$nextValue")
    }

  override def findPostById(postId: BlogId): F[Option[BlogPost]] =
    postState.get.map(_.get(postId))

  override def findCommentById(commentId: BlogCommentId): F[Option[BlogComment]] =
    commentState.get.map(_.get(commentId))

  override def listPublishedPosts: F[List[BlogPost]] =
    postState.get.map(_.values.filter(_.status == BlogPostStatus.Published).toList.sortBy(_.publishedAt.map(_.toEpochMilli).getOrElse(0L))(Ordering.Long.reverse))

  override def searchPublishedPosts(query: String): F[List[BlogPost]] =
    postState.get.map(
      _.values
        .filter(post => post.status == BlogPostStatus.Published && matchesQuery(post, query))
        .toList
        .sortBy(_.publishedAt.map(_.toEpochMilli).getOrElse(0L))(Ordering.Long.reverse)
    )

  override def listPostsByAuthorUserId(authorUserId: UserId): F[List[BlogPost]] =
    postState.get.map(_.values.filter(_.authorUserId == authorUserId).toList.sortBy(_.updatedAt.toEpochMilli)(Ordering.Long.reverse))

  override def searchPostsByAuthorUserId(authorUserId: UserId, query: String): F[List[BlogPost]] =
    postState.get.map(
      _.values
        .filter(post => post.authorUserId == authorUserId && matchesQuery(post, query))
        .toList
        .sortBy(_.updatedAt.toEpochMilli)(Ordering.Long.reverse)
    )

  override def listCommentsByPostId(postId: BlogId): F[List[BlogComment]] =
    commentState.get.map(_.values.filter(_.postId == postId).toList.sortBy(_.createdAt.toEpochMilli))

  override def listLikesByPostId(postId: BlogId): F[List[BlogLike]] =
    likeState.get.map(_.values.filter(_.postId == postId).toList.sortBy(_.createdAt.toEpochMilli))

  override def findLikeByPostIdAndUserId(postId: BlogId, userId: UserId): F[Option[BlogLike]] =
    likeState.get.map(_.get((postId, userId)))

  override def savePost(post: BlogPost): F[BlogPost] =
    postState.update(_ + (post.postId -> post)).as(post)

  override def saveComment(comment: BlogComment): F[BlogComment] =
    commentState.update(_ + (comment.commentId -> comment)).as(comment)

  override def saveLike(like: BlogLike): F[BlogLike] =
    likeState.modify { currentState =>
      currentState.get((like.postId, like.userId)) match
        case Some(existingLike) => currentState -> existingLike
        case None               => (currentState + ((like.postId, like.userId) -> like)) -> like
    }

  override def deleteLike(postId: BlogId, userId: UserId): F[Unit] =
    likeState.update(_ - ((postId, userId)))

  private def matchesQuery(post: BlogPost, query: String): Boolean =
    val normalizedQuery = query.trim.toLowerCase
    normalizedQuery.isEmpty ||
    post.title.toLowerCase.contains(normalizedQuery) ||
    post.summary.toLowerCase.contains(normalizedQuery) ||
    post.content.toLowerCase.contains(normalizedQuery)

object InMemoryBlogRepository:
  def create[F[_]: Sync]: InMemoryBlogRepository[F] =
    new InMemoryBlogRepository[F](
      postState = Ref.unsafe[F, Map[BlogId, BlogPost]](Map.empty),
      commentState = Ref.unsafe[F, Map[BlogCommentId, BlogComment]](Map.empty),
      likeState = Ref.unsafe[F, Map[(BlogId, UserId), BlogLike]](Map.empty),
      postImageSequence = Ref.unsafe[F, Long](0L),
      postSequence = Ref.unsafe[F, Long](0L),
      commentSequence = Ref.unsafe[F, Long](0L),
      likeSequence = Ref.unsafe[F, Long](0L)
    )
