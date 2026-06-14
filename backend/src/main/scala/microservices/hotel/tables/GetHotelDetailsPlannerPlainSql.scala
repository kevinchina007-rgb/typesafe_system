// GetHotelDetailsPlannerPlainSql 灏佽閰掑簵妯″潡鐨刾lain SQL 瀹炵幇銆?
package com.typesafe.travel.hotel.tables

// 这个类是 GetHotelDetailsPlanner 的后端 SQL 门面，只负责把详情动作拆成若干次数据库查询并返回组合结果。
// 前端不镜像它，因为它不是用户直接调用的 API 入口，而是 planner 背后的数据库执行步骤。
// 这里存在的价值，是把“查酒店详情”所需的 SQL 和映射逻辑从 planner 中剥离出去，让 planner 保持只做流程编排。

import cats.effect.IO
import com.typesafe.travel.hotel.objects.*

import java.sql.Connection

object GetHotelDetailsPlannerPlainSql:
  def details(connection: Connection, request: GetHotelDetailsPlannerRequest): IO[Option[Hotel]] =
    IO.blocking {
      HotelDetailsSqlSupport.getHotel(connection, request.hotelId)
    }
