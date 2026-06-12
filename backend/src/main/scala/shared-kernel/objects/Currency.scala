package com.typesafe.travel.shared.kernel

// 统一货币枚举。
enum Currency:
  case USD, EUR, CNY

// 货币枚举的辅助入口。
object Currency:
  val all: Vector[Currency] = Vector(Currency.USD, Currency.EUR, Currency.CNY)
  export CurrencySupport.parseCurrency as fromText
