// TrainStatusesSupport 定义火车模块的状态解析辅助。

package com.typesafe.travel.train.domain

object TrainStatusesSupport:
  def parseTrainJourneyStatus(value: String): TrainJourneyStatus =
    value.trim.toLowerCase match
      case "draft" => TrainJourneyStatus.Draft
      case "onsale" | "on_sale" => TrainJourneyStatus.OnSale
      case "closed" => TrainJourneyStatus.Closed
      case _ => TrainJourneyStatus.Draft

  def parseTrainSeatInventoryStatus(value: String): TrainSeatInventoryStatus =
    value.trim.toLowerCase match
      case "openforsale" | "open_for_sale" => TrainSeatInventoryStatus.OpenForSale
      case "soldout" | "sold_out" => TrainSeatInventoryStatus.SoldOut
      case "closed" => TrainSeatInventoryStatus.Closed
      case _ => TrainSeatInventoryStatus.Closed

  def parseTrainRefundType(value: String): TrainRefundType =
    value.trim.toLowerCase match
      case "fullrefund" | "full_refund" | "full" => TrainRefundType.FullRefund
      case "partialrefund" | "partial_refund" | "partial" => TrainRefundType.PartialRefund
      case "nonrefundable" | "non_refundable" | "none" => TrainRefundType.NonRefundable
      case _ => TrainRefundType.NonRefundable

  def parseTrainSeatPositionType(value: String): TrainSeatPositionType =
    value.trim.toLowerCase match
      case "window" => TrainSeatPositionType.Window
      case "aisle" => TrainSeatPositionType.Aisle
      case "middle" => TrainSeatPositionType.Middle
      case "other" => TrainSeatPositionType.Other
      case _ => TrainSeatPositionType.Other

  def parseTrainSeatStatus(value: String): TrainSeatStatus =
    value.trim.toLowerCase match
      case "available" => TrainSeatStatus.Available
      case "unavailable" => TrainSeatStatus.Unavailable
      case _ => TrainSeatStatus.Unavailable

  def parseTrainSeatPreference(value: String): TrainSeatPreference =
    value.trim.toLowerCase match
      case "window" => TrainSeatPreference.Window
      case "aisle" => TrainSeatPreference.Aisle
      case "middle" => TrainSeatPreference.Middle
      case "nopreference" | "no_preference" => TrainSeatPreference.NoPreference
      case _ => TrainSeatPreference.NoPreference
