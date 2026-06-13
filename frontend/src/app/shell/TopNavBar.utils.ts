import type { ManagerProfileOverride } from '@/app/shell/TopNavBar.types'

export const maximumAvatarBytes = 2 * 1024 * 1024
export const allowedAvatarMimeTypes = new Set(['image/png', 'image/jpeg', 'image/jpg'])

export function getInitials(label: string) {
  return label.trim().slice(0, 2).toUpperCase() || '游客'
}

export function managerProfileStorageKey(managerId: string) {
  return `flypig.managerProfile.${managerId}`
}

export function managerNameSuffix(managerType: string) {
  if (managerType === 'Airline') return '航空公司'
  if (managerType === 'Hotel') return '酒店'
  if (managerType === 'Train') return '火车票'
  if (managerType === 'Attraction') return '景点'
  if (managerType === 'SiteAdmin') return '网站管理员'
  return '管理者'
}

export function managerBusinessLabel(managerType: string) {
  if (managerType === 'Airline') return '航空公司'
  if (managerType === 'Hotel') return '酒店'
  if (managerType === 'Train') return '火车票'
  if (managerType === 'Attraction') return '景点'
  if (managerType === 'SiteAdmin') return '网站管理员'
  return '业务'
}

export function stripManagerSuffix(displayName: string, managerType: string) {
  const suffix = managerNameSuffix(managerType)
  return displayName
    .replace(new RegExp(`${suffix}$`), '')
    .replace(/航空运营$/, '')
    .replace(/酒店管理者/, '')
    .replace(/火车票管理者/, '')
    .replace(/景点管理者/, '')
    .replace(/网站管理员/, '')
    .trim()
}

export function readManagerProfileOverride(managerId: string): ManagerProfileOverride {
  try {
    return JSON.parse(window.localStorage.getItem(managerProfileStorageKey(managerId)) ?? '{}') as ManagerProfileOverride
  } catch {
    return {}
  }
}

export function writeManagerProfileOverride(managerId: string, override: ManagerProfileOverride) {
  window.localStorage.setItem(managerProfileStorageKey(managerId), JSON.stringify(override))
}
