// CurrencySupport 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

object CurrencySupport:
  def parseCurrency(value: String): Currency =
    value.trim.toUpperCase match
      case "USD" => Currency.USD
      case "EUR" => Currency.EUR
      case "CNY" => Currency.CNY
      case other => throw SharedValidationError.CurrencyWasInvalid(other)
