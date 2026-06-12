// PrincipalIdentifiers 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

final case class UserId(value: String) extends AnyVal
final case class CredentialId(value: String) extends AnyVal
final case class SessionId(value: String) extends AnyVal
final case class TravelerId(value: String) extends AnyVal
final case class ManagerId(value: String) extends AnyVal

