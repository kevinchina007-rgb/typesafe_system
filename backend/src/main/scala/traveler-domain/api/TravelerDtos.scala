package com.typesafe.travel.api.dto

import com.typesafe.travel.traveler.domain.TravelerProfile

final case class CreateTravelerRequestDto(
    fullName: String,
    documentType: String,
    documentNumber: String,
    phone: String,
    birthDate: String,
    seatPreference: String,
    mealPreference: String,
    accessibilityRequestNotes: Option[String],
    emergencyContactName: Option[String],
    emergencyContactPhoneNumber: Option[String],
    isDefaultTraveler: Boolean
)

final case class TravelerResponseDto(
    travelerId: String,
    ownerUserId: String,
    fullName: String,
    documentType: String,
    documentNumber: String,
    phone: String,
    birthDate: String,
    travelerType: String,
    status: String,
    isDefault: Boolean
)

final case class TravelerListResponseDto(
    travelers: List[TravelerResponseDto]
)

object TravelerResponseDto:
  def fromDomain(travelerProfile: TravelerProfile): TravelerResponseDto =
    TravelerResponseDto(
      travelerId = travelerProfile.travelerId.value,
      ownerUserId = travelerProfile.ownerUserId.value,
      fullName = travelerProfile.travelerFullName.value,
      documentType = travelerProfile.travelerDocumentType.toString,
      documentNumber = travelerProfile.travelerDocumentNumber.value,
      phone = travelerProfile.travelerPhoneNumber.value,
      birthDate = travelerProfile.travelerBirthDate.value.toString,
      travelerType = travelerProfile.travelerType.toString,
      status = travelerProfile.travelerProfileStatus.toString,
      isDefault = travelerProfile.isDefaultTravelerProfile
    )
