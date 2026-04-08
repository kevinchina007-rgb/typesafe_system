package com.typesafe.travel.shared.kernel

final case class PersonName private (value: String)
object PersonName:
  def create(value: String): Either[SharedValidationError, PersonName] =
    SharedValueValidation.validateTrimmedValue("person-name", value, 120).map(PersonName.apply)

  def unsafe(value: String): PersonName =
    create(value).fold(throw _, identity)

final case class EmailAddress private (value: String)
object EmailAddress:
  private val SimpleEmailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".r

  def create(value: String): Either[SharedValidationError, EmailAddress] =
    val normalizedValue = value.trim
    if normalizedValue.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("email-address"))
    else if normalizedValue.length > 200 then
      Left(SharedValidationError.StringWasTooLong("email-address", 200, normalizedValue.length))
    else if SimpleEmailRegex.matches(normalizedValue) then Right(EmailAddress(normalizedValue))
    else Left(SharedValidationError.EmailAddressWasInvalid(normalizedValue))

  def unsafe(value: String): EmailAddress =
    create(value).fold(throw _, identity)

final case class DocumentNumber private (value: String)
object DocumentNumber:
  def create(value: String): Either[SharedValidationError, DocumentNumber] =
    SharedValueValidation.validateTrimmedValue("document-number", value, 60).map(DocumentNumber.apply)

  def unsafe(value: String): DocumentNumber =
    create(value).fold(throw _, identity)

final case class CountryCode private (value: String)
object CountryCode:
  def create(value: String): Either[SharedValidationError, CountryCode] =
    val normalizedValue = value.trim.toUpperCase
    if normalizedValue.matches("^[A-Z]{2}$") then Right(CountryCode(normalizedValue))
    else Left(SharedValidationError.CountryCodeWasInvalid(normalizedValue))

  def unsafe(value: String): CountryCode =
    create(value).fold(throw _, identity)

final case class ContactNumber private (value: String)
object ContactNumber:
  def create(value: String): Either[SharedValidationError, ContactNumber] =
    SharedValueValidation.validateTrimmedValue("contact-number", value, 40).map(ContactNumber.apply)

  def unsafe(value: String): ContactNumber =
    create(value).fold(throw _, identity)

final case class LoyaltyProgramName private (value: String)
object LoyaltyProgramName:
  def create(value: String): Either[SharedValidationError, LoyaltyProgramName] =
    SharedValueValidation.validateTrimmedValue("loyalty-program-name", value, 80).map(LoyaltyProgramName.apply)

  def unsafe(value: String): LoyaltyProgramName =
    create(value).fold(throw _, identity)

final case class AvatarUrl private (value: String)
object AvatarUrl:
  def create(value: String): Either[SharedValidationError, AvatarUrl] =
    val normalizedValue = value.trim
    if normalizedValue.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("avatar-url"))
    else if normalizedValue.length > 300 then
      Left(SharedValidationError.StringWasTooLong("avatar-url", 300, normalizedValue.length))
    else if normalizedValue.startsWith("/uploads/avatars/") then Right(AvatarUrl(normalizedValue))
    else Left(SharedValidationError.AvatarUrlWasInvalid(normalizedValue))

  def unsafe(value: String): AvatarUrl =
    create(value).fold(throw _, identity)

