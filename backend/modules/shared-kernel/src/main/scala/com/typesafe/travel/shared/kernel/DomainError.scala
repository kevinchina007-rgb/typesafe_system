package com.typesafe.travel.shared.kernel

trait DomainError extends RuntimeException:
  def message: String
  override def getMessage: String = message
