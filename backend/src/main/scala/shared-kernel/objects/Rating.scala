// Rating 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

final case class Rating(value: Int)
object Rating:
  export RatingSupport.{createRating as create, unsafeRating as unsafe}
