// 这个文件是 tour-group 后端 planner 级 Plain SQL 的底层共享 support。
// 它提供读取团体、成员、计划项、选择、黑名单以及生成 ID 之类的基础数据库辅助函数，供上层 planner 编排调用。
// 前端不应该镜像这个文件；它只属于后端持久化实现。
package com.typesafe.travel.tourgroup.domain

object TourGroupPlannerPlainSqlSupport:
  export TourGroupMembershipSupport.*
  export TourGroupSelectionSupport.*
  export TourGroupBookingSupport.*
