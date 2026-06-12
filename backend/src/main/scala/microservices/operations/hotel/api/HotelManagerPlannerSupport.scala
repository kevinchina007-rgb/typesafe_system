// HotelManagerPlannerSupport 负责operations相关实现。

package com.typesafe.travel.operations.domain

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
