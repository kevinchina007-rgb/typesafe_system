// BlogPlannerPlainSqlSupport：博客域表访问与映射辅助。

package com.typesafe.travel.persistence.blog

import cats.effect.IO
import com.typesafe.travel.blog.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

object BlogPlannerPlainSqlSupport:
  def queryPostSummaries(connection: Connection, sql: String, values: List[String], currentUserId: Option[String]): List[BlogPostSummaryResponse] =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
      PlainSqlSupport.queryList(statement)(readPostSummary(connection, currentUserId))
    }

  def readPostSummary(connection: Connection, currentUserId: Option[String])(resultSet: ResultSet): BlogPostSummaryResponse =
    val postId = resultSet.getString("post_id")
    val authorUserId = resultSet.getString("author_user_id")
    val status = resultSet.getString("status")
    val images = loadImages(connection, postId)
    BlogPostSummaryResponse(
      postId = postId,
      authorUserId = authorUserId,
      authorDisplayName = resultSet.getString("author_display_name"),
      authorAvatarUrl = Option(resultSet.getString("author_avatar_url")),
      title = resultSet.getString("title"),
      summary = resultSet.getString("summary"),
      coverImageUrl = images.headOption.map(_.publicUrl),
      coverText = resultSet.getString("cover_text"),
      travelCity = Option(resultSet.getString("travel_city")),
      travelCities = loadCities(connection, postId),
      status = status,
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
      updatedAt = resultSet.getTimestamp("updated_at").toInstant.toString,
      publishedAt = Option(resultSet.getTimestamp("published_at")).map(_.toInstant.toString),
      commentCount = resultSet.getLong("comment_count"),
      likeCount = resultSet.getLong("like_count"),
      favoriteCount = resultSet.getLong("favorite_count"),
      likedByCurrentUser = currentUserId.exists(userId => postLikedBy(connection, postId, userId)),
      favoritedByCurrentUser = currentUserId.exists(userId => postFavoritedBy(connection, postId, userId)),
      isMyPost = currentUserId.contains(authorUserId),
      canEdit = currentUserId.contains(authorUserId) && (status == BlogPostStatus.Draft.toString || status == BlogPostStatus.Published.toString),
      canArchive = currentUserId.contains(authorUserId) && status != BlogPostStatus.Deleted.toString,
      images = images,
      tags = loadTags(connection, postId),
      searchResultSnippet = Option(resultSet.getString("summary"))
    )

  def loadPostContent(connection: Connection, postId: String): String =
    PlainSqlSupport.withStatement(connection, "select content from blog_posts where post_id = ?") { statement =>
      statement.setString(1, postId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString("content") else ""
      finally resultSet.close()
    }

  def loadImages(connection: Connection, postId: String): List[BlogImagePlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select image_id, public_url, original_file_name, sort_order, created_at from blog_post_images where post_id = ? order by sort_order asc, created_at asc") { statement =>
      statement.setString(1, postId)
      PlainSqlSupport.queryList(statement) { resultSet =>
        BlogImagePlannerResponse(
          imageId = resultSet.getString("image_id"),
          publicUrl = resultSet.getString("public_url"),
          originalFileName = resultSet.getString("original_file_name"),
          contentType = "image/*",
          byteSize = 0L,
          sortOrder = resultSet.getInt("sort_order")
        )
      }
    }

  def listProfileUsers(connection: Connection, input: ListBlogProfileUsersPlannerRequest, sql: String): IO[BlogProfileUserListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, sql) { statement =>
        statement.setString(1, input.viewerUserId.getOrElse(""))
        statement.setString(2, input.profileUserId)
        BlogProfileUserListPlannerResponse(
          PlainSqlSupport.queryList(statement) { resultSet =>
            BlogProfileUserResponse(
              userId = resultSet.getString("user_id"),
              nickname = resultSet.getString("nickname"),
              avatarUrl = Option(resultSet.getString("avatar_url")),
              followerCount = resultSet.getLong("follower_count"),
              followingCount = resultSet.getLong("following_count"),
              receivedLikeCount = resultSet.getLong("received_like_count"),
              isFollowing = resultSet.getBoolean("is_following"),
              hideRelations = resultSet.getBoolean("hide_relations"),
              relationListHidden = resultSet.getBoolean("hide_relations") && !input.viewerUserId.contains(resultSet.getString("user_id"))
            )
          }
        )
      }
    }

  def loadTags(connection: Connection, postId: String): List[BlogTagPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select tag_type, tag_value from blog_post_tags where post_id = ? order by tag_type, tag_value") { statement =>
      statement.setString(1, postId)
      PlainSqlSupport.queryList(statement) { resultSet =>
        BlogTagPlannerResponse(resultSet.getString("tag_type"), resultSet.getString("tag_value"))
      }
    }

  def loadCities(connection: Connection, postId: String): List[String] =
    val cities = PlainSqlSupport.withStatement(connection, "select city_name from blog_post_cities where post_id = ? order by sort_order asc, city_name asc") { statement =>
      statement.setString(1, postId)
      PlainSqlSupport.queryList(statement)(_.getString("city_name"))
    }
    if cities.nonEmpty then cities
    else
      PlainSqlSupport.withStatement(connection, "select travel_city from blog_posts where post_id = ?") { statement =>
        statement.setString(1, postId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then Option(resultSet.getString("travel_city")).toList
          else Nil
        finally resultSet.close()
      }

  def loadComments(connection: Connection, postId: String, currentUserId: Option[String]): List[BlogCommentResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select c.comment_id, c.post_id, c.author_user_id, u.nickname as author_display_name, u.avatar_url as author_avatar_url,
               c.content, c.parent_comment_id, c.reply_to_user_id, reply_user.nickname as reply_to_display_name,
               c.created_at,
               (select count(*) from blog_comment_likes l where l.comment_id = c.comment_id) as like_count
        from blog_comments c
        join users u on u.user_id = c.author_user_id
        left join users reply_user on reply_user.user_id = c.reply_to_user_id
        where c.post_id = ? and c.status = ?
        order by c.created_at asc
      """
    ) { statement =>
      statement.setString(1, postId)
      statement.setString(2, BlogCommentStatus.Visible.toString)
      PlainSqlSupport.queryList(statement) { resultSet =>
        val commentId = resultSet.getString("comment_id")
        val authorUserId = resultSet.getString("author_user_id")
        BlogCommentResponse(
          commentId = commentId,
          postId = resultSet.getString("post_id"),
          authorUserId = authorUserId,
          authorDisplayName = resultSet.getString("author_display_name"),
          authorAvatarUrl = Option(resultSet.getString("author_avatar_url")),
          content = resultSet.getString("content"),
          parentCommentId = Option(resultSet.getString("parent_comment_id")),
          replyToUserId = Option(resultSet.getString("reply_to_user_id")),
          replyToDisplayName = Option(resultSet.getString("reply_to_display_name")),
          createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
          likeCount = resultSet.getLong("like_count"),
          likedByCurrentUser = currentUserId.exists(userId => commentLikedBy(connection, commentId, userId)),
          isMyComment = currentUserId.contains(authorUserId),
          canDelete = currentUserId.contains(authorUserId)
        )
      }
    }

  def replaceImages(connection: Connection, postId: String, images: List[BlogImagePlannerResponse], now: Instant): Unit =
    PlainSqlSupport.withStatement(connection, "delete from blog_post_images where post_id = ?") { statement =>
      statement.setString(1, postId)
      statement.executeUpdate()
    }
    images.zipWithIndex.foreach { case (image, index) =>
      PlainSqlSupport.withStatement(connection, "insert into blog_post_images(image_id, post_id, public_url, original_file_name, sort_order, created_at) values (?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, if image.imageId.trim.nonEmpty then image.imageId else s"blog-image-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, postId)
        statement.setString(3, image.publicUrl)
        statement.setString(4, image.originalFileName)
        statement.setInt(5, index)
        statement.setTimestamp(6, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def replaceTags(connection: Connection, postId: String, tags: List[BlogTagPlannerResponse]): Unit =
    PlainSqlSupport.withStatement(connection, "delete from blog_post_tags where post_id = ?") { statement =>
      statement.setString(1, postId)
      statement.executeUpdate()
    }
    tags.distinct.foreach { tag =>
      PlainSqlSupport.withStatement(connection, "insert into blog_post_tags(post_id, tag_type, tag_value) values (?, ?, ?)") { statement =>
        statement.setString(1, postId)
        statement.setString(2, tag.tagType.trim)
        statement.setString(3, tag.tagValue.trim)
        statement.executeUpdate()
      }
    }

  def replaceCities(connection: Connection, postId: String, cities: List[String]): Unit =
    PlainSqlSupport.withStatement(connection, "delete from blog_post_cities where post_id = ?") { statement =>
      statement.setString(1, postId)
      statement.executeUpdate()
    }
    cities.zipWithIndex.foreach { case (city, index) =>
      PlainSqlSupport.withStatement(connection, "insert into blog_post_cities(post_id, city_name, sort_order) values (?, ?, ?) on conflict do nothing") { statement =>
        statement.setString(1, postId)
        statement.setString(2, city)
        statement.setInt(3, index)
        statement.executeUpdate()
      }
    }

  def normalizeCities(input: SaveBlogDraftPlannerRequest): List[String] =
    val cities = input.travelCities.getOrElse(Nil) ++ input.travelCity.toList
    cities.map(_.trim).filter(_.nonEmpty).distinct

  def insertInteraction(connection: Connection, targetId: String, actorUserId: String, now: Instant, table: String, idColumn: String, targetColumn: String, actorColumn: String, prefix: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, s"insert into $table($idColumn, $targetColumn, $actorColumn, created_at) values (?, ?, ?, ?) on conflict do nothing") { statement =>
        statement.setString(1, s"$prefix-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, targetId)
        statement.setString(3, actorUserId)
        statement.setTimestamp(4, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def deleteInteraction(connection: Connection, table: String, leftColumn: String, leftValue: String, rightColumn: String, rightValue: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, s"delete from $table where $leftColumn = ? and $rightColumn = ?") { statement =>
        statement.setString(1, leftValue)
        statement.setString(2, rightValue)
        statement.executeUpdate()
      }
    }

  def postLikedBy(connection: Connection, postId: String, userId: String): Boolean =
    exists(connection, "select 1 from blog_likes where post_id = ? and user_id = ?", postId, userId)

  def postFavoritedBy(connection: Connection, postId: String, userId: String): Boolean =
    exists(connection, "select 1 from blog_favorites where post_id = ? and user_id = ?", postId, userId)

  def commentLikedBy(connection: Connection, commentId: String, userId: String): Boolean =
    exists(connection, "select 1 from blog_comment_likes where comment_id = ? and user_id = ?", commentId, userId)

  def exists(connection: Connection, sql: String, left: String, right: String): Boolean =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      statement.setString(1, left)
      statement.setString(2, right)
      val resultSet = statement.executeQuery()
      try resultSet.next()
      finally resultSet.close()
    }
