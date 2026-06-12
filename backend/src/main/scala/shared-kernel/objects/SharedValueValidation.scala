package com.typesafe.travel.shared.kernel

// 通用值校验规则集合。
object SharedValueValidation:
  // 校验字符串字段是否为空或超长。
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
