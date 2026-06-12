// ApiPlan 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.api.routes

import cats.effect.IO
import io.circe.{Decoder, Encoder}

import java.sql.Connection

trait PlainApiPlan[Input: Decoder, Output: Encoder]:
  def name: String
  def plan(input: Input): IO[Output]

trait ConnectionApiPlan[Input: Decoder, Output: Encoder]:
  def name: String
  def plan(input: Input, connection: Connection): IO[Output]
