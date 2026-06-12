// BirthDateSupport 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

import java.time.LocalDate

object BirthDateSupport:
  def createBirthDate(value: LocalDate, currentDate: LocalDate): Either[SharedValidationError, BirthDate] =
    if value.isAfter(currentDate) then Left(SharedValidationError.BirthDateWasInFuture(value))
    else Right(BirthDate(value))

  def unsafeBirthDate(value: LocalDate): BirthDate =
    BirthDate(value)
