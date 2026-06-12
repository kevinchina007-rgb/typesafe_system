// AttractionStatusesSupport 定义景点模块的状态解析辅助。

package com.typesafe.travel.attraction.domain

object AttractionStatusesSupport:
  def parseAttractionStatus(value: String): AttractionStatus =
    value.trim.toLowerCase match
      case "draft" => AttractionStatus.Draft
      case "published" => AttractionStatus.Published
      case "closed" => AttractionStatus.Closed
      case _ => AttractionStatus.Draft

  def parseTicketTypeStatus(value: String): TicketTypeStatus =
    value.trim.toLowerCase match
      case "active" => TicketTypeStatus.Active
      case "inactive" => TicketTypeStatus.Inactive
      case _ => TicketTypeStatus.Inactive

  def parseAttractionTicketSessionStatus(value: String): AttractionTicketSessionStatus =
    value.trim.toLowerCase match
      case "active" => AttractionTicketSessionStatus.Active
      case "closed" => AttractionTicketSessionStatus.Closed
      case _ => AttractionTicketSessionStatus.Closed

  def parseTicketEligibilityRuleType(value: String): TicketEligibilityRuleType =
    value.trim.toLowerCase match
      case "agelessthan" | "age_less_than" => TicketEligibilityRuleType.AgeLessThan
      case "agebetween" | "age_between" => TicketEligibilityRuleType.AgeBetween
      case "ageatleast" | "age_at_least" => TicketEligibilityRuleType.AgeAtLeast
      case "documenttypeequals" | "document_type_equals" => TicketEligibilityRuleType.DocumentTypeEquals
      case "documentnumberprefix" | "document_number_prefix" => TicketEligibilityRuleType.DocumentNumberPrefix
      case _ => TicketEligibilityRuleType.DocumentNumberPrefix
