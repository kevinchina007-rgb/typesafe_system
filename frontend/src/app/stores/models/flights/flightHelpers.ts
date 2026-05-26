import type { AppLanguage, TravelerResponse } from '@/lib/mvp-types/index'
import { localizeCabinClass, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'

export function renderFlightTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function getFlightStatusLabel(status: string, currentLanguage: AppLanguage) {
  return mapBackendStatusToProductLabel(status, currentLanguage)
}

export function getLocalizedCabinLabel(cabinClass: string, currentLanguage: AppLanguage) {
  return localizeCabinClass(cabinClass, currentLanguage)
}
