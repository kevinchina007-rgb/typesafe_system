package com.typesafe.travel.api

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.syntax.all.*
import com.typesafe.travel.api.routes.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.persistence.order.TrainOrderExpirySweeper

import ApplicationWiringPaths.resolveConfiguredPath

object PersistenceApplicationWiring:
  def resource: Resource[IO, ApplicationWiring] =
    val databaseConfig = DatabaseConfig.loadFromEnvironment
    val avatarUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_AVATAR_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "avatars")
    val contentUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_CONTENT_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "content")
    val frontendDistRootDirectoryPath = resolveConfiguredPath("TRAVEL_FRONTEND_DIST_ROOT", "TRAVEL_STATIC_ROOT", "..", "frontend", "dist")

    DatabaseTransactor.resource(databaseConfig).flatMap { databaseTransactor =>
      val setup =
        Resource.eval(SchemaInitializer.initialize(databaseTransactor)) *>
          Resource.eval(ReferenceDataSeeder.seedIfNeeded(databaseTransactor))
      val wiring =
        ApplicationWiring(
          httpApp =
            (
              PlannerRouter(PlannerDefinitions.orderPlanners).routes <+>
                ApiRouter(
                  uploadedBinaryAssetReader = Some(UploadedBinaryAssetPlainSql(databaseConfig)),
                  avatarUploadRootDirectoryPath = avatarUploadRootDirectoryPath,
                  contentUploadRootDirectoryPath = contentUploadRootDirectoryPath,
                  frontendDistRootDirectoryPath = frontendDistRootDirectoryPath
                ).routes
            ).orNotFound
        )

      setup *>
        Resource.make(TrainOrderExpirySweeper.start(databaseConfig))(_.cancel).void *>
        Resource.pure(wiring)
    }
