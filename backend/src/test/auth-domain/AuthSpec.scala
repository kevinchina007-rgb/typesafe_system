package com.typesafe.travel.auth.domain

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.typesafe.travel.shared.kernel.EmailAddress
import munit.FunSuite

final class AuthSpec extends FunSuite:

  test("password hashing verifies and rejects weak passwords") {
    val loginEmail = EmailAddress.unsafe("auth-phase2@example.com")

    val passwordHash = hashPasswordForLoginEmail[IO]("StrongPass123", loginEmail).unsafeRunSync()
    val matches = verifyPassword[IO]("StrongPass123", passwordHash).unsafeRunSync()
    val doesNotMatch = verifyPassword[IO]("WrongPass123", passwordHash).unsafeRunSync()
    val weakPasswordAttempt = hashPasswordForLoginEmail[IO]("password123", loginEmail).attempt.unsafeRunSync()

    assert(matches)
    assert(!doesNotMatch)
    assert(weakPasswordAttempt.swap.exists(_ == AuthError.PasswordWasTooWeak))
  }
