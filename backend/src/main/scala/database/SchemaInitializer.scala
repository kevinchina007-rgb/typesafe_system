// SchemaInitializer 负责初始化数据库结构。

package com.typesafe.travel.persistence

import cats.effect.IO

import cats.effect.kernel.Async
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

object SchemaInitializer:
  def initialize(transactor: Transactor[IO]): IO[Unit] =
    initializeWithMigrationSteps(
      transactor = transactor,
      migrationSteps = MigrationPlan.defaultSteps,
      runReferenceSeedData = false
    )

  private[persistence] def initializeWithMigrationSteps(
      transactor: Transactor[IO],
      migrationSteps: List[MigrationStep],
      runReferenceSeedData: Boolean
  ): IO[Unit] =
    for
      _ <- createSchemaMigrationsTable.transact(transactor)
      appliedVersions <- loadAppliedMigrationVersions(transactor)
      _ <- migrationSteps
        .filterNot(migrationStep => appliedVersions.contains(migrationStep.version))
        .sortBy(_.version)
        .traverse_(migrationStep => applyMigrationStep(transactor, migrationStep))
      _ <- if runReferenceSeedData then ReferenceDataSeeder.seedIfNeeded(transactor) else IO.unit
    yield ()

  private def applyMigrationStep(transactor: Transactor[IO], migrationStep: MigrationStep): IO[Unit] =
    val migrationConnection =
      migrationStep.statements.traverse_(migrationStatement => Update0(migrationStatement, None).run.void) *>
        sql"""
          insert into schema_migrations (version, description, applied_at)
          values (${migrationStep.version}, ${migrationStep.description}, current_timestamp)
        """.update.run.void

    migrationConnection.transact(transactor)

  private def loadAppliedMigrationVersions(transactor: Transactor[IO]): IO[Set[Int]] =
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
