package com.typesafe.travel.persistence

import cats.effect.kernel.Async
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

object SchemaInitializer:
  def initialize[F[_]: Async](transactor: Transactor[F]): F[Unit] =
    initializeWithMigrationSteps(
      transactor = transactor,
      migrationSteps = MigrationPlan.defaultSteps,
      runReferenceSeedData = true
    )

  private[persistence] def initializeWithMigrationSteps[F[_]: Async](
      transactor: Transactor[F],
      migrationSteps: List[MigrationStep],
      runReferenceSeedData: Boolean
  ): F[Unit] =
    for
      _ <- createSchemaMigrationsTable.transact(transactor)
      appliedVersions <- loadAppliedMigrationVersions(transactor)
      _ <- migrationSteps
        .filterNot(migrationStep => appliedVersions.contains(migrationStep.version))
        .sortBy(_.version)
        .traverse_(migrationStep => applyMigrationStep(transactor, migrationStep))
      _ <- if runReferenceSeedData then ReferenceDataSeeder.seedIfNeeded(transactor) else Async[F].unit
    yield ()

  private def applyMigrationStep[F[_]: Async](transactor: Transactor[F], migrationStep: MigrationStep): F[Unit] =
    val migrationConnection =
      migrationStep.statements.traverse_(migrationStatement => Update0(migrationStatement, None).run.void) *>
        sql"""
          insert into schema_migrations (version, description, applied_at)
          values (${migrationStep.version}, ${migrationStep.description}, current_timestamp)
        """.update.run.void

    migrationConnection.transact(transactor)

  private def loadAppliedMigrationVersions[F[_]: Async](transactor: Transactor[F]): F[Set[Int]] =
    sql"select version from schema_migrations order by version"
      .query[Int]
      .to[List]
      .transact(transactor)
      .map(_.toSet)

  private val createSchemaMigrationsTable: ConnectionIO[Unit] =
    sql"""
      create table if not exists schema_migrations (
        version integer primary key,
        description varchar(160) not null,
        applied_at timestamp with time zone not null
      )
    """.update.run.void
