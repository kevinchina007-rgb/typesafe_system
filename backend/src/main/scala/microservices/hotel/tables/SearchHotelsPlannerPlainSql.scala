// 本文件封装 SearchHotelsPlanner 对应的 plain SQL 实现。
package com.typesafe.travel.hotel.tables

// 这个类是搜索酒店 planner 的后端 SQL 门面，用来收口搜索条件、分页/排序策略以及搜索结果映射。
// 前端不应该镜像它，因为前端不关心 SQL 的筛选顺序、联表方式和结果去重逻辑；前端只负责发请求和渲染 response。
// 它和 `SearchHotelsPlanner.ts` 是一前一后的分工关系，不是同层级的重复定义。

import cats.effect.IO
import com.typesafe.travel.hotel.objects.*

import java.sql.Connection

object SearchHotelsPlannerPlainSql:
  def list(connection: Connection, request: HotelSearchPlannerRequest): IO[List[Hotel]] =
    IO.blocking {
      HotelSearchSqlSupport.listHotels(connection, request.location)
    }
