package com.typesafe.travel.order.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.{ApiPlan, ConnectionApiPlan, PlainApiPlan}
import io.circe.{Decoder, Encoder}

import java.sql.Connection

trait OrderApiPlan[Input, Output] extends ApiPlan[Input, Output]

trait OrderPlainApiPlan[Input: Decoder, Output: Encoder] extends OrderApiPlan[Input, Output], PlainApiPlan[Input, Output]:
  def plan(input: Input): IO[Output]

trait OrderConnectionApiPlan[Input: Decoder, Output: Encoder] extends OrderApiPlan[Input, Output], ConnectionApiPlan[Input, Output]:
  def plan(input: Input, connection: Connection): IO[Output]
