package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object BlogSuggestionsPlanner extends ConnectionApiPlan[BlogSuggestionRequest, BlogSuggestionListPlannerResponse]:
  override val name: String = "BlogSuggestionsPlanner"
  override def plan(input: BlogSuggestionRequest, connection: Connection): IO[BlogSuggestionListPlannerResponse] =
    BlogPlannerPlainSql.suggestions(connection, input)

object ListBlogPostsPlanner extends ConnectionApiPlan[ListBlogPostsPlannerRequest, BlogPostListPlannerResponse]:
  override val name: String = "ListBlogPostsPlanner"
  override def plan(input: ListBlogPostsPlannerRequest, connection: Connection): IO[BlogPostListPlannerResponse] =
    val scope = input.scope.map(_.trim.toLowerCase).getOrElse("home")
    val status =
      scope match
        case "mine" | "drafts" => None
        case "profile" => Some(BlogPostStatus.Published.toString)
        case "favorites" => Some(BlogPostStatus.Published.toString)
        case "pending" => Some(BlogPostStatus.Draft.toString)
        case "reviewed" => None
        case _ => Some(BlogPostStatus.Published.toString)
    val authorUserId = Option.when(scope == "mine" || scope == "drafts" || scope == "profile")(input.userId.getOrElse(throw new IllegalArgumentException("userId is required")))
    val favoriteUserId = Option.when(scope == "favorites")(input.userId.getOrElse(throw new IllegalArgumentException("userId is required")))
    BlogPlannerPlainSql
      .listPosts(connection, status, authorUserId, input.q, input.tagType, input.tagValue, input.travelCity, input.travelCities, favoriteUserId, input.userId)
      .map(posts => BlogPostListPlannerResponse(if scope == "drafts" then posts.filter(_.status == BlogPostStatus.Draft.toString) else posts))

object GetBlogPostPlanner extends ConnectionApiPlan[BlogPostByIdPlannerRequest, BlogPostResponse]:
  override val name: String = "GetBlogPostPlanner"
  override def plan(input: BlogPostByIdPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.findPost(connection, input.postId, input.userId)

object SaveBlogDraftPlanner extends ConnectionApiPlan[SaveBlogDraftPlannerRequest, BlogPostResponse]:
  override val name: String = "SaveBlogDraftPlanner"
  override def plan(input: SaveBlogDraftPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    validateDraft(input, requireTags = false) *>
      BlogPlannerPlainSql
        .insertPost(connection, input, BlogPostStatus.Draft.toString, Instant.now())
        .flatMap(postId => BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId)))

object CreateBlogPostPlanner extends ConnectionApiPlan[CreateBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "CreateBlogPostPlanner"
  override def plan(input: CreateBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val draft = SaveBlogDraftPlannerRequest(
      userId = input.userId,
      postId = None,
      title = input.title,
      summary = input.summary,
      coverText = input.coverText.getOrElse(input.summary),
      content = input.content,
      images = input.images,
      tags = input.tags.getOrElse(Nil),
      travelCity = input.travelCity,
      travelCities = input.travelCities
    )
    validateDraft(draft, requireTags = true) *>
      BlogPlannerPlainSql
        .insertPost(connection, draft, BlogPostStatus.Published.toString, Instant.now())
        .flatMap(postId => BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId)))

object UpdateBlogPostPlanner extends ConnectionApiPlan[UpdateBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "UpdateBlogPostPlanner"
  override def plan(input: UpdateBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.findPostAuthor(connection, input.postId).flatMap { authorUserId =>
      if authorUserId != input.userId then IO.raiseError(new IllegalArgumentException("只有作者可以编辑这篇帖子"))
      else
        val draft = SaveBlogDraftPlannerRequest(
          userId = input.userId,
          postId = Some(input.postId),
          title = input.title,
          summary = input.summary,
          coverText = input.coverText.getOrElse(input.summary),
          content = input.content,
          images = input.images,
          tags = input.tags.getOrElse(Nil),
          travelCity = input.travelCity,
          travelCities = input.travelCities
        )
        validateDraft(draft, requireTags = true) *>
          BlogPlannerPlainSql
            .insertPost(connection, draft, BlogPostStatus.Published.toString, Instant.now())
            .flatMap(postId => BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId)))
    }

object PublishBlogPostPlanner extends ConnectionApiPlan[PublishBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "PublishBlogPostPlanner"
  override def plan(input: PublishBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.findPostAuthor(connection, input.postId).flatMap { authorUserId =>
      if authorUserId != input.userId then IO.raiseError(new IllegalArgumentException("只有作者可以发布这篇草稿"))
      else
        BlogPlannerPlainSql.updatePostStatus(connection, input.postId, BlogPostStatus.Published.toString, Instant.now()) *>
          BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
    }

object ApproveBlogPostPlanner extends ConnectionApiPlan[ModerateBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "ApproveBlogPostPlanner"
  override def plan(input: ModerateBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.updatePostStatus(connection, input.postId, BlogPostStatus.Published.toString, Instant.now()) *>
      BlogPlannerPlainSql.findPost(connection, input.postId, None)

object RejectBlogPostPlanner extends ConnectionApiPlan[ModerateBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "RejectBlogPostPlanner"
  override def plan(input: ModerateBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.updatePostStatus(connection, input.postId, BlogPostStatus.Hidden.toString, Instant.now()) *>
      BlogPlannerPlainSql.findPost(connection, input.postId, None)

object ArchiveBlogPostPlanner extends ConnectionApiPlan[BlogPostByIdPlannerRequest, BlogPostResponse]:
  override val name: String = "ArchiveBlogPostPlanner"
  override def plan(input: BlogPostByIdPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val userId = input.userId.getOrElse(throw new IllegalArgumentException("userId is required"))
    BlogPlannerPlainSql.findPostAuthor(connection, input.postId).flatMap { authorUserId =>
      if authorUserId != userId then IO.raiseError(new IllegalArgumentException("只有作者可以隐藏这篇帖子"))
      else
        BlogPlannerPlainSql.updatePostStatus(connection, input.postId, BlogPostStatus.Hidden.toString, Instant.now()) *>
          BlogPlannerPlainSql.findPost(connection, input.postId, Some(userId))
    }

object AddBlogCommentPlanner extends ConnectionApiPlan[BlogCommentPlannerRequest, BlogPostResponse]:
  override val name: String = "AddBlogCommentPlanner"
  override def plan(input: BlogCommentPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val normalizedContent = input.content.trim
    if normalizedContent.isEmpty then IO.raiseError(new IllegalArgumentException("评论内容不能为空"))
    else
      val now = Instant.now()
      for
        post <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
        commentId <- BlogPlannerPlainSql.insertComment(connection, input.copy(content = normalizedContent), now)
        _ <-
          if post.post.authorUserId == input.userId then IO.unit
          else BlogPlannerPlainSql.insertNotification(connection, post.post.authorUserId, Some(input.userId), "postCommented", Some(input.postId), Some(commentId), s"${post.post.authorDisplayName} 的帖子有了新评论", now)
        updated <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
      yield updated

object DeleteBlogCommentPlanner extends ConnectionApiPlan[DeleteBlogCommentPlannerRequest, BlogPostResponse]:
  override val name: String = "DeleteBlogCommentPlanner"
  override def plan(input: DeleteBlogCommentPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.findCommentBinding(connection, input.commentId).flatMap { case (postId, authorUserId) =>
      if authorUserId != input.userId then IO.raiseError(new IllegalArgumentException("只能删除自己的评论"))
      else BlogPlannerPlainSql.updateCommentStatus(connection, input.commentId, BlogCommentStatus.Deleted.toString) *> BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId))
    }

object LikeBlogPostPlanner extends ConnectionApiPlan[BlogLikePlannerRequest, BlogPostResponse]:
  override val name: String = "LikeBlogPostPlanner"
  override def plan(input: BlogLikePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val now = Instant.now()
    for
      post <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
      _ <- BlogPlannerPlainSql.likePost(connection, input.postId, input.userId, now)
      _ <-
        if post.post.authorUserId == input.userId then IO.unit
        else BlogPlannerPlainSql.insertNotification(connection, post.post.authorUserId, Some(input.userId), "postLiked", Some(input.postId), None, "有人喜欢了你的帖子", now)
      updated <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
    yield updated

object UnlikeBlogPostPlanner extends ConnectionApiPlan[BlogLikePlannerRequest, BlogPostResponse]:
  override val name: String = "UnlikeBlogPostPlanner"
  override def plan(input: BlogLikePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.unlikePost(connection, input.postId, input.userId) *> BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))

object LikeBlogCommentPlanner extends ConnectionApiPlan[BlogCommentLikePlannerRequest, BlogPostResponse]:
  override val name: String = "LikeBlogCommentPlanner"
  override def plan(input: BlogCommentLikePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val now = Instant.now()
    for
      binding <- BlogPlannerPlainSql.findCommentBinding(connection, input.commentId)
      (postId, commentAuthorUserId) = binding
      _ <- BlogPlannerPlainSql.likeComment(connection, input.commentId, input.userId, now)
      _ <-
        if commentAuthorUserId == input.userId then IO.unit
        else BlogPlannerPlainSql.insertNotification(connection, commentAuthorUserId, Some(input.userId), "commentLiked", Some(postId), Some(input.commentId), "有人赞了你的评论", now)
      updated <- BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId))
    yield updated

object UnlikeBlogCommentPlanner extends ConnectionApiPlan[BlogCommentLikePlannerRequest, BlogPostResponse]:
  override val name: String = "UnlikeBlogCommentPlanner"
  override def plan(input: BlogCommentLikePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    for
      binding <- BlogPlannerPlainSql.findCommentBinding(connection, input.commentId)
      (postId, _) = binding
      _ <- BlogPlannerPlainSql.unlikeComment(connection, input.commentId, input.userId)
      updated <- BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId))
    yield updated

object FavoriteBlogPostPlanner extends ConnectionApiPlan[BlogFavoritePlannerRequest, BlogPostResponse]:
  override val name: String = "FavoriteBlogPostPlanner"
  override def plan(input: BlogFavoritePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val now = Instant.now()
    for
      post <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
      _ <- BlogPlannerPlainSql.favoritePost(connection, input.postId, input.userId, now)
      _ <-
        if post.post.authorUserId == input.userId then IO.unit
        else BlogPlannerPlainSql.insertNotification(connection, post.post.authorUserId, Some(input.userId), "postFavorited", Some(input.postId), None, "有人收藏了你的帖子", now)
      updated <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
    yield updated

object UnfavoriteBlogPostPlanner extends ConnectionApiPlan[BlogFavoritePlannerRequest, BlogPostResponse]:
  override val name: String = "UnfavoriteBlogPostPlanner"
  override def plan(input: BlogFavoritePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.unfavoritePost(connection, input.postId, input.userId) *> BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))

object FollowBlogUserPlanner extends ConnectionApiPlan[BlogUserInteractionPlannerRequest, BlogProfilePlannerResponse]:
  override val name: String = "FollowBlogUserPlanner"
  override def plan(input: BlogUserInteractionPlannerRequest, connection: Connection): IO[BlogProfilePlannerResponse] =
    val now = Instant.now()
    if input.userId == input.targetUserId then IO.raiseError(new IllegalArgumentException("不能关注自己"))
    else
      BlogPlannerPlainSql.followUser(connection, input.userId, input.targetUserId, now) *>
        BlogPlannerPlainSql.insertNotification(connection, input.targetUserId, Some(input.userId), "userFollowed", None, None, "有人关注了你", now) *>
        BlogPlannerPlainSql.findProfile(connection, BlogProfilePlannerRequest(Some(input.userId), input.targetUserId))

object BlockBlogUserPlanner extends ConnectionApiPlan[BlogUserInteractionPlannerRequest, BlogProfilePlannerResponse]:
  override val name: String = "BlockBlogUserPlanner"
  override def plan(input: BlogUserInteractionPlannerRequest, connection: Connection): IO[BlogProfilePlannerResponse] =
    if input.userId == input.targetUserId then IO.raiseError(new IllegalArgumentException("不能屏蔽自己"))
    else BlogPlannerPlainSql.blockUser(connection, input.userId, input.targetUserId, Instant.now()) *> BlogPlannerPlainSql.findProfile(connection, BlogProfilePlannerRequest(Some(input.userId), input.targetUserId))

object ListBlogNotificationsPlanner extends ConnectionApiPlan[ListBlogNotificationsPlannerRequest, BlogNotificationListPlannerResponse]:
  override val name: String = "ListBlogNotificationsPlanner"
  override def plan(input: ListBlogNotificationsPlannerRequest, connection: Connection): IO[BlogNotificationListPlannerResponse] =
    BlogPlannerPlainSql.listNotifications(connection, input.userId)

object GetBlogProfilePlanner extends ConnectionApiPlan[BlogProfilePlannerRequest, BlogProfilePlannerResponse]:
  override val name: String = "GetBlogProfilePlanner"
  override def plan(input: BlogProfilePlannerRequest, connection: Connection): IO[BlogProfilePlannerResponse] =
    BlogPlannerPlainSql.findProfile(connection, input)

object UpdateBlogProfilePrivacyPlanner extends ConnectionApiPlan[UpdateBlogProfilePrivacyPlannerRequest, BlogProfilePlannerResponse]:
  override val name: String = "UpdateBlogProfilePrivacyPlanner"
  override def plan(input: UpdateBlogProfilePrivacyPlannerRequest, connection: Connection): IO[BlogProfilePlannerResponse] =
    for
      _ <- BlogPlannerPlainSql.updateProfilePrivacy(connection, input.userId, input.hideRelations, Instant.now())
      profile <- BlogPlannerPlainSql.findProfile(connection, BlogProfilePlannerRequest(Some(input.userId), input.userId))
    yield profile

object ListBlogFollowersPlanner extends ConnectionApiPlan[ListBlogProfileUsersPlannerRequest, BlogProfileUserListPlannerResponse]:
  override val name: String = "ListBlogFollowersPlanner"
  override def plan(input: ListBlogProfileUsersPlannerRequest, connection: Connection): IO[BlogProfileUserListPlannerResponse] =
    for
      hidden <- BlogPlannerPlainSql.profileRelationsHidden(connection, input.profileUserId, input.viewerUserId)
      response <- if hidden then IO.pure(BlogProfileUserListPlannerResponse(Nil)) else BlogPlannerPlainSql.listFollowers(connection, input)
    yield response

object ListBlogFollowingPlanner extends ConnectionApiPlan[ListBlogProfileUsersPlannerRequest, BlogProfileUserListPlannerResponse]:
  override val name: String = "ListBlogFollowingPlanner"
  override def plan(input: ListBlogProfileUsersPlannerRequest, connection: Connection): IO[BlogProfileUserListPlannerResponse] =
    for
      hidden <- BlogPlannerPlainSql.profileRelationsHidden(connection, input.profileUserId, input.viewerUserId)
      response <- if hidden then IO.pure(BlogProfileUserListPlannerResponse(Nil)) else BlogPlannerPlainSql.listFollowing(connection, input)
    yield response

private def validateDraft(input: SaveBlogDraftPlannerRequest, requireTags: Boolean): IO[Unit] =
  val title = input.title.trim
  val coverText = input.coverText.trim
  val content = input.content.trim
  val normalizedCities = input.travelCities.getOrElse(Nil).map(_.trim).filter(_.nonEmpty)
  if title.isEmpty then IO.raiseError(new IllegalArgumentException("标题不能为空"))
  else if coverText.isEmpty then IO.raiseError(new IllegalArgumentException("封面语不能为空"))
  else if content.isEmpty then IO.raiseError(new IllegalArgumentException("正文不能为空"))
  else if input.images.isEmpty then IO.raiseError(new IllegalArgumentException("至少需要一张图片作为封面"))
  else if requireTags && input.tags.isEmpty then IO.raiseError(new IllegalArgumentException("发布前请选择标签"))
  else if requireTags && normalizedCities.isEmpty && input.travelCity.forall(_.trim.isEmpty) then IO.raiseError(new IllegalArgumentException("发布前请选择旅行城市"))
  else IO.unit
