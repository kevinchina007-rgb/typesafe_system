package com.typesafe.travel.api

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.api.routes.*
import com.typesafe.travel.persistence.UploadedBinaryAssetReader
import com.typesafe.travel.static.StaticAssetRouter
import org.http4s.*
import org.http4s.dsl.Http4sDsl

import java.nio.file.Path

final class ApiRouter(
    protected val uploadedBinaryAssetReader: Option[UploadedBinaryAssetReader],
    protected val avatarUploadRootDirectoryPath: Path,
    protected val contentUploadRootDirectoryPath: Path,
    protected val frontendDistRootDirectoryPath: Path
) extends Http4sDsl[IO]:

  private val staticAssetRoutes: HttpRoutes[IO] =
    StaticAssetRouter(
      uploadedBinaryAssetReader = uploadedBinaryAssetReader,
      avatarUploadRootDirectoryPath = avatarUploadRootDirectoryPath,
      contentUploadRootDirectoryPath = contentUploadRootDirectoryPath,
      frontendDistRootDirectoryPath = frontendDistRootDirectoryPath
    ).routes

  val routes: HttpRoutes[IO] =
    HealthRouter.routes <+> TourGroupChatRouter().routes <+> staticAssetRoutes

object ApiRouter:
  def apply(
      uploadedBinaryAssetReader: Option[UploadedBinaryAssetReader],
      avatarUploadRootDirectoryPath: Path,
      contentUploadRootDirectoryPath: Path,
      frontendDistRootDirectoryPath: Path
  ): ApiRouter =
    new ApiRouter(
      uploadedBinaryAssetReader,
      avatarUploadRootDirectoryPath,
      contentUploadRootDirectoryPath,
      frontendDistRootDirectoryPath
    )
