package com.typesafe.travel.persistence

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.attraction.domain.TicketEligibilityRuleType
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.shared.kernel.EmailAddress
import doobie.*
import doobie.implicits.*

import java.time.{DayOfWeek, Instant, LocalDate, LocalTime, ZoneOffset}

object AttractionReferenceDataSeeder:
  private val DemoPassword = "Attraction2026!"
  private val SeededAt = Instant.parse("2026-05-18T00:00:00Z")
  private val DemoAttractionPrefix = "demo-attraction"
  private val AvailabilityStartDate = LocalDate.parse("2026-06-01")
  private val AvailabilityEndDate = LocalDate.parse("2026-07-31")
  private val AllWeekdaysCsv = DayOfWeek.values().toList.map(_.toString).mkString(",")

  private final case class CitySeed(cityName: String, citySlug: String)
  private final case class AttractionTemplate(
      suffix: String,
      attractionNameSuffix: String,
      locationSuffix: String,
      descriptionSuffix: String
  )
  private final case class TicketRuleSeed(ruleSuffix: String, ruleType: TicketEligibilityRuleType, ruleConfigJson: String)
  private final case class TicketTemplate(
      suffix: String,
      ticketTypeName: String,
      descriptionSuffix: String,
      sessionLabel: String,
      startHour: Int,
      startMinute: Int,
      endHour: Int,
      endMinute: Int,
      priceBase: BigDecimal,
      priceCityStep: BigDecimal,
      priceAttractionStep: BigDecimal,
      capacityBase: Int,
      capacityCityStep: Int,
      capacityAttractionStep: Int,
      rules: List[TicketRuleSeed]
  )

  private val citySeeds = List(
    CitySeed("北京", "beijing"),
    CitySeed("上海", "shanghai"),
    CitySeed("广州", "guangzhou"),
    CitySeed("深圳", "shenzhen"),
    CitySeed("成都", "chengdu"),
    CitySeed("重庆", "chongqing"),
    CitySeed("杭州", "hangzhou"),
    CitySeed("南京", "nanjing"),
    CitySeed("武汉", "wuhan"),
    CitySeed("西安", "xian"),
    CitySeed("天津", "tianjin"),
    CitySeed("郑州", "zhengzhou"),
    CitySeed("长沙", "changsha"),
    CitySeed("青岛", "qingdao"),
    CitySeed("厦门", "xiamen"),
    CitySeed("济南", "jinan"),
    CitySeed("合肥", "hefei"),
    CitySeed("南昌", "nanchang")
  )

  private val attractionTemplates = List(
    AttractionTemplate("ocean-park", "海洋乐园", "滨海度假区", "主打亲子海洋互动体验，适合暑期与周末游玩。"),
    AttractionTemplate("museum", "城市博物馆", "文化中心", "集合城市历史与当代展陈，适合深度打卡。"),
    AttractionTemplate("garden", "城市花园", "中央公园", "可步行游览的休闲景点，适合轻松半日游。"),
    AttractionTemplate("theme-park", "主题乐园", "欢乐大道", "大型游乐设施与演艺项目并重。"),
    AttractionTemplate("night-tour", "夜游灯会", "夜景街区", "夜间开园，灯光秀与夜景打卡优先。"),
    AttractionTemplate("science", "科技探索馆", "科技新城", "面向亲子与年轻游客的互动体验场馆。"),
    AttractionTemplate("art", "艺术馆", "艺术街区", "展览与公共艺术结合的城市景点。"),
    AttractionTemplate("ancient", "古城景区", "历史街区", "适合文化游与拍照打卡。")
  )

  private val ticketTemplates = List(
    TicketTemplate(
      suffix = "adult",
      ticketTypeName = "标准成人票",
      descriptionSuffix = "适合大多数游客的基础票型。",
      sessionLabel = "常规场",
      startHour = 9,
      startMinute = 0,
      endHour = 21,
      endMinute = 0,
      priceBase = BigDecimal(88),
      priceCityStep = BigDecimal(4),
      priceAttractionStep = BigDecimal(3),
      capacityBase = 260,
      capacityCityStep = 10,
      capacityAttractionStep = 8,
      rules = Nil
    ),
    TicketTemplate(
      suffix = "child",
      ticketTypeName = "儿童优惠票",
      descriptionSuffix = "仅限 18 岁以下游客。",
      sessionLabel = "亲子场",
      startHour = 9,
      startMinute = 30,
      endHour = 18,
      endMinute = 0,
      priceBase = BigDecimal(58),
      priceCityStep = BigDecimal(3),
      priceAttractionStep = BigDecimal(2),
      capacityBase = 180,
      capacityCityStep = 8,
      capacityAttractionStep = 6,
      rules = List(TicketRuleSeed("child", TicketEligibilityRuleType.AgeLessThan, """{"ruleConfigType":"AgeLessThan","maxExclusive":18}"""))
    ),
    TicketTemplate(
      suffix = "senior",
      ticketTypeName = "长者优惠票",
      descriptionSuffix = "仅限 70 岁及以上游客。",
      sessionLabel = "长者场",
      startHour = 10,
      startMinute = 0,
      endHour = 16,
      endMinute = 30,
      priceBase = BigDecimal(52),
      priceCityStep = BigDecimal(3),
      priceAttractionStep = BigDecimal(2),
      capacityBase = 140,
      capacityCityStep = 6,
      capacityAttractionStep = 5,
      rules = List(TicketRuleSeed("senior", TicketEligibilityRuleType.AgeAtLeast, """{"ruleConfigType":"AgeAtLeast","minInclusive":70}"""))
    ),
    TicketTemplate(
      suffix = "night",
      ticketTypeName = "夜游票",
      descriptionSuffix = "适合傍晚和夜间入园游玩。",
      sessionLabel = "夜游场",
      startHour = 18,
      startMinute = 30,
      endHour = 22,
      endMinute = 0,
      priceBase = BigDecimal(76),
      priceCityStep = BigDecimal(4),
      priceAttractionStep = BigDecimal(3),
      capacityBase = 160,
      capacityCityStep = 7,
      capacityAttractionStep = 5,
      rules = Nil
    )
  )

  private val expectedAttractionCount = citySeeds.size * attractionTemplates.size

  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    for
      demoCount <- sql"select count(*) from attractions where attraction_id like 'demo-attraction-%'".query[Long].unique.transact(transactor)
      _ <- if demoCount >= expectedAttractionCount.toLong then IO.unit else seedDemoData(transactor)
    yield ()

  private def seedDemoData(transactor: Transactor[IO]): IO[Unit] =
    citySeeds.zipWithIndex.traverse_ { case (citySeed, cityIndex) =>
      attractionTemplates.zipWithIndex.traverse_ { case (template, attractionIndex) =>
        seedAttraction(transactor, citySeed, cityIndex, template, attractionIndex)
      }
    }

  private def seedAttraction(
      transactor: Transactor[IO],
      citySeed: CitySeed,
      cityIndex: Int,
      attractionTemplate: AttractionTemplate,
      attractionIndex: Int
  ): IO[Unit] =
    val attractionId = s"$DemoAttractionPrefix-${citySeed.citySlug}-${attractionTemplate.suffix}"
    val managerId = s"manager-$attractionId"
    val managerEmail = s"ops-${citySeed.citySlug}-${attractionTemplate.suffix}@attraction.example"
    val managerDisplayName = s"${citySeed.cityName}${attractionTemplate.attractionNameSuffix}管理者"
    val attractionName = s"${citySeed.cityName}${attractionTemplate.attractionNameSuffix}"
    val location = s"${citySeed.cityName}${attractionTemplate.locationSuffix}"
    val description = s"${citySeed.cityName}${attractionTemplate.attractionNameSuffix}${attractionTemplate.descriptionSuffix}6-7 月每日均有充足票量。"

    for
      passwordHash <- hashPasswordForLoginEmail(DemoPassword, EmailAddress.unsafe(managerEmail))
      statements = attractionSeedStatements(
        citySeed = citySeed,
        cityIndex = cityIndex,
        attractionIndex = attractionIndex,
        attractionId = attractionId,
        managerId = managerId,
        managerEmail = managerEmail,
        managerDisplayName = managerDisplayName,
        attractionName = attractionName,
        location = location,
        description = description,
        passwordHash = passwordHash
      )
      _ <- statements.sequence.transact(transactor).void
    yield ()

  private def attractionSeedStatements(
      citySeed: CitySeed,
      cityIndex: Int,
      attractionIndex: Int,
      attractionId: String,
      managerId: String,
      managerEmail: String,
      managerDisplayName: String,
      attractionName: String,
      location: String,
      description: String,
      passwordHash: String
  ): List[ConnectionIO[Int]] =
    val attractionStatements =
      List(
        insertOrUpsertAttraction(attractionId, managerId, attractionName, citySeed.cityName, location, description, SeededAt),
        insertOrUpsertAttractionManager(managerId, managerEmail, managerDisplayName, SeededAt),
        insertOrUpsertManagerCredential("Attraction", managerId, managerEmail, passwordHash, SeededAt)
      )

    val ticketStatements =
      ticketTemplates.zipWithIndex.flatMap { case (ticketTemplate, _) =>
        val ticketTypeId = s"ticket-type-$attractionId-${ticketTemplate.suffix}"
        val ticketTypePrice = computePrice(ticketTemplate, cityIndex, attractionIndex)
        val ticketTypeCapacity = computeCapacity(ticketTemplate, cityIndex, attractionIndex)

        val ticketTypeStatements =
          List(
            insertOrUpsertTicketType(
              ticketTypeId = ticketTypeId,
              attractionId = attractionId,
              name = ticketTemplate.ticketTypeName,
              description = s"${citySeed.cityName}${ticketTemplate.ticketTypeName}${ticketTemplate.descriptionSuffix}",
              priceAmount = ticketTypePrice,
              availableFromDate = AvailabilityStartDate,
              availableToDate = AvailabilityEndDate,
              totalQuantity = ticketTypeCapacity,
              validWeekdaysCsv = AllWeekdaysCsv,
              createdAt = SeededAt
            )
          )

        val ruleStatements =
          ticketTemplate.rules.map { ruleSeed =>
            insertOrUpsertTicketRule(
              ruleId = s"rule-$attractionId-${ticketTemplate.suffix}-${ruleSeed.ruleSuffix}",
              ticketTypeId = ticketTypeId,
              ruleType = ruleSeed.ruleType.toString,
              ruleConfigJson = ruleSeed.ruleConfigJson,
              createdAt = SeededAt
            )
          }

        val sessionStatements =
          dateRange().map { useDate =>
            val sessionId = s"ticket-session-$ticketTypeId-$useDate"
            val startsAt = localInstant(useDate, ticketTemplate.startHour, ticketTemplate.startMinute)
            val endsAt = localInstant(useDate, ticketTemplate.endHour, ticketTemplate.endMinute)
            insertOrUpsertTicketSession(
              sessionId = sessionId,
              ticketTypeId = ticketTypeId,
              sessionName = s"$useDate ${ticketTemplate.sessionLabel}",
              useDate = useDate,
              startsAt = startsAt,
              endsAt = endsAt,
              capacity = ticketTypeCapacity,
              createdAt = SeededAt
            )
          }

        ticketTypeStatements ++ ruleStatements ++ sessionStatements
      }

    attractionStatements ++ ticketStatements

  private def computePrice(ticketTemplate: TicketTemplate, cityIndex: Int, attractionIndex: Int): BigDecimal =
    (ticketTemplate.priceBase + ticketTemplate.priceCityStep * BigDecimal(cityIndex) + ticketTemplate.priceAttractionStep * BigDecimal(attractionIndex))
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)

  private def computeCapacity(ticketTemplate: TicketTemplate, cityIndex: Int, attractionIndex: Int): Int =
    ticketTemplate.capacityBase + cityIndex * ticketTemplate.capacityCityStep + attractionIndex * ticketTemplate.capacityAttractionStep

  private def dateRange(): Vector[LocalDate] =
    Iterator.iterate(AvailabilityStartDate)(_.plusDays(1)).takeWhile(date => !date.isAfter(AvailabilityEndDate)).toVector

  private def localInstant(date: LocalDate, hour: Int, minute: Int): Instant =
    date.atTime(LocalTime.of(hour, minute)).atOffset(ZoneOffset.ofHours(8)).toInstant

  private def insertOrUpsertAttraction(
      attractionId: String,
      managerId: String,
      name: String,
      city: String,
      location: String,
      description: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtValue = createdAt.toString
    sql"""
      insert into attractions (attraction_id, manager_id, name, city, location, description, status, created_at)
      values ($attractionId, $managerId, $name, $city, $location, $description, ${"Published"}, cast($createdAtValue as timestamptz))
      on conflict (attraction_id) do update set
        manager_id = excluded.manager_id,
        name = excluded.name,
        city = excluded.city,
        location = excluded.location,
        description = excluded.description,
        status = excluded.status,
        created_at = excluded.created_at
    """.update.run

  private def insertOrUpsertAttractionManager(
      managerId: String,
      email: String,
      displayName: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtValue = createdAt.toString
    sql"""
      insert into attraction_managers (manager_id, email, display_name, status, created_at)
      values ($managerId, $email, $displayName, ${"Active"}, cast($createdAtValue as timestamptz))
      on conflict (manager_id) do update set
        email = excluded.email,
        display_name = excluded.display_name,
        status = excluded.status,
        created_at = excluded.created_at
    """.update.run

  private def insertOrUpsertManagerCredential(
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
        ${s"credential-$managerId"},
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

  private def insertOrUpsertTicketType(
      ticketTypeId: String,
      attractionId: String,
      name: String,
      description: String,
      priceAmount: BigDecimal,
      availableFromDate: LocalDate,
      availableToDate: LocalDate,
      totalQuantity: Int,
      validWeekdaysCsv: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val availableFromDateValue = availableFromDate.toString
    val availableToDateValue = availableToDate.toString
    val createdAtValue = createdAt.toString
    sql"""
      insert into ticket_types (
        ticket_type_id, attraction_id, name, description, price_amount, price_currency,
        available_from_date, available_to_date, total_quantity, valid_weekdays, status, created_at
      ) values (
        $ticketTypeId,
        $attractionId,
        $name,
        $description,
        $priceAmount,
        ${"CNY"},
        cast($availableFromDateValue as date),
        cast($availableToDateValue as date),
        $totalQuantity,
        $validWeekdaysCsv,
        ${"Active"},
        cast($createdAtValue as timestamptz)
      )
      on conflict (ticket_type_id) do update set
        attraction_id = excluded.attraction_id,
        name = excluded.name,
        description = excluded.description,
        price_amount = excluded.price_amount,
        price_currency = excluded.price_currency,
        available_from_date = excluded.available_from_date,
        available_to_date = excluded.available_to_date,
        total_quantity = excluded.total_quantity,
        valid_weekdays = excluded.valid_weekdays,
        status = excluded.status,
        created_at = excluded.created_at
    """.update.run

  private def insertOrUpsertTicketRule(
      ruleId: String,
      ticketTypeId: String,
      ruleType: String,
      ruleConfigJson: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtValue = createdAt.toString
    sql"""
      insert into ticket_type_rules (rule_id, ticket_type_id, rule_type, rule_config_json, created_at)
      values ($ruleId, $ticketTypeId, $ruleType, $ruleConfigJson, cast($createdAtValue as timestamptz))
      on conflict (rule_id) do update set
        ticket_type_id = excluded.ticket_type_id,
        rule_type = excluded.rule_type,
        rule_config_json = excluded.rule_config_json,
        created_at = excluded.created_at
    """.update.run

  private def insertOrUpsertTicketSession(
      sessionId: String,
      ticketTypeId: String,
      sessionName: String,
      useDate: LocalDate,
      startsAt: Instant,
      endsAt: Instant,
      capacity: Int,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val useDateValue = useDate.toString
    val createdAtValue = createdAt.toString
    sql"""
      insert into attraction_ticket_sessions (
        session_id, ticket_type_id, session_name, use_date, starts_at, ends_at, capacity, status, created_at
      ) values (
        $sessionId,
        $ticketTypeId,
        $sessionName,
        cast($useDateValue as date),
        cast(${startsAt.toString} as timestamptz),
        cast(${endsAt.toString} as timestamptz),
        $capacity,
        ${"Active"},
        cast($createdAtValue as timestamptz)
      )
      on conflict (session_id) do update set
        ticket_type_id = excluded.ticket_type_id,
        session_name = excluded.session_name,
        use_date = excluded.use_date,
        starts_at = excluded.starts_at,
        ends_at = excluded.ends_at,
        capacity = excluded.capacity,
        status = excluded.status,
        created_at = excluded.created_at
    """.update.run
