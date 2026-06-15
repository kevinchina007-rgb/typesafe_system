// PersistenceApplicationWiring 负责组装 persistence 层相关依赖。

package com.typesafe.travel.api

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.syntax.all.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.persistence.order.TrainOrderExpirySweeper
import com.typesafe.travel.static.StaticAssetRouter
import com.typesafe.travel.api.routes.PlannerDefinitions
import com.typesafe.travel.api.routes.PlannerRouter
import com.typesafe.travel.api.routes.HealthRouter

import ApplicationWiringPaths.resolveConfiguredPath

object PersistenceApplicationWiring:
  def resource: Resource[IO, ApplicationWiring] =
    val databaseConfig = DatabaseConfig.loadFromEnvironment
    val avatarUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_AVATAR_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "avatars")
    val contentUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_CONTENT_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "content")
    val frontendDistRootDirectoryPath = resolveConfiguredPath("TRAVEL_FRONTEND_DIST_ROOT", "TRAVEL_STATIC_ROOT", "..", "frontend", "dist")

    DatabaseTransactor.resource(databaseConfig).flatMap { databaseTransactor =>
      val setup =
        Resource.eval(SchemaInitializer.initialize(databaseTransactor))
      val wiring =
        ApplicationWiring(
          httpApp =
            (
              HealthRouter.routes <+>
                PlannerRouter(PlannerDefinitions.allPlanners).routes <+>
                StaticAssetRouter(
                  uploadedBinaryAssetReader = Some(UploadedBinaryAssetPlainSql(databaseConfig)),
                  avatarUploadRootDirectoryPath = avatarUploadRootDirectoryPath,
                  contentUploadRootDirectoryPath = contentUploadRootDirectoryPath,
                  frontendDistRootDirectoryPath = frontendDistRootDirectoryPath
                ).routes
            ).orNotFound
        )

      setup *>
        Resource.eval(ReferenceDataSeeder.seedIfNeeded(databaseTransactor)) *>
        Resource.make(TrainOrderExpirySweeper.start(databaseConfig))(_.cancel).void *>
        Resource.pure(wiring)
    }
