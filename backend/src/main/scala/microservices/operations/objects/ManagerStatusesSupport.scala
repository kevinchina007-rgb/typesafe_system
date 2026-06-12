// ManagerStatusesSupport 定义operations模块的状态解析辅助。

package com.typesafe.travel.operations.domain

object ManagerStatusesSupport:
  def parseManagerStatus(value: String): ManagerStatus =
    value.trim.toLowerCase match
      case "inactive" => ManagerStatus.Inactive
      case _ => ManagerStatus.Active

  def parseManagerType(value: String): ManagerType =
    value.trim.toLowerCase match
      case "hotel" => ManagerType.Hotel
      case "attraction" => ManagerType.Attraction
      case _ => ManagerType.Airline
