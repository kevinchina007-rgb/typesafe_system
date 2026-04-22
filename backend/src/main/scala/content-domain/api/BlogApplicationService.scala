package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.api.storage.{ContentImageCollection, ContentImageStorage}
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.identity.domain.{User, UserError, UserRepository}
import com.typesafe.travel.shared.kernel.*

import java.time.Instant

// Blog application service 璐熻矗鎶?Blog 鏍稿績鏁版嵁缁勮鎴愬墠绔洿鎺ュ彲鐢ㄧ殑灞曠ず妯″瀷銆?
// 鐐硅禐鏁般€佽瘎璁烘暟銆佷綔鑰呭睍绀轰俊鎭€佹悳绱㈡憳瑕佺瓑瀛楁閮藉湪杩欎竴灞傝绠楋紝涓嶅洖鍐欐垚鏉冨▉瀛楁銆?
enum BlogPostScope:
  case Latest, Mine

enum BlogModerationScope:
  case Pending, Reviewed

enum BlogImageUploadError(val message: String) extends DomainError:
  case ImageWasMissing
      extends BlogImageUploadError("Blog image file was missing")
  case ImageFileTypeWasInvalid(contentTypeValue: String)
      extends BlogImageUploadError(s"Blog image content type '$contentTypeValue' is not supported")
  case ImageFileExtensionWasInvalid(fileNameValue: String)
      extends BlogImageUploadError(s"Blog image file '$fileNameValue' is not supported")
  case ImageFileWasTooLarge(maximumBytes: Long, actualBytes: Long)
      extends BlogImageUploadError(s"Blog image exceeded the maximum size of $maximumBytes bytes with $actualBytes bytes")

final case class BlogPostView(
    postId: BlogId,
    authorUserId: UserId,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    title: String,
    summary: String,
    status: String,
    createdAt: Instant,
    updatedAt: Instant,
    publishedAt: Option[Instant],
    commentCount: Int,
    likeCount: Int,
    likedByCurrentUser: Boolean,
    isMyPost: Boolean,
    canEdit: Boolean,
    canArchive: Boolean,
    imageRefs: List[BlogImageRef],
    searchResultSnippet: Option[String]
)

final case class BlogCommentView(
    commentId: BlogCommentId,
    postId: BlogId,
    authorUserId: UserId,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    content: String,
    createdAt: Instant,
    isMyComment: Boolean,
    canDelete: Boolean
)

// 璇︽儏椤垫妸鍒楄〃鎽樿鍜屾鏂?璇勮鍚堝湪涓€璧疯繑鍥烇紝閬垮厤鍓嶇鍐嶆嫾澶氭璇锋眰缁撴灉銆?
final case class BlogPostDetailsView(post: BlogPostView, content: String, comments: List[BlogCommentView])

trait BlogApplicationService[F[_]]:
  def listPosts(currentUserId: Option[UserId], scope: BlogPostScope, query: Option[String]): F[List[BlogPostView]]
  def listPostsForModeration(scope: BlogModerationScope): F[List[BlogPostView]]
  def suggestPublishedPosts(query: String): F[List[SearchSuggestion]]
  def getPost(postId: BlogId, currentUserId: Option[UserId]): F[BlogPostDetailsView]
  def createPendingReviewPost(authorUserId: UserId, title: String, summary: String, content: String, imageRefs: List[BlogImageRef], now: Instant): F[BlogPostDetailsView]
  def updatePost(postId: BlogId, authorUserId: UserId, title: String, summary: String, content: String, imageRefs: List[BlogImageRef], now: Instant): F[BlogPostDetailsView]
  def archivePost(postId: BlogId, authorUserId: UserId, now: Instant): F[BlogPostDetailsView]
  def approvePost(postId: BlogId, approvedAt: Instant): F[BlogPostDetailsView]
  def rejectPost(postId: BlogId, rejectedAt: Instant): F[BlogPostDetailsView]
  def addComment(postId: BlogId, authorUserId: UserId, content: String, now: Instant): F[BlogPostDetailsView]
  def deleteComment(commentId: BlogCommentId, authorUserId: UserId): F[BlogPostDetailsView]
  def likePost(postId: BlogId, userId: UserId, now: Instant): F[BlogPostDetailsView]
  def unlikePost(postId: BlogId, userId: UserId): F[BlogPostDetailsView]
  def uploadImage(authorUserId: UserId, originalFileName: String, contentTypeValue: String, fileBytes: Array[Byte], now: Instant): F[BlogImageRef]

final class LiveBlogApplicationService[F[_]: MonadThrow](
    blogRepository: BlogRepository[F],
    userRepository: UserRepository[F],
    contentImageStorage: ContentImageStorage[F]
) extends BlogApplicationService[F]:
  private val allowedContentTypes =
    Set("image/png", "image/jpeg", "image/jpg", "image/webp")

  private val allowedFileExtensions =
    Set("png", "jpg", "jpeg", "webp")

  private val maximumImageBytes: Long = 5L * 1024L * 1024L

  override def listPosts(currentUserId: Option[UserId], scope: BlogPostScope, query: Option[String]): F[List[BlogPostView]] =
    // latest / mine 鍏变韩鍚屼竴濂?view 鎶曞奖閫昏緫锛屽樊鍒彧鍦ㄥ彲璇昏寖鍥淬€?
    scope match
      case BlogPostScope.Latest =>
        for
          posts <- query.filter(_.trim.nonEmpty) match
            case Some(searchQuery) => blogRepository.searchPublishedPosts(searchQuery)
            case None              => blogRepository.listPublishedPosts
          sortedPosts = sortPostsByQuery(posts, query)
          views <- sortedPosts.traverse(toPostView(_, currentUserId, query))
        yield views
      case BlogPostScope.Mine =>
        currentUserId match
          case Some(authorUserId) =>
            for
              _ <- requireUser(authorUserId)
              posts <- query.filter(_.trim.nonEmpty) match
                case Some(searchQuery) => blogRepository.searchPostsByAuthorUserId(authorUserId, searchQuery)
                case None              => blogRepository.listPostsByAuthorUserId(authorUserId)
              sortedPosts = sortPostsByQuery(posts, query)
              views <- sortedPosts.traverse(toPostView(_, currentUserId, query))
            yield views
          case None =>
            MonadThrow[F].raiseError(UserError.UserWasNotFound(UserId("guest")))

  override def listPostsForModeration(scope: BlogModerationScope): F[List[BlogPostView]] =
    for
      posts <- blogRepository.listAllPosts
      filteredPosts = scope match
        case BlogModerationScope.Pending =>
          posts.filter(post => post.status == BlogPostStatus.PendingReview)
        case BlogModerationScope.Reviewed =>
          posts.filter(post =>
            post.status == BlogPostStatus.Published || post.status == BlogPostStatus.Rejected
          )
      views <- filteredPosts.traverse(toPostView(_, None, None))
    yield views

  override def suggestPublishedPosts(query: String): F[List[SearchSuggestion]] =
    SearchRanking.usableKeyword(query) match
      case None => MonadThrow[F].pure(List.empty)
      case Some(normalizedKeyword) =>
        blogRepository
          .searchPublishedPosts(normalizedKeyword)
          .map(sortPostsByQuery(_, Some(normalizedKeyword)))
          .map(
            _.map { post =>
              SearchSuggestion(
                resourceType = SearchResourceType.Blog,
                value = post.title,
                title = post.title,
                subtitle = buildSearchSnippet(post, normalizedKeyword).getOrElse(post.summary.take(80)),
                score = blogSearchScore(post, normalizedKeyword)
              )
            }
          )
          .map(_.filter(_.score > 0).take(8))

  override def getPost(postId: BlogId, currentUserId: Option[UserId]): F[BlogPostDetailsView] =
    for
      post <- findReadablePost(postId, currentUserId)
      postView <- toPostView(post, currentUserId, None)
      comments <- buildCommentViews(post.postId, currentUserId)
    yield BlogPostDetailsView(postView, post.content, comments)

  override def createPendingReviewPost(authorUserId: UserId, title: String, summary: String, content: String, imageRefs: List[BlogImageRef], now: Instant): F[BlogPostDetailsView] =
    for
      _ <- requireUser(authorUserId)
      postId <- blogRepository.nextPostId
      post <- MonadThrow[F].fromEither(createPendingReviewBlogPost(postId, authorUserId, title, summary, content, imageRefs, now))
      _ <- blogRepository.savePost(post)
      view <- getPost(post.postId, Some(authorUserId))
    yield view

  override def updatePost(postId: BlogId, authorUserId: UserId, title: String, summary: String, content: String, imageRefs: List[BlogImageRef], now: Instant): F[BlogPostDetailsView] =
    for
      _ <- requireUser(authorUserId)
      post <- blogRepository.findPostById(postId).flatMap(_.liftTo[F](BlogError.BlogPostWasNotFound(postId)))
      updatedPost <- MonadThrow[F].fromEither(updateBlogPost(post, authorUserId, title, summary, content, imageRefs, now))
      _ <- blogRepository.savePost(updatedPost)
      view <- getPost(postId, Some(authorUserId))
    yield view

  override def archivePost(postId: BlogId, authorUserId: UserId, now: Instant): F[BlogPostDetailsView] =
    for
      _ <- requireUser(authorUserId)
      post <- blogRepository.findPostById(postId).flatMap(_.liftTo[F](BlogError.BlogPostWasNotFound(postId)))
      archivedPost <- MonadThrow[F].fromEither(archiveBlogPost(post, authorUserId, now))
      _ <- blogRepository.savePost(archivedPost)
      view <- getPost(postId, Some(authorUserId))
    yield view

  override def approvePost(postId: BlogId, approvedAt: Instant): F[BlogPostDetailsView] =
    for
      post <- blogRepository.findPostById(postId).flatMap(_.liftTo[F](BlogError.BlogPostWasNotFound(postId)))
      approvedPost = approveBlogPost(post, approvedAt)
      _ <- blogRepository.savePost(approvedPost)
      view <- getPost(postId, None)
    yield view

  override def rejectPost(postId: BlogId, rejectedAt: Instant): F[BlogPostDetailsView] =
    for
      post <- blogRepository.findPostById(postId).flatMap(_.liftTo[F](BlogError.BlogPostWasNotFound(postId)))
      rejectedPost = rejectBlogPost(post, rejectedAt)
      _ <- blogRepository.savePost(rejectedPost)
      view <- getPost(postId, None)
    yield view

  override def addComment(postId: BlogId, authorUserId: UserId, content: String, now: Instant): F[BlogPostDetailsView] =
    for
      _ <- requireUser(authorUserId)
      _ <- findPublishedPost(postId)
      commentId <- blogRepository.nextCommentId
      comment <- MonadThrow[F].fromEither(createVisibleBlogComment(commentId, postId, authorUserId, content, now))
      _ <- blogRepository.saveComment(comment)
      view <- getPost(postId, Some(authorUserId))
    yield view

  override def deleteComment(commentId: BlogCommentId, authorUserId: UserId): F[BlogPostDetailsView] =
    for
      _ <- requireUser(authorUserId)
      comment <- blogRepository.findCommentById(commentId).flatMap(_.liftTo[F](BlogError.BlogCommentWasNotFound(commentId)))
      deletedComment <- MonadThrow[F].fromEither(deleteBlogComment(comment, authorUserId))
      _ <- blogRepository.saveComment(deletedComment)
      view <- getPost(comment.postId, Some(authorUserId))
    yield view

  override def likePost(postId: BlogId, userId: UserId, now: Instant): F[BlogPostDetailsView] =
    for
      _ <- requireUser(userId)
      _ <- findPublishedPost(postId)
      existingLike <- blogRepository.findLikeByPostIdAndUserId(postId, userId)
      _ <- existingLike match
        case Some(_) => MonadThrow[F].unit
        case None =>
          for
            likeId <- blogRepository.nextLikeId
            _ <- blogRepository.saveLike(createBlogLike(likeId, postId, userId, now))
          yield ()
      view <- getPost(postId, Some(userId))
    yield view

  override def unlikePost(postId: BlogId, userId: UserId): F[BlogPostDetailsView] =
    for
      _ <- requireUser(userId)
      _ <- findPublishedPost(postId)
      _ <- blogRepository.deleteLike(postId, userId)
      view <- getPost(postId, Some(userId))
    yield view

  override def uploadImage(authorUserId: UserId, originalFileName: String, contentTypeValue: String, fileBytes: Array[Byte], now: Instant): F[BlogImageRef] =
    for
      _ <- requireUser(authorUserId)
      _ <- validateImagePresence(originalFileName, fileBytes)
      _ <- validateContentType(contentTypeValue)
      fileExtension <- validateFileExtension(originalFileName)
      _ <- validateFileSize(fileBytes)
      storedFile <- contentImageStorage.storeImage(ContentImageCollection.Blog, authorUserId, originalFileName, fileExtension, fileBytes)
      imageId <- blogRepository.nextPostImageId
    yield BlogImageRef(
      imageId = imageId,
      publicUrl = storedFile.publicUrl,
      originalFileName = storedFile.originalFileName,
      sortOrder = 0,
      createdAt = now
    )

  private def validateImagePresence(originalFileName: String, fileBytes: Array[Byte]): F[Unit] =
    if originalFileName.trim.nonEmpty && fileBytes.nonEmpty then MonadThrow[F].unit
    else MonadThrow[F].raiseError(BlogImageUploadError.ImageWasMissing)

  private def validateContentType(contentTypeValue: String): F[String] =
    val normalizedContentType = contentTypeValue.trim.toLowerCase
    if allowedContentTypes.contains(normalizedContentType) then MonadThrow[F].pure(normalizedContentType)
    else MonadThrow[F].raiseError(BlogImageUploadError.ImageFileTypeWasInvalid(contentTypeValue))

  private def validateFileExtension(originalFileName: String): F[String] =
    val normalizedFileName = originalFileName.trim.toLowerCase
    val extensionValue = normalizedFileName.split('.').lastOption.getOrElse("")
    if allowedFileExtensions.contains(extensionValue) then MonadThrow[F].pure(extensionValue)
    else MonadThrow[F].raiseError(BlogImageUploadError.ImageFileExtensionWasInvalid(originalFileName))

  private def validateFileSize(fileBytes: Array[Byte]): F[Unit] =
    if fileBytes.length.toLong <= maximumImageBytes then MonadThrow[F].unit
    else MonadThrow[F].raiseError(BlogImageUploadError.ImageFileWasTooLarge(maximumImageBytes, fileBytes.length.toLong))

  private def findPublishedPost(postId: BlogId): F[BlogPost] =
    blogRepository.findPostById(postId).flatMap {
      case Some(post) if post.isPubliclyVisible => MonadThrow[F].pure(post)
      case Some(post)                           => MonadThrow[F].raiseError(BlogError.BlogPostWasNotPublished(postId, post.status))
      case None                                 => MonadThrow[F].raiseError(BlogError.BlogPostWasNotFound(postId))
    }

  private def findReadablePost(postId: BlogId, currentUserId: Option[UserId]): F[BlogPost] =
    blogRepository.findPostById(postId).flatMap {
      case Some(post) if post.isPubliclyVisible => MonadThrow[F].pure(post)
      case Some(post) if currentUserId.contains(post.authorUserId) => MonadThrow[F].pure(post)
      case Some(post) => MonadThrow[F].raiseError(BlogError.BlogPostWasNotPublished(postId, post.status))
      case None       => MonadThrow[F].raiseError(BlogError.BlogPostWasNotFound(postId))
    }

  private def requireUser(userId: UserId): F[User] =
    userRepository.findByUserId(userId).flatMap(_.liftTo[F](UserError.UserWasNotFound(userId)))

  private def buildCommentViews(postId: BlogId, currentUserId: Option[UserId]): F[List[BlogCommentView]] =
    for
      comments <- blogRepository.listCommentsByPostId(postId)
      // 璇勮琛ㄩ噷娌℃湁 authorDisplayName / canDelete 杩欑被鍓嶇瀛楁锛岃繖閲岀粺涓€鎶曞奖鍑烘潵銆?
      visibleComments = comments.filter(_.isVisible)
      views <- visibleComments.traverse { comment =>
        loadAuthor(comment.authorUserId).map { author =>
          BlogCommentView(
            commentId = comment.commentId,
            postId = comment.postId,
            authorUserId = comment.authorUserId,
            authorDisplayName = author.userDisplayName.value,
            authorAvatarUrl = author.avatarUrl.map(_.value),
            content = comment.content,
            createdAt = comment.createdAt,
            isMyComment = currentUserId.contains(comment.authorUserId),
            canDelete = currentUserId.contains(comment.authorUserId) && comment.isVisible
          )
        }
      }
    yield views

  private def toPostView(post: BlogPost, currentUserId: Option[UserId], query: Option[String]): F[BlogPostView] =
    // 杩欓噷鏄?Blog core data -> BlogPostView 鐨勪富瑕佹敹鍙ｇ偣銆?
    // 鏄惁鎴戠殑鏂囩珷銆佹槸鍚﹀凡鐐硅禐銆佽瘎璁烘暟銆佹悳绱㈡憳瑕佺瓑閮藉湪杩愯鏃剁敓鎴愩€?
    for
      author <- loadAuthor(post.authorUserId)
      comments <- blogRepository.listCommentsByPostId(post.postId)
      likes <- blogRepository.listLikesByPostId(post.postId)
    yield BlogPostView(
      postId = post.postId,
      authorUserId = post.authorUserId,
      authorDisplayName = author.userDisplayName.value,
      authorAvatarUrl = author.avatarUrl.map(_.value),
      title = post.title,
      summary = post.summary,
      status = post.status.toString,
      createdAt = post.createdAt,
      updatedAt = post.updatedAt,
      publishedAt = post.publishedAt,
      commentCount = comments.count(_.isVisible),
      likeCount = likes.size,
      likedByCurrentUser = currentUserId.exists(userId => likes.exists(_.userId == userId)),
      isMyPost = currentUserId.contains(post.authorUserId),
      canEdit = currentUserId.contains(post.authorUserId),
      canArchive = currentUserId.exists(post.canBeArchivedBy),
      imageRefs = post.imageRefs,
      searchResultSnippet = query.filter(_.trim.nonEmpty).flatMap(searchQuery => buildSearchSnippet(post, searchQuery))
    )

  private def loadAuthor(authorUserId: UserId): F[User] =
    userRepository.findByUserId(authorUserId).flatMap(_.liftTo[F](UserError.UserWasNotFound(authorUserId)))

  private def buildSearchSnippet(post: BlogPost, query: String): Option[String] =
    // snippet 鍙湇鍔℃悳绱㈠睍绀猴紝涓嶆槸 Blog 鎸佷箙鍖栧瓧娈点€?
    val normalizedQuery = query.trim.toLowerCase
    if normalizedQuery.isEmpty then None
    else
      List(post.title, post.summary, post.content)
        .map(_.trim)
        .find(_.toLowerCase.contains(normalizedQuery))
        .map { text =>
          val normalizedText = text.replaceAll("\\s+", " ")
          val matchIndex = normalizedText.toLowerCase.indexOf(normalizedQuery)
          if matchIndex < 0 then normalizedText.take(120)
          else
            val startIndex = math.max(0, matchIndex - 30)
            val endIndex = math.min(normalizedText.length, matchIndex + normalizedQuery.length + 60)
            normalizedText.substring(startIndex, endIndex)
        }

  private def sortPostsByQuery(posts: List[BlogPost], query: Option[String]): List[BlogPost] =
    query.filter(_.trim.nonEmpty) match
      case Some(searchQuery) => posts.sortBy(post => -blogSearchScore(post, searchQuery))
      case None              => posts

  private def blogSearchScore(post: BlogPost, query: String): Int =
    SearchRanking.weightedScore(
      query,
      post.title -> 4,
      post.summary -> 2,
      post.content -> 1
    )
