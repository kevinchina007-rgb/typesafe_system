package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.application.{BlogImageUploadError, BlogPostScope}
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.content.domain.BlogImageRef
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers
import org.http4s.multipart.Multipart

import java.time.Instant

trait BlogApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def blogRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root / "api" / "blog" / "posts" =>
      val scope = request.params.get("scope").map(_.trim.toLowerCase).getOrElse("latest")
      val currentUserId = request.params.get("userId").map(UserId.apply)
      val query = request.params.get("q").map(_.trim).filter(_.nonEmpty)
      for
        posts <- blogApplicationService.listPosts(currentUserId, parseBlogScope(scope), query)
        response <- Ok(BlogPostListResponseDto(posts.map(BlogPostSummaryResponseDto.fromView)).asJson)
      yield response

    case request @ GET -> Root / "api" / "blog" / "posts" / postIdValue =>
      for
        post <- blogApplicationService.getPost(BlogId(postIdValue), request.params.get("userId").map(UserId.apply))
        response <- Ok(BlogPostResponseDto.fromView(post).asJson)
      yield response

    case request @ POST -> Root / "api" / "blog" / "images" =>
      for
        multipartPayload <- request.as[Multipart[F]]
        userIdValue <- fromEither(request.params.get("userId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("userId")))
        imagePart <- multipartPayload.parts.find(_.name.contains("image")).liftTo[F](BlogImageUploadError.ImageWasMissing)
        imageFileName <- imagePart.filename.liftTo[F](BlogImageUploadError.ImageWasMissing)
        imageContentType =
          imagePart.headers
            .get[headers.`Content-Type`]
            .map(contentTypeHeader => s"${contentTypeHeader.mediaType.mainType}/${contentTypeHeader.mediaType.subType}")
            .getOrElse("")
        imageBytes <- imagePart.body.compile.to(Array)
        now <- currentInstantF
        imageRef <- blogApplicationService.uploadImage(UserId(userIdValue), imageFileName, imageContentType, imageBytes, now)
        response <- Created(ContentImageResponseDto.fromBlogImageRef(imageRef).asJson)
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" =>
      for
        createRequest <- request.as[CreateBlogPostRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.createPublishedPost(
          UserId(createRequest.userId),
          createRequest.title,
          createRequest.summary,
          createRequest.content,
          parseBlogImages(createRequest.images),
          now
        )
        response <- Created(BlogPostResponseDto.fromView(post).asJson)
      yield response

    case request @ PATCH -> Root / "api" / "blog" / "posts" / postIdValue =>
      for
        updateRequest <- request.as[UpdateBlogPostRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.updatePost(
          BlogId(postIdValue),
          UserId(updateRequest.userId),
          updateRequest.title,
          updateRequest.summary,
          updateRequest.content,
          parseBlogImages(updateRequest.images),
          now
        )
        response <- Ok(BlogPostResponseDto.fromView(post).asJson)
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" / postIdValue / "archive" =>
      for
        likeRequest <- request.as[BlogLikeRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.archivePost(BlogId(postIdValue), UserId(likeRequest.userId), now)
        response <- Ok(BlogPostResponseDto.fromView(post).asJson)
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" / postIdValue / "comments" =>
      for
        createRequest <- request.as[CreateBlogCommentRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.addComment(BlogId(postIdValue), UserId(createRequest.userId), createRequest.content, now)
        response <- Created(BlogPostResponseDto.fromView(post).asJson)
      yield response

    case request @ DELETE -> Root / "api" / "blog" / "comments" / commentIdValue =>
      for
        deleteRequest <- request.as[DeleteBlogCommentRequestDto]
        post <- blogApplicationService.deleteComment(BlogCommentId(commentIdValue), UserId(deleteRequest.userId))
        response <- Ok(BlogPostResponseDto.fromView(post).asJson)
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" / postIdValue / "likes" =>
      for
        likeRequest <- request.as[BlogLikeRequestDto]
        now <- currentInstantF
        post <- blogApplicationService.likePost(BlogId(postIdValue), UserId(likeRequest.userId), now)
        response <- Ok(BlogPostResponseDto.fromView(post).asJson)
      yield response

    case request @ POST -> Root / "api" / "blog" / "posts" / postIdValue / "unlike" =>
      for
        likeRequest <- request.as[BlogLikeRequestDto]
        post <- blogApplicationService.unlikePost(BlogId(postIdValue), UserId(likeRequest.userId))
        response <- Ok(BlogPostResponseDto.fromView(post).asJson)
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
