// 本文件定义 TravelersPage 页面数据模型和 JSON 编解码。

import type { AppLanguage, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { TravelerProfileInput } from '@/microservices/traveler/objects/TravelerProfileInput'

export type TravelersPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onSignedInUserChange: (user: UserResponse | null) => void
  onShowNotice: PageNoticeHandler
}

export type TravelerFormDraft = {
  travelerId: string | null
  fullName: string
  gender: string
  nationality: string
  documentType: string
  documentNumber: string
  documentExpiryDate: string
  phone: string
  email: string
  birthDate: string
  seatPreference: string
  mealPreference: string
  quietSeatPreferred: boolean
  assistanceType: string
  requirementNote: string
  hasLargeLuggage: boolean
  luggageNote: string
  emergencyContactName: string
  emergencyContactPhoneNumber: string
  isDefaultTraveler: boolean
}

export type TravelersPageController = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onCreateTraveler: (payload: TravelerProfileInput) => Promise<void>
  onUpdateTraveler: (payload: TravelerFormDraft) => Promise<void>
  onSetDefaultTraveler: (traveler: TravelerResponse) => Promise<void>
  onDeleteTraveler: (travelerId: string) => Promise<void>
  onReloadTravelers: () => Promise<void>
}

export type TravelerPanelProps = Pick<
  TravelersPageController,
  'currentLanguage' | 'isBusy' | 'isGuestMode' | 'travelers' | 'translate' | 'onCreateTraveler' | 'onUpdateTraveler' | 'onSetDefaultTraveler' | 'onDeleteTraveler' | 'onReloadTravelers'
>

export type TravelersPageRegionKey = 'header' | 'form' | 'list'

export type TravelersPageRegion = {
  key: TravelersPageRegionKey
  title: string
  description: string
}

export const TRAVELERS_PAGE_REGIONS: TravelersPageRegion[] = [
  {
    key: 'header',
    title: '页面标题区',
    description: '展示旅客页标题和说明。',
  },
  {
    key: 'form',
    title: '编辑表单区',
    description: '用于新增或编辑常用旅客。',
  },
  {
    key: 'list',
    title: '旅客列表区',
    description: '展示已有旅客并支持编辑与删除。',
  },
]

export const emptyTravelerFormDraft: TravelerFormDraft = {
  travelerId: null,
  fullName: '',
  gender: '未填',
  nationality: '中国',
  documentType: 'passport',
  documentNumber: '',
  documentExpiryDate: '',
  phone: '',
  email: '',
  birthDate: '',
  seatPreference: 'none',
  mealPreference: 'standard',
  quietSeatPreferred: false,
  assistanceType: '无',
  requirementNote: '',
  hasLargeLuggage: false,
  luggageNote: '',
  emergencyContactName: '',
  emergencyContactPhoneNumber: '',
  isDefaultTraveler: false,
}
