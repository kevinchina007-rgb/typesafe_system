package com.typesafe.travel.persistence

import cats.effect.unsafe.implicits.global
import com.typesafe.travel.identity.domain.User
import com.typesafe.travel.persistence.identity.DoobieUserRepository
import com.typesafe.travel.shared.kernel.*
import doobie.implicits.*
import munit.FunSuite

import java.nio.file.Files
import java.time.Instant

final class SchemaInitializerSpec extends FunSuite:
  test("schema initializer is idempotent and file-backed data remains readable after reinitialize") {
    val databaseDirectoryPath = Files.createTempDirectory("travel-persistence-phase2")
    val databasePath = databaseDirectoryPath.resolve("travel-db")

    val firstTransactor = PersistenceTestSupport.createFileTransactor(databasePath)
    SchemaInitializer.initialize(firstTransactor).unsafeRunSync()

    val userRepository = DoobieUserRepository[cats.effect.IO](firstTransactor)
    val savedUser = User.restorePersistedUser(
      userId = UserId("user-file-roundtrip"),
      primaryEmailAddress = EmailAddress.unsafe("file-roundtrip@example.com"),
      userDisplayName = PersonName.unsafe("File Roundtrip"),
      userPhoneNumber = ContactNumber.unsafe("+8613800001001"),
      avatarUrl = None,
      userAccountStatus = com.typesafe.travel.identity.domain.UserAccountStatus.Active,
      membershipLevel = com.typesafe.travel.identity.domain.UserMembershipLevel.Silver,
      loyaltyPoints = Points.unsafe(5200),
      defaultTravelerProfileId = None,
      registeredAt = Instant.parse("2026-03-26T06:00:00Z")
    )
    userRepository.saveUser(savedUser).unsafeRunSync()

    val secondTransactor = PersistenceTestSupport.createFileTransactor(databasePath)
    SchemaInitializer.initialize(secondTransactor).unsafeRunSync()

    val reloadedUser = DoobieUserRepository[cats.effect.IO](secondTransactor).findByUserId(savedUser.userId).unsafeRunSync()
    val appliedMigrationVersions =
      sql"select version from schema_migrations order by version".query[Int].to[List].transact(secondTransactor).unsafeRunSync()

    assertEquals(reloadedUser, Some(savedUser))
    assertEquals(appliedMigrationVersions, List(1, 2, 3, 5, 6))
  }

  test("failed migration is not recorded in schema_migrations") {
    val transactor = PersistenceTestSupport.createTestTransactor()

    val failedResult =
      SchemaInitializer
        .initializeWithMigrationSteps(
          transactor = transactor,
          migrationSteps = List(
            MigrationStep(
              version = 99,
              description = "broken_migration",
              statements = List(
                "create table broken_demo (broken_id varchar(20) primary key)",
                "this is not valid sql"
              )
            )
          ),
          runReferenceSeedData = false
        )
        .attempt
        .unsafeRunSync()

    val appliedMigrationVersions =
      sql"select version from schema_migrations order by version".query[Int].to[List].transact(transactor).unsafeRunSync()

    assert(failedResult.isLeft)
    assertEquals(appliedMigrationVersions, Nil)
  }
