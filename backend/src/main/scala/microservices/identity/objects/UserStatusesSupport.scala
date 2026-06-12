// UserStatusesSupport 定义身份模块的状态解析辅助。

package com.typesafe.travel.identity.domain

object UserStatusesSupport:
  def parseUserAccountStatus(value: String): UserAccountStatus =
    value.trim.toLowerCase match
      case "pendingactivation" | "pending_activation" => UserAccountStatus.PendingActivation
      case "active" => UserAccountStatus.Active
      case "suspended" => UserAccountStatus.Suspended
      case "closed" => UserAccountStatus.Closed
      case _ => UserAccountStatus.PendingActivation

  def parseUserMembershipLevel(value: String): UserMembershipLevel =
    value.trim.toLowerCase match
      case "silver" => UserMembershipLevel.Silver
      case "gold" => UserMembershipLevel.Gold
      case "platinum" => UserMembershipLevel.Platinum
      case _ => UserMembershipLevel.Standard
