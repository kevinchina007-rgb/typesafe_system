package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.application.{BlogImageUploadError, BlogPostScope}
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.content.domain.BlogImageRef
import com.typesafe.travel.shared.kernel.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.Multipart

import java.time.Instant

trait BlogApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def blogRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "blog" / "suggestions" :? SearchQueryParamMatcher(queryValue) =>
      for
        queryText <- fromEither(queryValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("q")))
        suggestions <- blogApplicationService.suggestPublishedPosts(queryText)
        response <- okJson(SearchSuggestionListResponseDto(suggestions.map(SearchSuggestionResponseDto.fromApplication)))
      yield response

    case request @ GET -> Root / "api" / "blog" / "posts" =>
      val scope = request.params.get("scope").map(_.trim.toLowerCase).getOrElse("latest")
      val query = request.params.get("q").map(_.trim).filter(_.nonEmpty)
      val requestedUserIdF =
        if scope == "mine" then requireCurrentUserId(request).map(Some(_))
        else Async[F].pure(currentUserSessionId(request).map(_ => None).getOrElse(None))
      for
        currentUserId <- currentUserSessionId(request) match
          case Some(sessionId) => currentInstantF.flatMap(now => authApplicationService.restoreCurrentUser(sessionId, now).map(view => Some(view.user.userId)).handleError(_ => None))
          case None            => Async[F].pure(None)
        effectiveUserId <- if scope == "mine" then requireCurrentUserId(request).map(Some(_)) else Async[F].pure(currentUserId)
        posts <- blogApplicationService.listPosts(effectiveUserId, parseBlogScope(scope), query)
        response <- okJson(BlogPostListResponseDto(posts.map(BlogPostSummaryResponseDto.fromView)))
      yield response

    case request @ GET -> Root / "api" / "blog" / "posts" / postIdValue =>
      for
        currentUserId <- currentUserSessionId(request) match
          case Some(sessionId) => currentInstantF.flatMap(now => authApplicationService.restoreCurrentUser(sessionId, now).map(view => Some(view.user.userId)).handleError(_ => None))
          case None            => Async[F].pure(None)
        post <- blogApplicationService.getPost(BlogId(postIdValue), currentUserId)
        response <- okJson(BlogPostResponseDto.fromView(post))
      yield response

    case request @ POST -> Root / "api" / "blog" / "images" =>
      for
        currentUserId <- requireCurrentUserId(request)
        multipartPayload <- request.as[Multipart[F]]
        imagePart <- requireMultipartPart(multipartPayload, "image", BlogImageUploadError.ImageWasMissing)
        uploadedImage <- readUploadedBinary(imagePart, BlogImageUploadError.ImageWasMissing)
        now <- currentInstantF
        imageRef <- blogApplicationService.uploadImage(currentUserId, uploadedImage.originalFileName, uploadedImage.contentTypeValue, uploadedImage.fileBytes, now)
        response <- createdJson(ContentImageResponseDto.fromBlogImageRef(imageRef))
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" =>
      for
        currentUserId <- requireCurrentUserId(request)
        createRequest <- request.as[CreateBlogPostRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.createPublishedPost(
          currentUserId,
          createRequest.title,
          createRequest.summary,
          createRequest.content,
          parseBlogImages(createRequest.images),
          now
        )
        response <- createdJson(BlogPostResponseDto.fromView(post))
      yield response

    case request @ PATCH -> Root / "api" / "blog" / "posts" / postIdValue =>
      for
        currentUserId <- requireCurrentUserId(request)
        updateRequest <- request.as[UpdateBlogPostRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.updatePost(
          BlogId(postIdValue),
          currentUserId,
          updateRequest.title,
          updateRequest.summary,
          updateRequest.content,
          parseBlogImages(updateRequest.images),
          now
        )
        response <- okJson(BlogPostResponseDto.fromView(post))
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" / postIdValue / "archive" =>
      for
        currentUserId <- requireCurrentUserId(request)
        likeRequest <- request.as[BlogLikeRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.archivePost(BlogId(postIdValue), currentUserId, now)
        response <- okJson(BlogPostResponseDto.fromView(post))
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" / postIdValue / "comments" =>
      for
        currentUserId <- requireCurrentUserId(request)
        createRequest <- request.as[CreateBlogCommentRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.addComment(BlogId(postIdValue), currentUserId, createRequest.content, now)
        response <- createdJson(BlogPostResponseDto.fromView(post))
      yield response

    case request @ DELETE -> Root / "api" / "blog" / "comments" / commentIdValue =>
      for
        currentUserId <- requireCurrentUserId(request)
        deleteRequest <- request.as[DeleteBlogCommentRequestDto]
        post <- blogApplicationService.deleteComment(BlogCommentId(commentIdValue), currentUserId)
        response <- okJson(BlogPostResponseDto.fromView(post))
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" / postIdValue / "likes" =>
      for
        currentUserId <- requireCurrentUserId(request)
        likeRequest <- request.as[BlogLikeRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.likePost(BlogId(postIdValue), currentUserId, now)
        response <- okJson(BlogPostResponseDto.fromView(post))
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" / postIdValue / "unlike" =>
      for
        currentUserId <- requireCurrentUserId(request)
        likeRequest <- request.as[BlogLikeRequestDto]
        post <- blogApplicationService.unlikePost(BlogId(postIdValue), currentUserId)
        response <- okJson(BlogPostResponseDto.fromView(post))
      yield response
  }

  private def parseBlogScope(value: String): BlogPostScope =
    value match
      case "mine" => BlogPostScope.Mine
      case _      => BlogPostScope.Latest

  private def parseBlogImages(images: List[ContentImageResponseDto]): List[BlogImageRef] =
    images.zipWithIndex.map { case (image, index) =>
      BlogImageRef(
        imageId = BlogImageId(image.imageId),
        publicUrl = image.publicUrl,
        originalFileName = image.originalFileName,
        sortOrder = index,
        createdAt = Instant.parse(image.createdAt)
      )
    }
