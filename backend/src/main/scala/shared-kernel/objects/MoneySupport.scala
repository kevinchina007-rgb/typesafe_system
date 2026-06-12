// MoneySupport 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

object MoneySupport:
  extension (money: Money)
    def add(otherMoney: Money): Either[SharedValidationError, Money] =
      if money.currency == otherMoney.currency then
        Right(unsafeMoney(money.amount + otherMoney.amount, money.currency))
      else
        Left(SharedValidationError.MonetaryCurrenciesDidNotMatch("money-addition", money.currency, otherMoney.currency))

    def subtract(otherMoney: Money): Either[SharedValidationError, Money] =
      if money.currency != otherMoney.currency then
        Left(SharedValidationError.MonetaryCurrenciesDidNotMatch("money-subtraction", money.currency, otherMoney.currency))
      else
        createMoney(money.amount - otherMoney.amount, money.currency)

    def multiply(multiplier: Int): Either[SharedValidationError, Money] =
      if multiplier >= 0 then createMoney(money.amount * BigDecimal(multiplier), money.currency)
      else Left(SharedValidationError.NumberWasOutOfRange("money-multiplier", BigDecimal(0), BigDecimal(Int.MaxValue), BigDecimal(multiplier)))

  def createMoney(amount: BigDecimal, currency: Currency): Either[SharedValidationError, Money] =
    if amount >= 0 then Right(Money(amount, currency))
    else Left(SharedValidationError.MonetaryAmountWasNegative(amount))

  def unsafeMoney(amount: BigDecimal, currency: Currency): Money =
    createMoney(amount, currency).fold(throw _, identity)

  def zeroMoney(currency: Currency): Money = unsafeMoney(BigDecimal(0), currency)
