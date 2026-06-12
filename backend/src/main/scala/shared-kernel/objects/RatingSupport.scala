// RatingSupport 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

object RatingSupport:
  def createRating(value: Int): Either[SharedValidationError, Rating] =
    if value >= 1 && value <= 5 then Right(Rating(value))
    else Left(SharedValidationError.NumberWasOutOfRange("rating", BigDecimal(1), BigDecimal(5), BigDecimal(value)))

  def unsafeRating(value: Int): Rating =
    createRating(value).fold(throw _, identity)
