package com.typesafe.travel.api.routes

import cats.MonadThrow
import cats.effect.kernel.Async
import cats.syntax.all.*
import io.circe.Encoder
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers
import org.http4s.multipart.{Multipart, Part}

final case class UploadedBinaryFile(
    originalFileName: String,
    contentTypeValue: String,
    fileBytes: Array[Byte]
)

trait RouteTransportSupport[F[_]: Async] extends Http4sDsl[F]:

  protected def okJson[A: Encoder](body: A): F[Response[F]] =
    Ok(body.asJson)

  protected def createdJson[A: Encoder](body: A): F[Response[F]] =
    Created(body.asJson)

  protected def requireQueryParam(request: Request[F], fieldName: String): F[String] =
    MonadThrow[F].fromEither(
      request.params.get(fieldName).map(_.trim).filter(_.nonEmpty).toRight(com.typesafe.travel.shared.kernel.SharedValidationError.RequiredFieldWasEmpty(fieldName))
    )

  protected def requireMultipartPart(multipartPayload: Multipart[F], fieldName: String, missingError: Throwable): F[Part[F]] =
    multipartPayload.parts.find(_.name.contains(fieldName)).liftTo[F](missingError)

  protected def readUploadedBinary(part: Part[F], missingError: Throwable): F[UploadedBinaryFile] =
    for
      fileName <- part.filename.liftTo[F](missingError)
      contentTypeValue =
        part.headers
          .get[headers.`Content-Type`]
          .map(contentTypeHeader => s"${contentTypeHeader.mediaType.mainType}/${contentTypeHeader.mediaType.subType}")
          .getOrElse("application/octet-stream")
      fileBytes <- part.body.compile.to(Array)
    yield UploadedBinaryFile(
      originalFileName = fileName,
      contentTypeValue = contentTypeValue,
      fileBytes = fileBytes
    )
