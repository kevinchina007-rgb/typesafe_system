// ReferenceDataSeederAirlines handles airline manager reference data.
package com.typesafe.travel.persistence

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.shared.kernel.EmailAddress
import doobie.*
import doobie.implicits.*

import java.time.Instant

object ReferenceDataSeederAirlines:
  def seedAirlineManagers(transactor: Transactor[IO]): IO[Unit] =
    airlineManagerSeeds.traverse_ { seed =>
      seedAirlineManagerRecord(transactor, seed)
    }
  val airlineManagerDemoPassword = "Airline2026!"
  val airlineManagerDemoCreatedAt = Instant.parse("2026-05-18T00:00:00Z")
  val airlineManagerSeeds = List(
    AirlineManagerSeed("airline-nailong", "NL", "奶龙航空", "manager-airline-nailong", "ops@nailong.example", "奶龙航空运营", "/images/airlines/NL.svg"),
    AirlineManagerSeed("airline-laoda", "LD", "科比航空", "manager-airline-laoda", "ops@laoda.example", "科比航空运营", "/images/airlines/LD.svg"),
    AirlineManagerSeed("airline-mihoyo", "MH", "万户航空", "manager-airline-mihoyo", "ops@mihoyo.example", "万户航空运营", "/images/airlines/MH.svg"),
    AirlineManagerSeed("airline-tafei", "TF", "双子塔航空", "manager-airline-tafei", "ops@tafei.example", "双子塔航空运营", "/images/airlines/TF.svg"),
    AirlineManagerSeed("airline-genshin", "YS", "雪豹航空", "manager-airline-genshin", "ops@genshin.example", "雪豹航空运营", "/images/airlines/YS.svg"),
    AirlineManagerSeed("airline-wxd", "WX", "星际穿越航空", "manager-airline-wxd", "ops@wxd.example", "星际穿越航空运营", "/images/airlines/WX.svg"),
    AirlineManagerSeed("airline-zhenxun", "ZX", "祖国人航空", "manager-airline-zhenxun", "ops@zhenxun.example", "祖国人航空运营", "/images/airlines/ZX.svg"),
    AirlineManagerSeed("airline-jntm", "JN", "SpaceX航空", "manager-airline-jntm", "ops@jntm.example", "SpaceX航空运营", "/images/airlines/JN.svg"),
    AirlineManagerSeed("airline-pangmao", "PM", "无人驾驶航空", "manager-airline-pangmao", "ops@pangmao.example", "无人驾驶航空运营", "/images/airlines/PM.svg"),
    AirlineManagerSeed("airline-niuma", "NM", "卡皮巴拉航空", "manager-airline-niuma", "ops@niuma.example", "卡皮巴拉航空运营", "/images/airlines/NM.svg")
  )
  final case class AirlineManagerSeed(
      airlineId: String,
      airlineCode: String,
      airlineName: String,
      managerId: String,
      managerEmail: String,
      displayName: String,
      logoAssetPath: String
  )

  final case class RoomTypeTemplate(
      suffix: String,
      roomTypeName: String,
      capacity: Int,
      bedType: String,
      baseAvailableRooms: Int,
      basePriceOffset: BigDecimal,
      imageUrl: String
  )

  def seedAirlineManagerRecord(transactor: Transactor[IO], seed: AirlineManagerSeed): IO[Unit] =
    for
      passwordHash <- hashPasswordForLoginEmail(airlineManagerDemoPassword, EmailAddress.unsafe(seed.managerEmail))
      statements = List(
        insertOrUpdateAirline(seed.airlineId, seed.airlineName, seed.airlineCode, seed.logoAssetPath, airlineManagerDemoCreatedAt),
        insertOrUpdateAirlineManager(seed.managerId, seed.airlineId, seed.managerEmail, seed.displayName, "Active", airlineManagerDemoCreatedAt),
        insertOrUpdateManagerCredential("Airline", seed.managerId, seed.managerEmail, passwordHash, airlineManagerDemoCreatedAt)
      )
      _ <- statements.sequence.transact(transactor).void
    yield ()

  def insertOrUpdateAirline(
      airlineId: String,
      name: String,
      code: String,
      logoAssetPath: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtValue = createdAt.toString
    sql"""
      insert into airlines (airline_id, name, code, status, created_at, logo_asset_path)
      values ($airlineId, $name, $code, ${"Active"}, cast($createdAtValue as timestamptz), $logoAssetPath)
      on conflict (airline_id) do update set
        name = excluded.name,
        code = excluded.code,
        status = excluded.status,
        logo_asset_path = excluded.logo_asset_path
    """.update.run

  def insertOrUpdateAirlineManager(
      managerId: String,
      airlineId: String,
      email: String,
      displayName: String,
      status: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtValue = createdAt.toString
    sql"""
      insert into airline_managers (manager_id, airline_id, email, display_name, status, created_at)
      values ($managerId, $airlineId, $email, $displayName, $status, cast($createdAtValue as timestamptz))
      on conflict (manager_id) do update set
        airline_id = excluded.airline_id,
        email = excluded.email,
        display_name = excluded.display_name,
        status = excluded.status,
        created_at = excluded.created_at
    """.update.run

  def insertOrUpdateManagerCredential(
      managerType: String,
      managerId: String,
      email: String,
      passwordHash: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtValue = createdAt.toString
    sql"""
      insert into manager_credentials (
        credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at, password_updated_at
      ) values (
        ${s"credential-${managerType.toLowerCase}-$managerId"},
        $managerType,
        $managerId,
        $email,
        $passwordHash,
        ${"Active"},
        cast($createdAtValue as timestamptz),
        cast($createdAtValue as timestamptz),
        cast($createdAtValue as timestamptz)
      )
      on conflict (manager_type, manager_id) do update set
        login_email = excluded.login_email,
        password_hash = excluded.password_hash,
        status = excluded.status,
        updated_at = excluded.updated_at,
        password_updated_at = excluded.password_updated_at
    """.update.run
