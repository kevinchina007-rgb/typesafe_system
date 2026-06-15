// BlogPlannerPlainSql 封装内容模块的plain SQL 实现。

package com.typesafe.travel.persistence.blog

import cats.effect.IO
import com.typesafe.travel.blog.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSqlSupport.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

object BlogPlannerPlainSql:
  private val selectPostSql =
    """
      select p.post_id, p.author_user_id, u.nickname as author_display_name, u.avatar_url as author_avatar_url,
             p.title, p.summary, p.content, coalesce(p.cover_text, p.summary) as cover_text,
             p.travel_city, p.status, p.created_at, p.updated_at, p.published_at,
             (select count(*) from blog_comments c where c.post_id = p.post_id and c.status = 'Visible') as comment_count,
             (select count(*) from blog_likes l where l.post_id = p.post_id) as like_count,
             (select count(*) from blog_favorites f where f.post_id = p.post_id) as favorite_count
      from blog_posts p
      join users u on u.user_id = p.author_user_id
    """

  def suggestions(connection: Connection, input: BlogSuggestionRequest): IO[BlogSuggestionListPlannerResponse] =
    IO.blocking {
      val q = s"%${input.q.trim}%"
      PlainSqlSupport.withStatement(
        connection,
        selectPostSql + " where p.status = ? and (p.title ilike ? or p.summary ilike ? or p.content ilike ?) order by p.published_at desc nulls last limit 12"
      ) { statement =>
        statement.setString(1, BlogPostStatus.Published.toString)
        statement.setString(2, q)
        statement.setString(3, q)
        statement.setString(4, q)
        BlogSuggestionListPlannerResponse(
          PlainSqlSupport.queryList(statement) { resultSet =>
            BlogSuggestionPlannerResponse("blog", resultSet.getString("post_id"), resultSet.getString("title"), resultSet.getString("summary"))
          }
        )
      }
    }

  def listPosts(
      connection: Connection,
      status: Option[String],
      authorUserId: Option[String],
      query: Option[String],
      tagType: Option[String],
      tagValue: Option[String],
      travelCity: Option[String],
      travelCities: Option[List[String]],
      favoriteUserId: Option[String],
      currentUserId: Option[String]
  ): IO[List[BlogPostSummaryResponse]] =
    IO.blocking {
      val sql = new StringBuilder(selectPostSql)
      val clauses = List.newBuilder[String]
      val values = List.newBuilder[String]

      status.foreach { nextStatus =>
        clauses += "p.status = ?"
        values += nextStatus
      }
      authorUserId.foreach { userId =>
        clauses += "p.author_user_id = ?"
        values += userId
      }
      favoriteUserId.foreach { userId =>
        clauses += "exists (select 1 from blog_favorites f where f.post_id = p.post_id and f.user_id = ?)"
        values += userId
      }
      travelCity.map(_.trim).filter(_.nonEmpty).foreach { city =>
        clauses += "(p.travel_city = ? or exists (select 1 from blog_post_cities city where city.post_id = p.post_id and city.city_name = ?))"
        values += city
        values += city
      }
      val normalizedCities = travelCities.getOrElse(Nil).map(_.trim).filter(_.nonEmpty).distinct
      if normalizedCities.nonEmpty then
        clauses += s"exists (select 1 from blog_post_cities city where city.post_id = p.post_id and city.city_name in (${normalizedCities.map(_ => "?").mkString(", ")}))"
        normalizedCities.foreach(values += _)
      query.map(_.trim).filter(_.nonEmpty).foreach { q =>
        clauses += "(p.title ilike ? or p.summary ilike ? or p.content ilike ?)"
        values += s"%$q%"
        values += s"%$q%"
        values += s"%$q%"
      }
      for
        nextTagType <- tagType.map(_.trim).filter(_.nonEmpty)
        nextTagValue <- tagValue.map(_.trim).filter(_.nonEmpty)
      do
        clauses += "exists (select 1 from blog_post_tags t where t.post_id = p.post_id and t.tag_type = ? and t.tag_value = ?)"
        values += nextTagType
        values += nextTagValue

      val whereClauses = clauses.result()
      if whereClauses.nonEmpty then sql.append(whereClauses.mkString(" where ", " and ", ""))
      sql.append(" order by p.updated_at desc, p.created_at desc")

      queryPostSummaries(connection, sql.toString(), values.result(), currentUserId)
    }

  def findPost(connection: Connection, postId: String, currentUserId: Option[String]): IO[BlogPostResponse] =
    IO.blocking {
      val summary = queryPostSummaries(connection, selectPostSql + " where p.post_id = ?", List(postId), currentUserId).headOption
        .getOrElse(throw new IllegalArgumentException(s"Blog post '$postId' was not found"))
      BlogPostResponse(summary, content = loadPostContent(connection, postId), comments = loadComments(connection, postId, currentUserId))
    }

  def insertPost(connection: Connection, input: SaveBlogDraftPlannerRequest, status: String, now: Instant): IO[String] =
    IO.blocking {
      val postId = input.postId.filter(_.trim.nonEmpty).getOrElse(s"post-${UUID.randomUUID().toString.take(12)}")
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into blog_posts(post_id, author_user_id, title, summary, content, cover_text, travel_city, status, created_at, updated_at, published_at)
          values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
          on conflict (post_id) do update set
            title = excluded.title,
            summary = excluded.summary,
            content = excluded.content,
            cover_text = excluded.cover_text,
            travel_city = excluded.travel_city,
            status = excluded.status,
            updated_at = excluded.updated_at,
            published_at = excluded.published_at
        """
      ) { statement =>
        statement.setString(1, postId)
        statement.setString(2, input.userId)
        statement.setString(3, input.title.trim)
        statement.setString(4, input.summary.trim)
        statement.setString(5, input.content.trim)
        statement.setString(6, input.coverText.trim)
        val normalizedCities = normalizeCities(input)
        statement.setString(7, normalizedCities.headOption.orNull)
        statement.setString(8, status)
        statement.setTimestamp(9, Timestamp.from(now))
        statement.setTimestamp(10, Timestamp.from(now))
        statement.setTimestamp(11, if status == BlogPostStatus.Published.toString then Timestamp.from(now) else null)
        statement.executeUpdate()
      }
      replaceImages(connection, postId, input.images, now)
      replaceTags(connection, postId, input.tags)
      replaceCities(connection, postId, normalizeCities(input))
      postId
    }

  def updatePostStatus(connection: Connection, postId: String, status: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update blog_posts set status = ?, updated_at = ?, published_at = coalesce(published_at, ?) where post_id = ?") { statement =>
        statement.setString(1, status)
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setTimestamp(3, if status == BlogPostStatus.Published.toString then Timestamp.from(now) else null)
        statement.setString(4, postId)
        statement.executeUpdate()
      }
    }

  def findPostAuthor(connection: Connection, postId: String): IO[String] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "select author_user_id from blog_posts where post_id = ?") { statement =>
        statement.setString(1, postId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then resultSet.getString("author_user_id")
          else throw new IllegalArgumentException(s"Blog post '$postId' was not found")
        finally resultSet.close()
      }
    }

  def insertComment(connection: Connection, input: BlogCommentPlannerRequest, now: Instant): IO[String] =
    IO.blocking {
      val commentId = s"comment-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into blog_comments(comment_id, post_id, author_user_id, content, parent_comment_id, reply_to_user_id, status, created_at)
          values (?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, commentId)
        statement.setString(2, input.postId)
        statement.setString(3, input.userId)
        statement.setString(4, input.content.trim)
        statement.setString(5, input.parentCommentId.orNull)
        statement.setString(6, input.replyToUserId.orNull)
        statement.setString(7, BlogCommentStatus.Visible.toString)
        statement.setTimestamp(8, Timestamp.from(now))
        statement.executeUpdate()
      }
      commentId
    }

  def findCommentBinding(connection: Connection, commentId: String): IO[(String, String)] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "select post_id, author_user_id from blog_comments where comment_id = ?") { statement =>
        statement.setString(1, commentId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then (resultSet.getString("post_id"), resultSet.getString("author_user_id"))
          else throw new IllegalArgumentException(s"Blog comment '$commentId' was not found")
        finally resultSet.close()
      }
    }

  def updateCommentStatus(connection: Connection, commentId: String, status: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update blog_comments set status = ? where comment_id = ?") { statement =>
        statement.setString(1, status)
        statement.setString(2, commentId)
        statement.executeUpdate()
      }
    }

  def likePost(connection: Connection, postId: String, userId: String, now: Instant): IO[Unit] =
    insertInteraction(connection, postId, userId, now, "blog_likes", "like_id", "post_id", "user_id", "like")

  def unlikePost(connection: Connection, postId: String, userId: String): IO[Unit] =
    deleteInteraction(connection, "blog_likes", "post_id", postId, "user_id", userId)

  def likeComment(connection: Connection, commentId: String, userId: String, now: Instant): IO[Unit] =
    insertInteraction(connection, commentId, userId, now, "blog_comment_likes", "like_id", "comment_id", "user_id", "comment-like")

  def unlikeComment(connection: Connection, commentId: String, userId: String): IO[Unit] =
    deleteInteraction(connection, "blog_comment_likes", "comment_id", commentId, "user_id", userId)

  def favoritePost(connection: Connection, postId: String, userId: String, now: Instant): IO[Unit] =
    insertInteraction(connection, postId, userId, now, "blog_favorites", "favorite_id", "post_id", "user_id", "favorite")

  def unfavoritePost(connection: Connection, postId: String, userId: String): IO[Unit] =
    deleteInteraction(connection, "blog_favorites", "post_id", postId, "user_id", userId)

  def followUser(connection: Connection, userId: String, targetUserId: String, now: Instant): IO[Unit] =
    insertInteraction(connection, targetUserId, userId, now, "blog_user_follows", "follow_id", "target_user_id", "follower_user_id", "follow")

  def blockUser(connection: Connection, userId: String, targetUserId: String, now: Instant): IO[Unit] =
    insertInteraction(connection, targetUserId, userId, now, "blog_user_blocks", "block_id", "target_user_id", "blocker_user_id", "block")

  def insertNotification(
      connection: Connection,
      receiverUserId: String,
      actorUserId: Option[String],
      notificationType: String,
      postId: Option[String],
      commentId: Option[String],
      content: String,
      now: Instant
  ): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        "insert into blog_notifications(notification_id, receiver_user_id, actor_user_id, notification_type, post_id, comment_id, content, is_read, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, s"notice-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, receiverUserId)
        statement.setString(3, actorUserId.orNull)
        statement.setString(4, notificationType)
        statement.setString(5, postId.orNull)
        statement.setString(6, commentId.orNull)
        statement.setString(7, content)
        statement.setBoolean(8, false)
        statement.setTimestamp(9, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def listNotifications(connection: Connection, userId: String): IO[BlogNotificationListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select n.notification_id, n.receiver_user_id, n.actor_user_id, u.nickname as actor_display_name,
                 u.avatar_url as actor_avatar_url, n.notification_type, n.post_id, n.comment_id, n.content,
                 n.is_read, n.created_at
          from blog_notifications n
          left join users u on u.user_id = n.actor_user_id
          where n.receiver_user_id = ?
          order by n.created_at desc
        """
      ) { statement =>
        statement.setString(1, userId)
        BlogNotificationListPlannerResponse(
          PlainSqlSupport.queryList(statement) { resultSet =>
            BlogNotificationResponse(
              notificationId = resultSet.getString("notification_id"),
              receiverUserId = resultSet.getString("receiver_user_id"),
              actorUserId = Option(resultSet.getString("actor_user_id")),
              actorDisplayName = Option(resultSet.getString("actor_display_name")),
              actorAvatarUrl = Option(resultSet.getString("actor_avatar_url")),
              notificationType = resultSet.getString("notification_type"),
              postId = Option(resultSet.getString("post_id")),
              commentId = Option(resultSet.getString("comment_id")),
              content = resultSet.getString("content"),
              isRead = resultSet.getBoolean("is_read"),
              createdAt = resultSet.getTimestamp("created_at").toInstant.toString
            )
          }
        )
      }
    }

  def findProfile(connection: Connection, input: BlogProfilePlannerRequest): IO[BlogProfilePlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select u.user_id, u.nickname, u.avatar_url,
                 (select count(*) from blog_user_follows f where f.target_user_id = u.user_id) as follower_count,
                 (select count(*) from blog_user_follows f where f.follower_user_id = u.user_id) as following_count,
                 (select count(*) from blog_likes l join blog_posts p on p.post_id = l.post_id where p.author_user_id = u.user_id) as received_like_count,
                 exists(select 1 from blog_user_follows f where f.follower_user_id = ? and f.target_user_id = u.user_id) as is_following,
                 coalesce(s.hide_relations, false) as hide_relations
          from users u
          left join blog_profile_settings s on s.user_id = u.user_id
          where u.user_id = ?
        """
      ) { statement =>
        statement.setString(1, input.viewerUserId.getOrElse(""))
        statement.setString(2, input.profileUserId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then
            BlogProfilePlannerResponse(
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
          else throw new IllegalArgumentException(s"User '${input.profileUserId}' was not found")
        finally resultSet.close()
      }
    }

  def listFollowers(connection: Connection, input: ListBlogProfileUsersPlannerRequest): IO[BlogProfileUserListPlannerResponse] =
    listProfileUsers(
      connection,
      input,
      """
        select u.user_id, u.nickname, u.avatar_url,
               (select count(*) from blog_user_follows f where f.target_user_id = u.user_id) as follower_count,
               (select count(*) from blog_user_follows f where f.follower_user_id = u.user_id) as following_count,
               (select count(*) from blog_likes l join blog_posts p on p.post_id = l.post_id where p.author_user_id = u.user_id) as received_like_count,
               exists(select 1 from blog_user_follows f where f.follower_user_id = ? and f.target_user_id = u.user_id) as is_following,
               coalesce(s.hide_relations, false) as hide_relations
        from blog_user_follows relation
        join users u on u.user_id = relation.follower_user_id
        left join blog_profile_settings s on s.user_id = u.user_id
        where relation.target_user_id = ?
        order by relation.created_at desc
      """
    )

  def listFollowing(connection: Connection, input: ListBlogProfileUsersPlannerRequest): IO[BlogProfileUserListPlannerResponse] =
    listProfileUsers(
      connection,
      input,
      """
        select u.user_id, u.nickname, u.avatar_url,
               (select count(*) from blog_user_follows f where f.target_user_id = u.user_id) as follower_count,
               (select count(*) from blog_user_follows f where f.follower_user_id = u.user_id) as following_count,
               (select count(*) from blog_likes l join blog_posts p on p.post_id = l.post_id where p.author_user_id = u.user_id) as received_like_count,
               exists(select 1 from blog_user_follows f where f.follower_user_id = ? and f.target_user_id = u.user_id) as is_following,
               coalesce(s.hide_relations, false) as hide_relations
        from blog_user_follows relation
        join users u on u.user_id = relation.target_user_id
        left join blog_profile_settings s on s.user_id = u.user_id
        where relation.follower_user_id = ?
        order by relation.created_at desc
      """
    )

  def profileRelationsHidden(connection: Connection, profileUserId: String, viewerUserId: Option[String]): IO[Boolean] =
    IO.blocking {
      if viewerUserId.contains(profileUserId) then false
      else
        PlainSqlSupport.withStatement(connection, "select coalesce(hide_relations, false) as hide_relations from blog_profile_settings where user_id = ?") { statement =>
          statement.setString(1, profileUserId)
          val resultSet = statement.executeQuery()
          try if resultSet.next() then resultSet.getBoolean("hide_relations") else false
          finally resultSet.close()
        }
    }

  def updateProfilePrivacy(connection: Connection, userId: String, hideRelations: Boolean, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into blog_profile_settings(user_id, hide_relations, updated_at)
          values (?, ?, ?)
          on conflict (user_id) do update set
            hide_relations = excluded.hide_relations,
            updated_at = excluded.updated_at
        """
      ) { statement =>
        statement.setString(1, userId)
        statement.setBoolean(2, hideRelations)
        statement.setTimestamp(3, Timestamp.from(now))
        statement.executeUpdate()
      }
    }
