package com.typesafe.travel.persistence

import cats.effect.IO
import com.typesafe.travel.api.*

import java.sql.Connection

object ExplorePlannerPlainSql:
  def suggestions(connection: Connection, input: ExploreSuggestionsPlannerRequest): IO[ExploreSuggestionListPlannerResponse] =
    IO.blocking {
      val q = s"%${input.q.trim}%"
      val results =
        querySuggestions(connection, "flight", "select flight_id as id, flight_number as title, departure_airport || ' -> ' || arrival_airport as subtitle from flights where flight_number ilike ? or departure_airport ilike ? or arrival_airport ilike ? limit 6", q) ++
          querySuggestions(connection, "hotel", "select hotel_id as id, name as title, location as subtitle from hotels where name ilike ? or location ilike ? or location ilike ? limit 6", q) ++
          querySuggestions(connection, "attraction", "select attraction_id as id, name as title, city || ' · ' || location as subtitle from attractions where name ilike ? or city ilike ? or location ilike ? limit 6", q) ++
          querySuggestions(connection, "blog", "select post_id as id, title, summary as subtitle from blog_posts where status = 'Published' and (title ilike ? or summary ilike ? or content ilike ?) limit 6", q)
      ExploreSuggestionListPlannerResponse(results.distinctBy(item => item.resourceType -> item.value).take(12))
    }

  def search(connection: Connection, input: ExploreSearchPlannerRequest): IO[ExploreSearchListPlannerResponse] =
    IO.blocking {
      val q = s"%${input.q.trim}%"
      val requested = input.resourceType.map(_.trim.toLowerCase).filter(_.nonEmpty)
      val results =
        include(requested, "flight") {
          queryResults(connection, "flight", "flights", "select flight_id as id, flight_number as title, departure_airport || ' -> ' || arrival_airport as summary, cast(departure_time as text) as meta_label, null as image_url from flights where flight_number ilike ? or departure_airport ilike ? or arrival_airport ilike ? limit 10", q)
        } ++
          include(requested, "hotel") {
            queryResults(connection, "hotel", "hotels", "select hotel_id as id, name as title, location as summary, status as meta_label, null as image_url from hotels where name ilike ? or location ilike ? or location ilike ? limit 10", q)
          } ++
          include(requested, "attraction") {
            queryResults(connection, "attraction", "attractions", "select attraction_id as id, name as title, city || ' · ' || location as summary, status as meta_label, null as image_url from attractions where name ilike ? or city ilike ? or location ilike ? limit 10", q)
          } ++
          include(requested, "blog") {
            queryResults(connection, "blog", "blog", "select post_id as id, title, summary, status as meta_label, null as image_url from blog_posts where status = 'Published' and (title ilike ? or summary ilike ? or content ilike ?) limit 10", q)
          }
      ExploreSearchListPlannerResponse(results.sortBy(result => (-result.score, result.title)).take(30))
    }

  private def include[A](requested: Option[String], resourceType: String)(value: => List[A]): List[A] =
    if requested.exists(_ != resourceType) then Nil else value

  private def querySuggestions(connection: Connection, resourceType: String, sql: String, q: String): List[ExploreSuggestionPlannerResponse] =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      statement.setString(1, q)
      statement.setString(2, q)
      statement.setString(3, q)
      PlainSqlSupport.queryList(statement) { resultSet =>
        ExploreSuggestionPlannerResponse(resourceType, resultSet.getString("id"), resultSet.getString("title"), resultSet.getString("subtitle"))
      }
    }

  private def queryResults(connection: Connection, resourceType: String, navigationHint: String, sql: String, q: String): List[ExploreSearchResultPlannerResponse] =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      statement.setString(1, q)
      statement.setString(2, q)
      statement.setString(3, q)
      PlainSqlSupport.queryList(statement) { resultSet =>
        val title = resultSet.getString("title")
        val summary = resultSet.getString("summary")
        ExploreSearchResultPlannerResponse(
          resourceType,
          resultSet.getString("id"),
          title,
          summary,
          resultSet.getString("meta_label"),
          navigationHint,
          Option(resultSet.getString("image_url")),
          score = weightedScore(q.stripPrefix("%").stripSuffix("%"), title, summary)
        )
      }
    }

  private def weightedScore(query: String, title: String, summary: String): Int =
    val normalized = query.trim.toLowerCase
    (if title.toLowerCase.contains(normalized) then 4 else 0) + (if summary.toLowerCase.contains(normalized) then 2 else 0)
