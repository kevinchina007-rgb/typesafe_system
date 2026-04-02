package com.typesafe.travel.attraction.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.traveler.domain.TravelerProfile

import java.time.LocalDate
import java.time.Period

trait TicketEligibilityService[F[_]]:
  def evaluateTraveler(
      travelerProfile: TravelerProfile,
      ticketType: TicketType,
      useDate: LocalDate
  ): F[TicketEligibilityResult]

final class LiveTicketEligibilityService[F[_]: MonadThrow]
    extends TicketEligibilityService[F]:
  override def evaluateTraveler(
      travelerProfile: TravelerProfile,
      ticketType: TicketType,
      useDate: LocalDate
  ): F[TicketEligibilityResult] =
    ticketType.eligibilityRules.toVector.traverse(ruleFailureReason(travelerProfile, _, useDate)).map { failureReasons =>
      val collectedFailureReasons = failureReasons.flatten
      TicketEligibilityResult(
        travelerId = travelerProfile.travelerId,
        eligible = collectedFailureReasons.isEmpty,
        failureReasons = collectedFailureReasons
      )
    }

  private def ruleFailureReason(
      travelerProfile: TravelerProfile,
      ticketEligibilityRule: TicketEligibilityRule,
      useDate: LocalDate
  ): F[Option[String]] =
    MonadThrow[F].fromEither(ticketEligibilityRule.parseConfig).map {
      case TicketEligibilityRuleConfig.AgeLessThan(maxExclusive) =>
        val travelerAge = Period.between(travelerProfile.travelerBirthDate.value, useDate).getYears
        Option.when(!(travelerAge < maxExclusive))(s"Age must be below $maxExclusive")
      case TicketEligibilityRuleConfig.AgeBetween(minInclusive, maxInclusive) =>
        val travelerAge = Period.between(travelerProfile.travelerBirthDate.value, useDate).getYears
        Option.when(travelerAge < minInclusive || travelerAge > maxInclusive)(
          s"Age must be between $minInclusive and $maxInclusive"
        )
      case TicketEligibilityRuleConfig.AgeAtLeast(minInclusive) =>
        val travelerAge = Period.between(travelerProfile.travelerBirthDate.value, useDate).getYears
        Option.when(travelerAge < minInclusive)(s"Age must be at least $minInclusive")
      case TicketEligibilityRuleConfig.DocumentTypeEquals(documentType) =>
        Option.when(travelerProfile.travelerDocumentType != documentType)(s"Document type must be ${documentType.toString}")
      case TicketEligibilityRuleConfig.DocumentNumberPrefix(prefix) =>
        Option.when(!travelerProfile.travelerDocumentNumber.value.startsWith(prefix))(s"Document number must start with $prefix")
    }

object TicketEligibilityService:
  def apply[F[_]: MonadThrow](): TicketEligibilityService[F] =
    new LiveTicketEligibilityService[F]
