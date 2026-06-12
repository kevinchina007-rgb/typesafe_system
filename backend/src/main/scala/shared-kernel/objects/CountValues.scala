package com.typesafe.travel.shared.kernel

// 点数的领域值对象。
final case class Points(value: Long)

// 点数的构造入口和加减操作。
object Points:
  export CountValuesSupport.{add, subtract}
  export CountValuesSupport.{createPoints as create, unsafePoints as unsafe, zeroPoints as zero}

// 容量的领域值对象。
final case class Capacity(value: Int)

// 容量的构造入口。
object Capacity:
  export CountValuesSupport.{createCapacity as create, unsafeCapacity as unsafe}

// 座位数的领域值对象。
final case class SeatCount(value: Int)

// 座位数的构造入口。
object SeatCount:
  export CountValuesSupport.{createSeatCount as create, unsafeSeatCount as unsafe}

// 房间数的领域值对象。
final case class RoomCount(value: Int)

// 房间数的构造入口。
object RoomCount:
  export CountValuesSupport.{createRoomCount as create, unsafeRoomCount as unsafe}
