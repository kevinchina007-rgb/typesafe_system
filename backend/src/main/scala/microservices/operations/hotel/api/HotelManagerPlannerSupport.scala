// 本文件封装 operations/hotel 模块的公共支撑逻辑。
package com.typesafe.travel.operations.domain

// 这个 support 文件只在后端使用，负责 operations/hotel 这条链路里共享的校验、日期生成、读取封装和映射辅助。
// 前端不需要镜像它，因为它不是用户直接调用的入口，而是多个 hotel manager planner 共用的执行辅助层。
// 这里的职责越清晰，`RegisterHotelManagerPlanner`、`UpdateHotelManagerProfilePlanner`、`CreateManagerRoomTypePlanner` 就越容易保持薄。

import cats.effect.IO

import java.time.LocalDate

def validateRegisterHotel(input: RegisterHotelManagerPlannerRequest): IO[Unit] =
  IO {
    require(input.email.trim.nonEmpty, "email is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
    require(input.hotelName.trim.nonEmpty, "hotelName is required")
    require(input.location.trim.nonEmpty, "location is required")
    require(input.password.nonEmpty, "password is required")
  }

def validateRoomType(input: CreateManagerRoomTypePlannerRequest, startDate: LocalDate, endDate: LocalDate): IO[Unit] =
  IO {
    require(input.managerId.trim.nonEmpty, "managerId is required")
    require(input.roomTypeName.trim.nonEmpty, "roomTypeName is required")
    require(input.capacity > 0, "capacity must be positive")
    require(input.bedType.trim.nonEmpty, "bedType is required")
    require(BigDecimal(input.nightlyPrice) >= BigDecimal(0), "nightlyPrice cannot be negative")
    require(input.currency.trim.nonEmpty, "currency is required")
    require(input.availableRooms >= 0, "availableRooms cannot be negative")
    require(!endDate.isBefore(startDate), "inventoryEndDate cannot be before inventoryStartDate")
  }

def datesBetween(startDate: LocalDate, endDate: LocalDate): List[LocalDate] =
  Iterator.iterate(startDate)(_.plusDays(1)).takeWhile(!_.isAfter(endDate)).toList
