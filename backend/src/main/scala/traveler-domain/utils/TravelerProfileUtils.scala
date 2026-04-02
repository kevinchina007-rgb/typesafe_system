package com.typesafe.travel.traveler.domain

import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

def newTravelerProfile(
    travelerId: TravelerId,
    ownerUserId: UserId,
    travelerFullName: PersonName,
    travelerDocumentType: TravelerDocumentType,
    travelerDocumentNumber: DocumentNumber,
    travelerPhoneNumber: ContactNumber,
    travelerBirthDate: BirthDate,
    travelerType: TravelerType,
    travelerPreferences: TravelerPreferences,
    travelerEmergencyContact: Option[TravelerEmergencyContact],
    isDefaultTravelerProfile: Boolean
): TravelerProfile =
  TravelerProfile(
    travelerId = travelerId,
    ownerUserId = ownerUserId,
    travelerFullName = travelerFullName,
    travelerDocumentType = travelerDocumentType,
    travelerDocumentNumber = travelerDocumentNumber,
    travelerPhoneNumber = travelerPhoneNumber,
    travelerBirthDate = travelerBirthDate,
    travelerType = travelerType,
    travelerIdentityDocuments = Nil,
    travelerEmergencyContact = travelerEmergencyContact,
    travelerLoyaltyMemberships = Nil,
    travelerPreferences = travelerPreferences,
    travelerProfileStatus = TravelerProfileStatus.Draft,
    isDefaultTravelerProfile = isDefaultTravelerProfile
  )


def restoreTravelerProfile(
    travelerId: TravelerId,
    ownerUserId: UserId,
    travelerFullName: PersonName,
    travelerDocumentType: TravelerDocumentType,
    travelerDocumentNumber: DocumentNumber,
    travelerPhoneNumber: ContactNumber,
    travelerBirthDate: BirthDate,
    travelerType: TravelerType,
    travelerIdentityDocuments: List[TravelerIdentityDocument],
    travelerEmergencyContact: Option[TravelerEmergencyContact],
    travelerLoyaltyMemberships: List[TravelerLoyaltyMembership],
    travelerPreferences: TravelerPreferences,
    travelerProfileStatus: TravelerProfileStatus,
    isDefaultTravelerProfile: Boolean
): TravelerProfile =
  TravelerProfile(
    travelerId = travelerId,
    ownerUserId = ownerUserId,
    travelerFullName = travelerFullName,
    travelerDocumentType = travelerDocumentType,
    travelerDocumentNumber = travelerDocumentNumber,
    travelerPhoneNumber = travelerPhoneNumber,
    travelerBirthDate = travelerBirthDate,
    travelerType = travelerType,
    travelerIdentityDocuments = travelerIdentityDocuments,
    travelerEmergencyContact = travelerEmergencyContact,
    travelerLoyaltyMemberships = travelerLoyaltyMemberships,
    travelerPreferences = travelerPreferences,
    travelerProfileStatus = travelerProfileStatus,
    isDefaultTravelerProfile = isDefaultTravelerProfile
  )
