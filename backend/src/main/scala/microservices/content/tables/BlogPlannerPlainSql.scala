package com.typesafe.travel.persistence.content

import cats.effect.IO
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

object BlogPlannerPlainSql:
  private val selectPostSql =
    "select post_id, author_user_id, title, summary, content, status, created_at, updated_at, published_at from blog_posts"

  def suggestions(connection: Connection, input: BlogSuggestionRequest): IO[BlogSuggestionListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        selectPostSql + " where status = ? and (title ilike ? or summary ilike ? or content ilike ?) order by published_at desc nulls last limit 12"
      ) { statement =>
        val q = s"%${input.q.trim}%"
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

  def list(connection: Connection, input: ListBlogPostsPlannerRequest): IO[BlogPostListPlannerResponse] =
    IO.blocking {
      val scope = input.scope.map(_.trim.toLowerCase).getOrElse("latest")
      val query = input.q.map(_.trim).filter(_.nonEmpty)
      val mine = scope == "mine"
      val sql = new StringBuilder(selectPostSql)
      val values = List.newBuilder[String]
      if mine then
        sql.append(" where author_user_id = ?")
        values += input.userId.getOrElse(throw BlogError.BlogPostAuthorMismatch(BlogId("unknown"), UserId("unknown"))).trim
      else if scope == "pending" then
        sql.append(" where status = ?")
        values += BlogPostStatus.PendingReview.toString
      else if scope == "reviewed" then
        sql.append(" where status in (?, ?)")
        values += BlogPostStatus.Published.toString
        values += BlogPostStatus.Rejected.toString
      else
        sql.append(" where status = ?")
        values += BlogPostStatus.Published.toString
      query.foreach { q =>
        sql.append(" and (title ilike ? or summary ilike ? or content ilike ?)")
        values += s"%$q%"
        values += s"%$q%"
        values += s"%$q%"
      }
      sql.append(" order by updated_at desc, created_at desc")
      queryPosts(connection, sql.toString(), values.result())
    }.map(BlogPostListPlannerResponse.apply)

  def details(connection: Connection, input: BlogPostByIdPlannerRequest): IO[BlogPost] =
    IO.blocking {
      queryPosts(connection, selectPostSql + " where post_id = ?", List(input.postId)).headOption
        .getOrElse(throw BlogError.BlogPostWasNotFound(BlogId(input.postId)))
    }

  def create(connection: Connection, input: CreateBlogPostPlannerRequest, now: Instant): IO[BlogPost] =
    save(
      connection,
      BlogPost(
        BlogId(s"post-${UUID.randomUUID().toString.take(12)}"),
        UserId(input.userId),
        input.title.trim,
        input.summary.trim,
        input.content.trim,
        input.images,
        BlogPostStatus.PendingReview,
        now,
        now,
        None
      )
    )

  def update(connection: Connection, input: UpdateBlogPostPlannerRequest, now: Instant): IO[BlogPost] =
    details(connection, BlogPostByIdPlannerRequest(input.postId, Some(input.userId))).flatMap { previous =>
      if previous.authorUserId.value != input.userId then IO.raiseError(BlogError.BlogPostAuthorMismatch(previous.postId, UserId(input.userId)))
      else save(connection, previous.copy(title = input.title.trim, summary = input.summary.trim, content = input.content.trim, imageRefs = input.images, updatedAt = now))
    }

  def approve(connection: Connection, input: ModerateBlogPostPlannerRequest, now: Instant): IO[BlogPost] =
    details(connection, BlogPostByIdPlannerRequest(input.postId, None)).flatMap(post => save(connection, post.copy(status = BlogPostStatus.Published, updatedAt = now, publishedAt = Some(now))))

  def reject(connection: Connection, input: ModerateBlogPostPlannerRequest, now: Instant): IO[BlogPost] =
    details(connection, BlogPostByIdPlannerRequest(input.postId, None)).flatMap(post => save(connection, post.copy(status = BlogPostStatus.Rejected, updatedAt = now)))

  def archive(connection: Connection, input: BlogPostByIdPlannerRequest, now: Instant): IO[BlogPost] =
    details(connection, input).flatMap(post => save(connection, post.copy(status = BlogPostStatus.Archived, updatedAt = now)))

  def addComment(connection: Connection, input: BlogCommentPlannerRequest, now: Instant): IO[BlogPost] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into blog_comments(comment_id, post_id, author_user_id, content, status, created_at) values (?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, s"comment-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, input.postId)
        statement.setString(3, input.userId)
        statement.setString(4, input.content.trim)
        statement.setString(5, BlogCommentStatus.Visible.toString)
        statement.setTimestamp(6, Timestamp.from(now))
        statement.executeUpdate()
      }
    } *> details(connection, BlogPostByIdPlannerRequest(input.postId, Some(input.userId)))

  def deleteComment(connection: Connection, input: DeleteBlogCommentPlannerRequest): IO[BlogPost] =
    IO.blocking {
      val postId = PlainSqlSupport.withStatement(connection, "select post_id, author_user_id from blog_comments where comment_id = ?") { statement =>
        statement.setString(1, input.commentId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then
            if resultSet.getString("author_user_id") != input.userId then throw BlogError.BlogCommentAuthorMismatch(BlogCommentId(input.commentId), UserId(input.userId))
            resultSet.getString("post_id")
          else throw BlogError.BlogCommentWasNotFound(BlogCommentId(input.commentId))
        finally resultSet.close()
      }
      PlainSqlSupport.withStatement(connection, "update blog_comments set status = ? where comment_id = ?") { statement =>
        statement.setString(1, BlogCommentStatus.Deleted.toString)
        statement.setString(2, input.commentId)
        statement.executeUpdate()
      }
      postId
    }.flatMap(postId => details(connection, BlogPostByIdPlannerRequest(postId, Some(input.userId))))

  def like(connection: Connection, input: BlogLikePlannerRequest, now: Instant): IO[BlogPost] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into blog_likes(like_id, post_id, user_id, created_at) values (?, ?, ?, ?) on conflict (post_id, user_id) do nothing") { statement =>
        statement.setString(1, s"like-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, input.postId)
        statement.setString(3, input.userId)
        statement.setTimestamp(4, Timestamp.from(now))
        statement.executeUpdate()
      }
    } *> details(connection, BlogPostByIdPlannerRequest(input.postId, Some(input.userId)))

  def unlike(connection: Connection, input: BlogLikePlannerRequest): IO[BlogPost] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "delete from blog_likes where post_id = ? and user_id = ?") { statement =>
        statement.setString(1, input.postId)
        statement.setString(2, input.userId)
        statement.executeUpdate()
      }
    } *> details(connection, BlogPostByIdPlannerRequest(input.postId, Some(input.userId)))

  private def save(connection: Connection, post: BlogPost): IO[BlogPost] =
    IO.blocking {
      val updatedRows = PlainSqlSupport.withStatement(
        connection,
        "update blog_posts set author_user_id = ?, title = ?, summary = ?, content = ?, status = ?, created_at = ?, updated_at = ?, published_at = ? where post_id = ?"
      ) { statement =>
        setPost(statement, post, 1)
        statement.setString(9, post.postId.value)
        statement.executeUpdate()
      }
      if updatedRows == 0 then
        PlainSqlSupport.withStatement(
          connection,
          "insert into blog_posts(post_id, author_user_id, title, summary, content, status, created_at, updated_at, published_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        ) { statement =>
          statement.setString(1, post.postId.value)
          setPost(statement, post, 2)
          statement.executeUpdate()
        }
      PlainSqlSupport.withStatement(connection, "delete from blog_post_images where post_id = ?") { statement =>
        statement.setString(1, post.postId.value)
        statement.executeUpdate()
      }
      post.imageRefs.sortBy(_.sortOrder).foreach { image =>
        PlainSqlSupport.withStatement(connection, "insert into blog_post_images(image_id, post_id, public_url, original_file_name, sort_order, created_at) values (?, ?, ?, ?, ?, ?)") { statement =>
          statement.setString(1, image.imageId.value)
          statement.setString(2, post.postId.value)
          statement.setString(3, image.publicUrl)
          statement.setString(4, image.originalFileName)
          statement.setInt(5, image.sortOrder)
          statement.setTimestamp(6, Timestamp.from(image.createdAt))
          statement.executeUpdate()
        }
      }
      post
    }

  private def queryPosts(connection: Connection, sql: String, values: List[String]): List[BlogPost] =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
      PlainSqlSupport.queryList(statement)(readPost(connection))
    }

  private def readPost(connection: Connection)(resultSet: ResultSet): BlogPost =
    val postId = BlogId(resultSet.getString("post_id"))
    BlogPost(
      postId,
      UserId(resultSet.getString("author_user_id")),
      resultSet.getString("title"),
      resultSet.getString("summary"),
      resultSet.getString("content"),
      loadImages(connection, postId),
      BlogPostStatus.fromText(resultSet.getString("status")),
      resultSet.getTimestamp("created_at").toInstant,
      resultSet.getTimestamp("updated_at").toInstant,
      Option(resultSet.getTimestamp("published_at")).map(_.toInstant)
    )

  private def loadImages(connection: Connection, postId: BlogId): List[BlogImageRef] =
    PlainSqlSupport.withStatement(connection, "select image_id, public_url, original_file_name, sort_order, created_at from blog_post_images where post_id = ? order by sort_order asc") { statement =>
      statement.setString(1, postId.value)
      PlainSqlSupport.queryList(statement) { resultSet =>
        BlogImageRef(BlogImageId(resultSet.getString("image_id")), resultSet.getString("public_url"), resultSet.getString("original_file_name"), resultSet.getInt("sort_order"), resultSet.getTimestamp("created_at").toInstant)
      }
    }

  private def setPost(statement: java.sql.PreparedStatement, post: BlogPost, start: Int): Unit =
    statement.setString(start, post.authorUserId.value)
    statement.setString(start + 1, post.title)
    statement.setString(start + 2, post.summary)
    statement.setString(start + 3, post.content)
    statement.setString(start + 4, post.status.toString)
    statement.setTimestamp(start + 5, Timestamp.from(post.createdAt))
    statement.setTimestamp(start + 6, Timestamp.from(post.updatedAt))
    statement.setTimestamp(start + 7, post.publishedAt.map(Timestamp.from).orNull)
