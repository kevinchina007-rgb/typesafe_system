package com.typesafe.travel.shared.kernel

object SharedValueValidation:
  def validateTrimmedValue(
      fieldName: String,
      value: String,
      maximumLength: Int
  ): Either[SharedValidationError, String] =
    val normalizedValue = value.trim
    if normalizedValue.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty(fieldName))
    else if normalizedValue.length > maximumLength then
      Left(SharedValidationError.StringWasTooLong(fieldName, maximumLength, normalizedValue.length))
    else Right(normalizedValue)

