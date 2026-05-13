package com.typesafe.travel.api

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.syntax.all.*
import com.typesafe.travel.api.routes.*
import com.typesafe.travel.persistence.*

import ApplicationWiringPaths.resolveConfiguredPath

object PersistenceApplicationWiring:
  def resource: Resource[IO, ApplicationWiring] =
    val databaseConfig = DatabaseConfig.loadFromEnvironment
    val avatarUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_AVATAR_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "avatars")
    val contentUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_CONTENT_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "content")
    val frontendDistRootDirectoryPath = resolveConfiguredPath("TRAVEL_FRONTEND_DIST_ROOT", "TRAVEL_STATIC_ROOT", "..", "frontend", "dist")

    DatabaseTransactor.resource(databaseConfig).evalMap { databaseTransactor =>
      for
        _ <- SchemaInitializer.initialize(databaseTransactor)
        uploadedBinaryAssetReader = UploadedBinaryAssetPlainSql(databaseConfig)
        apiRouter =
          ApiRouter(
            uploadedBinaryAssetReader = Some(uploadedBinaryAssetReader),
            avatarUploadRootDirectoryPath = avatarUploadRootDirectoryPath,
            contentUploadRootDirectoryPath = contentUploadRootDirectoryPath,
            frontendDistRootDirectoryPath = frontendDistRootDirectoryPath
          )
        plannerRouter =
          PlannerRouter(PlannerDefinitions.orderPlanners)
      yield ApplicationWiring(
        httpApp = (plannerRouter.routes <+> apiRouter.routes).orNotFound
      )
    }
