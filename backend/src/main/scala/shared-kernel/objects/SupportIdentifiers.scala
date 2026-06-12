// SupportIdentifiers 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

final case class StaffId(value: String) extends AnyVal
final case class AuditTaskId(value: String) extends AnyVal
final case class SupportTicketId(value: String) extends AnyVal
final case class SupportMessageId(value: String) extends AnyVal

