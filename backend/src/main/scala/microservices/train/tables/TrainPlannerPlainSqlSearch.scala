// TrainPlannerPlainSqlSearch 只负责 train 模块的后端搜索 SQL 与结果映射，属于纯后端实现，不需要前端镜像。
package com.typesafe.travel.train.domain

import cats.effect.IO

import java.sql.Connection
import java.time.Instant

object TrainPlannerPlainSqlSearch:
  def suggestions(connection: Connection, input: TrainSuggestionsPlannerRequest): IO[TrainSuggestionListPlannerResponse] =
    TrainPlannerPlainSqlShared.ensureReferenceData(connection) *>
      IO.blocking {
        val q = s"%${TrainPlannerPlainSqlShared.normalizeTrainStationQuery(input.q).toLowerCase}%"
        com.typesafe.travel.persistence.PlainSqlSupport.withStatement(
          connection,
          """
            select distinct t.train_id, t.train_number, s1.station_name as from_station, s2.station_name as to_station
            from trains t
            join train_stops s1 on s1.train_id = t.train_id
            join train_stops s2 on s2.train_id = t.train_id and s2.sequence_no > s1.sequence_no
            where lower(t.train_number) like ? or lower(s1.station_name) like ? or lower(s2.station_name) like ?
            order by t.train_number
            limit 10
          """
        ) { statement =>
          statement.setString(1, q)
          statement.setString(2, q)
          statement.setString(3, q)
          TrainSuggestionListPlannerResponse(
            com.typesafe.travel.persistence.PlainSqlSupport.queryList(statement) { row =>
              TrainSuggestionsPlannerResponse(
                resourceType = "train",
                value = row.getString("train_id"),
                title = row.getString("train_number"),
                subtitle = s"${row.getString("from_station")} -> ${row.getString("to_station")}"
              )
            }
          )
        }
      }

  def search(connection: Connection, input: SearchTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    val baseSql =
      """
        where (? is null or exists (select 1 from train_stops ds where ds.train_id = t.train_id and cast(coalesce(ds.departure_time, ds.arrival_time) as date) = cast(? as date)))
        order by t.sale_starts_at, t.train_id
      """
    TrainPlannerPlainSqlShared.queryTrains(connection, baseSql, List(input.date.orNull, input.date.orNull), now).flatMap { response =>
      val filteredResponse = response.filter(train => TrainPlannerPlainSqlShared.trainMatchesSearch(train, input))
      if filteredResponse.nonEmpty then IO.pure(TrainListPlannerResponse(filteredResponse))
      else
        TrainPlannerPlainSqlShared.ensureReferenceData(connection) *>
          TrainPlannerPlainSqlShared.queryTrains(connection, baseSql, List(input.date.orNull, input.date.orNull), now).map(response => TrainListPlannerResponse(response.filter(train => TrainPlannerPlainSqlShared.trainMatchesSearch(train, input))))
    }
