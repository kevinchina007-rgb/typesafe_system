package com.typesafe.travel.shared.kernel

import java.time.{LocalDate, OffsetDateTime}

trait DomainError extends RuntimeException:
  def message: String
  override def getMessage: String = message

sealed trait SharedKernelError extends DomainError

enum SharedValidationError(val message: String) extends SharedKernelError:
  case RequiredFieldWasEmpty(fieldName: String)
      extends SharedValidationError(s"Field '$fieldName' must not be empty")
  case EmailAddressWasInvalid(emailAddressValue: String)
      extends SharedValidationError(s"Email address '$emailAddressValue' is invalid")
  case CountryCodeWasInvalid(countryCodeValue: String)
      extends SharedValidationError(s"Country code '$countryCodeValue' is invalid")
  case AirportCodeWasInvalid(airportCodeValue: String)
      extends SharedValidationError(s"Airport code '$airportCodeValue' is invalid")
  case MonetaryAmountWasNegative(amountValue: BigDecimal)
      extends SharedValidationError(s"Money amount '$amountValue' must not be negative")
  case MonetaryCurrenciesDidNotMatch(
      operationName: String,
      leftCurrency: Currency,
      rightCurrency: Currency
  ) extends SharedValidationError(
        s"Operation '$operationName' requires matching currencies but received $leftCurrency and $rightCurrency"
      )
  case NumberWasOutOfRange(
      fieldName: String,
      minimumValue: BigDecimal,
      maximumValue: BigDecimal,
      actualValue: BigDecimal
  ) extends SharedValidationError(
        s"Field '$fieldName' must be between $minimumValue and $maximumValue but was $actualValue"
      )
  case TemporalRangeWasInvalid(rangeName: String, startValue: LocalDate, endValue: LocalDate)
      extends SharedValidationError(s"$rangeName is invalid because $endValue is before or equal to $startValue")
  case FlightScheduleWasInvalid(departureAt: OffsetDateTime, arrivalAt: OffsetDateTime)
      extends SharedValidationError(s"Flight schedule is invalid because $arrivalAt is not after $departureAt")
  case BirthDateWasInFuture(birthDateValue: LocalDate)
      extends SharedValidationError(s"Birth date '$birthDateValue' must not be in the future")
  case StringWasTooLong(fieldName: String, maximumLength: Int, actualLength: Int)
      extends SharedValidationError(
        s"Field '$fieldName' must be at most $maximumLength characters but was $actualLength"
      )
