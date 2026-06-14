package com.typesafe.travel.hotel.tables

object HotelRoomTypeImageSqlSupport:
  def normalizeRoomTypeImageUrl(value: String): Option[String] =
    Option(value).map(_.trim).filter(_.nonEmpty)
