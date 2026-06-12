// PersonAndIdentityValuesSupport 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

object PersonAndIdentityValuesSupport:
  def createPersonName(value: String): Either[SharedValidationError, PersonName] =
    SharedValueValidation.validateTrimmedValue("person-name", value, 120).map(PersonName.apply)

  def unsafePersonName(value: String): PersonName =
    createPersonName(value).fold(throw _, identity)

  private val SimpleEmailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".r

  def createEmailAddress(value: String): Either[SharedValidationError, EmailAddress] =
    val normalizedValue = value.trim
    if normalizedValue.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("email-address"))
    else if normalizedValue.length > 200 then
      Left(SharedValidationError.StringWasTooLong("email-address", 200, normalizedValue.length))
    else if SimpleEmailRegex.matches(normalizedValue) then Right(EmailAddress(normalizedValue))
    else Left(SharedValidationError.EmailAddressWasInvalid(normalizedValue))

  def unsafeEmailAddress(value: String): EmailAddress =
    createEmailAddress(value).fold(throw _, identity)

  def createDocumentNumber(value: String): Either[SharedValidationError, DocumentNumber] =
    SharedValueValidation.validateTrimmedValue("document-number", value, 60).map(DocumentNumber.apply)

  def unsafeDocumentNumber(value: String): DocumentNumber =
    createDocumentNumber(value).fold(throw _, identity)

  def createCountryCode(value: String): Either[SharedValidationError, CountryCode] =
    val normalizedValue = value.trim.toUpperCase
    if normalizedValue.matches("^[A-Z]{2}$") then Right(CountryCode(normalizedValue))
    else Left(SharedValidationError.CountryCodeWasInvalid(normalizedValue))

  def unsafeCountryCode(value: String): CountryCode =
    createCountryCode(value).fold(throw _, identity)

  def createContactNumber(value: String): Either[SharedValidationError, ContactNumber] =
    SharedValueValidation.validateTrimmedValue("contact-number", value, 40).map(ContactNumber.apply)

  def unsafeContactNumber(value: String): ContactNumber =
    createContactNumber(value).fold(throw _, identity)

  def createLoyaltyProgramName(value: String): Either[SharedValidationError, LoyaltyProgramName] =
    SharedValueValidation.validateTrimmedValue("loyalty-program-name", value, 80).map(LoyaltyProgramName.apply)

  def unsafeLoyaltyProgramName(value: String): LoyaltyProgramName =
    createLoyaltyProgramName(value).fold(throw _, identity)

  def createAvatarUrl(value: String): Either[SharedValidationError, AvatarUrl] =
    val normalizedValue = value.trim
    if normalizedValue.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("avatar-url"))
    else if normalizedValue.length > 300 then
      Left(SharedValidationError.StringWasTooLong("avatar-url", 300, normalizedValue.length))
    else if normalizedValue.startsWith("/uploads/avatars/") then Right(AvatarUrl(normalizedValue))
    else Left(SharedValidationError.AvatarUrlWasInvalid(normalizedValue))

  def unsafeAvatarUrl(value: String): AvatarUrl =
    createAvatarUrl(value).fold(throw _, identity)
