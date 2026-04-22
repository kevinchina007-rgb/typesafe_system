package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*

trait BlogRepository[F[_]]:
  def nextPostId: F[BlogId]
  def nextCommentId: F[BlogCommentId]
  def nextLikeId: F[BlogLikeId]
  def nextPostImageId: F[BlogImageId]
  def findPostById(postId: BlogId): F[Option[BlogPost]]
  def listAllPosts: F[List[BlogPost]]
  def findCommentById(commentId: BlogCommentId): F[Option[BlogComment]]
  def listPublishedPosts: F[List[BlogPost]]
  def searchPublishedPosts(query: String): F[List[BlogPost]]
  def listPostsByAuthorUserId(authorUserId: UserId): F[List[BlogPost]]
  def searchPostsByAuthorUserId(authorUserId: UserId, query: String): F[List[BlogPost]]
  def listCommentsByPostId(postId: BlogId): F[List[BlogComment]]
  def listLikesByPostId(postId: BlogId): F[List[BlogLike]]
  def findLikeByPostIdAndUserId(postId: BlogId, userId: UserId): F[Option[BlogLike]]
  def savePost(post: BlogPost): F[BlogPost]
  def saveComment(comment: BlogComment): F[BlogComment]
  def saveLike(like: BlogLike): F[BlogLike]
  def deleteLike(postId: BlogId, userId: UserId): F[Unit]
