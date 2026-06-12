// TrainValueObjectsSupport 定义火车模块的辅助定义。

package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*

object TrainValueObjectsSupport:
  def createTrainStationCode(value: String): Either[SharedValidationError, TrainStationCode] =
    TrainStationCode.create(value)

  def unsafeTrainStationCode(value: String): TrainStationCode =
    TrainStationCode.unsafe(value)

  def createTrainStationName(value: String): Either[SharedValidationError, TrainStationName] =
    TrainStationName.create(value)

  def unsafeTrainStationName(value: String): TrainStationName =
    TrainStationName.unsafe(value)

  def createTrainNumber(value: String): Either[SharedValidationError, TrainNumber] =
    TrainNumber.create(value)

  def unsafeTrainNumber(value: String): TrainNumber =
    TrainNumber.unsafe(value)

  def createTrainSeatClass(value: String): Either[SharedValidationError, TrainSeatClass] =
    TrainSeatClass.create(value)

  def unsafeTrainSeatClass(value: String): TrainSeatClass =
    TrainSeatClass.unsafe(value)

  def createTrainSeatRowNo(value: Int): Either[SharedValidationError, TrainSeatRowNo] =
    TrainSeatRowNo.create(value)

  def createRefundRate(value: BigDecimal): Either[SharedValidationError, RefundRate] =
    RefundRate.create(value)

  def unsafeRefundRate(value: BigDecimal): RefundRate =
    RefundRate.unsafe(value)
