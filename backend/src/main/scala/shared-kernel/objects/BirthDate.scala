package com.typesafe.travel.shared.kernel

import java.time.LocalDate

// 出生日期的领域值对象。
final case class BirthDate(value: LocalDate)

// 出生日期的构造入口和安全/非安全创建方法。
object BirthDate:
  export BirthDateSupport.{createBirthDate as create, unsafeBirthDate as unsafe}
