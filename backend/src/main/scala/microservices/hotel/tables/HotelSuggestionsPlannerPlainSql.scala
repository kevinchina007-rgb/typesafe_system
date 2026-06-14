// HotelSuggestionsPlannerPlainSql 灏佽閰掑簵妯″潡鐨刾lain SQL 瀹炵幇銆?
package com.typesafe.travel.hotel.tables

// 这个文件只实现酒店搜索建议的后端查询，比如联想词、相关酒店和位置提示等轻量结果。
// 它不需要前端镜像，因为前端只消费 suggestions 的 JSON 结果，不参与生成这些建议的 SQL 规则。
// 单独保留它，是为了让建议链路和主搜索链路分开演进，而不把搜索提示逻辑塞进别的 planner。

import cats.effect.IO
import com.typesafe.travel.hotel.objects.*

import java.sql.Connection

object HotelSuggestionsPlannerPlainSql:
  private val selectHotelSql =
    "select hotel_id, name, location from hotels"

  def suggestions(connection: Connection, request: HotelSuggestionPlannerRequest): IO[SearchSuggestionListPlannerResponse] =
    IO.blocking {
      val statement = connection.prepareStatement(selectHotelSql + " where lower(name) like ? or lower(location) like ? order by name, hotel_id limit 12")
      try
        val like = s"%${request.q.trim.toLowerCase}%"
        statement.setString(1, like)
        statement.setString(2, like)
        val resultSet = statement.executeQuery()
        try
          val rows = List.newBuilder[SearchSuggestionPlannerResponse]
          while resultSet.next() do
            rows += SearchSuggestionPlannerResponse(
              resourceType = "hotel",
              value = resultSet.getString("hotel_id"),
              title = resultSet.getString("name"),
              subtitle = resultSet.getString("location")
            )
          SearchSuggestionListPlannerResponse(rows.result())
        finally resultSet.close()
      finally statement.close()
    }
