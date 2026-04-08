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
  TravelerProfile.create(
    travelerId,
    ownerUserId,
    travelerFullName,
    travelerDocumentType,
    travelerDocumentNumber,
    travelerPhoneNumber,
    travelerBirthDate,
    travelerType,
    travelerPreferences,
    travelerEmergencyContact,
    isDefaultTravelerProfile
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
  TravelerProfile.restore(
    travelerId,
    ownerUserId,
    travelerFullName,
    travelerDocumentType,
    travelerDocumentNumber,
    travelerPhoneNumber,
    travelerBirthDate,
    travelerType,
    travelerIdentityDocuments,
    travelerEmergencyContact,
    travelerLoyaltyMemberships,
    travelerPreferences,
    travelerProfileStatus,
    isDefaultTravelerProfile
  )
