package com.typesafe.travel.shared.kernel

// 金额和币种组合的领域值对象。
final case class Money(amount: BigDecimal, currency: Currency)

// 金额的构造和基础运算入口。
object Money:
  export MoneySupport.{add, subtract, multiply}
  export MoneySupport.{createMoney as create, unsafeMoney as unsafe, zeroMoney as zero}
