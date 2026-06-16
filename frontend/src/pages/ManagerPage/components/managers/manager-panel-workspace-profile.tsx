import { useEffect, useState } from 'react'

import { formatIsoDateTime, localizeBedType, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import type { ManagerFlightPlannerResponse } from '@/lib/mvp-types/manager'
import type { ManagerFlightOrderResponse } from '@/lib/mvp-types/manager'
import type { ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'
import { getFlightDetailsPlannerAirlineDisplayNameByCode, getFlightDetailsPlannerAirlineLogoPathByCode } from '@/app/stores/models/flights/flightAirlineCatalog'

export type AirlineProfileDraft = {
  displayName: string
  companyName: string
  airlineCode: string
  logoPath: string
}

// 航空管理资料区块，负责展示和编辑当前航司的基础资料。
// 航空管理资料区块，负责展示和编辑当前航司资料。
export function ManagerProfileSection({
  managerSession,
  profileDraft,
  profileSavedAt,
  currentLanguage,
  onProfileChange,
  onSaveProfile,
  onLogout,
}: {
  managerSession: NonNullable<ManagerPanelProps['managerSession']>
  profileDraft: AirlineProfileDraft
  profileSavedAt: string | null
  currentLanguage: ManagerPanelProps['currentLanguage']
  onProfileChange: (draft: AirlineProfileDraft) => void
  onSaveProfile: () => Promise<void>
  onLogout: () => void
}) {
  return (
    <section className="grid gap-6 bg-slate-100 px-6 py-8">
      <div className="grid gap-6 bg-white p-7 md:grid-cols-[220px_minmax(0,1fr)]">
        <div className="grid content-start justify-items-center gap-4">
          <div className="flex h-32 w-32 items-center justify-center border-2 border-slate-200 bg-slate-950 text-xl font-bold text-white">
            {profileDraft.logoPath ? <img src={profileDraft.logoPath} alt="" className="h-full w-full object-cover" /> : profileDraft.airlineCode || 'LOGO'}
          </div>
          <strong className="text-2xl font-bold text-slate-950">{profileDraft.companyName || '航空公司'}</strong>
          <span className="text-sm font-medium text-slate-500">{mapBackendStatusToProductLabel(managerSession.status, currentLanguage)}</span>
        </div>

        <form
          className="grid gap-5"
          onSubmit={event => {
            event.preventDefault()
            void onSaveProfile()
          }}
        >
          <div>
            <p className="text-sm font-bold text-slate-500">管理者信息</p>
            <h3 className="m-0 text-3xl font-bold text-slate-950">资料设置</h3>
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <label>管理者昵称<input value={profileDraft.displayName} onChange={event => onProfileChange({ ...profileDraft, displayName: event.target.value })} /></label>
            <label>登录邮箱<input value={managerSession.email} readOnly /></label>
            <label>航空公司名称<input value={profileDraft.companyName} onChange={event => onProfileChange({ ...profileDraft, companyName: event.target.value })} /></label>
            <label>航空公司代码<input value={profileDraft.airlineCode} onChange={event => onProfileChange({ ...profileDraft, airlineCode: event.target.value })} /></label>
            <label className="md:col-span-2">头像 / Logo 地址<input value={profileDraft.logoPath} onChange={event => onProfileChange({ ...profileDraft, logoPath: event.target.value })} /></label>
          </div>
          <div className="flex flex-wrap items-center gap-4">
            <button type="submit" className="inline-flex min-h-12 items-center justify-center bg-pink-500 px-8 py-3 text-base font-bold text-white transition hover:bg-pink-600">
              保存资料
            </button>
            <button
              type="button"
              className="inline-flex min-h-12 items-center justify-center border border-slate-300 bg-white px-8 py-3 text-base font-bold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
              onClick={onLogout}
            >
              退出登录
            </button>
            {profileSavedAt ? <span className="text-sm font-medium text-slate-500">{`已保存 ${formatIsoDateTime(profileSavedAt, '-')}`}</span> : null}
          </div>
        </form>
      </div>
    </section>
  )
}

// 酒店工作区，负责酒店资料和房型创建入口。
// 酒店工作区，负责酒店资料和房型创建入口。
export function HotelWorkspace({
  currentLanguage,
  isBusy,
  managerSession,
  managedHotels,
  translate,
  onCreateManagerRoomType,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  isBusy: boolean
  managerSession: NonNullable<ManagerPanelProps['managerSession']>
  managedHotels: ManagerPanelProps['managedHotels']
  translate: (translationKey: string) => string
  onCreateManagerRoomType: ManagerPanelProps['onCreateManagerRoomType']
}) {
  return (
    <section className="grid gap-5">
      <form
        className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          const roomImageEntry = formData.get('roomImageFile')
          await onCreateManagerRoomType({
            managerId: managerSession.managerId,
            roomTypeName: String(formData.get('roomTypeName') ?? ''),
            capacity: Number(formData.get('capacity') ?? 2),
            bedType: String(formData.get('bedType') ?? 'queen'),
            nightlyPrice: String(formData.get('nightlyPrice') ?? '699'),
            currency: String(formData.get('currency') ?? 'CNY'),
            availableRooms: Number(formData.get('availableRooms') ?? 5),
            inventoryStartDate: String(formData.get('inventoryStartDate') ?? ''),
            inventoryEndDate: String(formData.get('inventoryEndDate') ?? ''),
            roomImageFile: roomImageEntry instanceof File && roomImageEntry.size > 0 ? roomImageEntry : null,
          })
          event.currentTarget.reset()
        }}
      >
        <h3 className="m-0 text-2xl font-bold text-slate-950">{translate('manager.createRoomType')}</h3>
        <div className="grid gap-4 md:grid-cols-3">
          <label>{translate('manager.roomTypeName')}<input name="roomTypeName" required /></label>
          <label>{translate('manager.capacity')}<input name="capacity" type="number" min={1} defaultValue={2} required /></label>
          <label>{translate('manager.nightlyPrice')}<input name="nightlyPrice" type="number" min={1} defaultValue={699} required /></label>
          <label>{translate('manager.availableRooms')}<input name="availableRooms" type="number" min={1} defaultValue={5} required /></label>
          <label>{translate('manager.inventoryStartDate')}<input name="inventoryStartDate" type="date" defaultValue="2026-04-01" required /></label>
          <label>{translate('manager.inventoryEndDate')}<input name="inventoryEndDate" type="date" defaultValue="2026-04-30" required /></label>
          <label className="md:col-span-3 grid gap-2">
            <span>{translate('manager.roomImage')}</span>
            <input name="roomImageFile" type="file" accept="image/png,image/jpeg,image/jpg,image/webp" />
            <span className="text-sm font-medium text-slate-500">可选，上传后会显示在房型卡片中间。</span>
          </label>
        </div>
        <button className="inline-flex min-h-11 w-fit items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" type="submit" disabled={isBusy}>
          {translate('manager.createRoomType')}
        </button>
      </form>

      <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
        <h3 className="m-0 text-2xl font-bold text-slate-950">{translate('manager.hotelName')}</h3>
        {managedHotels.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('manager.empty')}</p>
        ) : (
          <ul className="grid gap-3">
            {managedHotels.map(hotel => (
              <li key={hotel.hotelId} className="grid gap-2 border border-slate-200 bg-slate-50 p-4">
                <strong className="text-xl font-black text-slate-950">{hotel.hotelName}</strong>
                <p className="m-0 text-sm font-medium text-slate-600">{`${translate('manager.hotelLocation')}: ${hotel.location}`}</p>
                <div className="flex flex-wrap gap-2">
                  {(hotel.roomTypes ?? []).map(roomType => (
                    <span key={roomType.roomTypeId} className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
                      {`${roomType.roomTypeName} · ${localizeBedType(roomType.bedType, currentLanguage)} · ${roomType.basePrice} ${roomType.currency}`}
                    </span>
                  ))}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}

// 酒店资料区块，负责编辑酒店基础资料。
export function HotelProfileSection({
  currentLanguage,
  managerSession,
  managedHotels,
  translate,
  onUpdateHotelManagerProfile,
  onLogoutManager,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  managerSession: NonNullable<ManagerPanelProps['managerSession']>
  managedHotels: ManagerPanelProps['managedHotels']
  translate: (translationKey: string) => string
  onUpdateHotelManagerProfile: ManagerPanelProps['onUpdateHotelManagerProfile']
  onLogoutManager: ManagerPanelProps['onLogoutManager']
}) {
  const currentHotel = managedHotels.find(hotel => hotel.hotelId === managerSession.scopeId) ?? managedHotels[0] ?? null
  const [profileDraft, setProfileDraft] = useState(() => buildHotelProfileDraft(managerSession, currentHotel))

  useEffect(() => {
    setProfileDraft(buildHotelProfileDraft(managerSession, currentHotel))
  }, [currentHotel?.hotelId, currentHotel?.hotelName, currentHotel?.location, managerSession.displayName, managerSession.email, managerSession.managerId])

  return (
    <section className="grid gap-6 bg-slate-100 px-6 py-8">
      <div className="grid gap-6 bg-white p-7 md:grid-cols-[220px_minmax(0,1fr)]">
        <div className="grid content-start justify-items-center gap-4">
          <div className="flex h-32 w-32 items-center justify-center border-2 border-slate-200 bg-slate-950 text-xl font-bold text-white">
            {currentHotel?.hotelName?.slice(0, 1) ?? '酒'}
          </div>
          <strong className="text-2xl font-bold text-slate-950">{currentHotel?.hotelName ?? '酒店'}</strong>
          <span className="text-sm font-medium text-slate-500">{mapBackendStatusToProductLabel(managerSession.status, currentLanguage)}</span>
        </div>

        <form
          className="grid gap-5"
          onSubmit={event => {
            event.preventDefault()
            void onUpdateHotelManagerProfile({
              managerId: managerSession.managerId,
              displayName: profileDraft.displayName.trim(),
              email: profileDraft.email.trim(),
              hotelName: profileDraft.hotelName.trim(),
              hotelLocation: profileDraft.hotelLocation.trim(),
            })
          }}
        >
          <div>
            <p className="text-sm font-bold text-slate-500">管理者信息</p>
            <h3 className="m-0 text-3xl font-bold text-slate-950">资料设置</h3>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <label className="grid gap-2 text-lg font-medium text-slate-950">
              {translate('manager.displayName')}
              <input
                className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none"
                value={profileDraft.displayName}
                onChange={event => setProfileDraft({ ...profileDraft, displayName: event.target.value })}
              />
            </label>
            <label className="grid gap-2 text-lg font-medium text-slate-950">
              {translate('manager.email')}
              <input
                className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none"
                value={profileDraft.email}
                onChange={event => setProfileDraft({ ...profileDraft, email: event.target.value })}
              />
            </label>
            <label className="grid gap-2 text-lg font-medium text-slate-950">
              {translate('manager.hotelName')}
              <input
                className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none"
                value={profileDraft.hotelName}
                onChange={event => setProfileDraft({ ...profileDraft, hotelName: event.target.value })}
              />
            </label>
            <label className="grid gap-2 text-lg font-medium text-slate-950">
              {translate('manager.hotelLocation')}
              <input
                className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none"
                value={profileDraft.hotelLocation}
                onChange={event => setProfileDraft({ ...profileDraft, hotelLocation: event.target.value })}
              />
            </label>
            <label className="grid gap-2 text-lg font-medium text-slate-950 md:col-span-2">
              {translate('manager.scope')}
              <input className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none" value={currentHotel?.hotelId ?? managerSession.scopeId} readOnly />
            </label>
          </div>

          <div className="flex flex-wrap items-center gap-5 pt-2">
            <button type="submit" className="inline-flex min-h-12 items-center justify-center bg-pink-500 px-12 py-3 text-base font-bold text-white transition hover:bg-pink-600">
              保存资料
            </button>
            <button
              type="button"
              className="inline-flex min-h-12 items-center justify-center border border-slate-300 bg-white px-12 py-3 text-base font-bold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
              onClick={onLogoutManager}
            >
              退出登录
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}

// 酒店资料草稿，保存当前编辑中的酒店信息。
export type HotelProfileDraft = {
  displayName: string
  email: string
  hotelName: string
  hotelLocation: string
}

// 把酒店管理者和当前酒店信息整理成表单草稿。
// 把酒店会话和当前酒店信息整理成表单草稿。
export function buildHotelProfileDraft(
  managerSession: NonNullable<ManagerPanelProps['managerSession']>,
  currentHotel: ManagerPanelProps['managedHotels'][number] | null,
): HotelProfileDraft {
  return {
    displayName: managerSession.displayName,
    email: managerSession.email,
    hotelName: currentHotel?.hotelName ?? '',
    hotelLocation: currentHotel?.location ?? '',
  }
}

// 把航司管理者和已有航班信息整理成资料草稿。
// 把航司会话和现有航班数据整理成资料草稿。
export function buildProfileDraft(managerSession: ManagerPanelProps['managerSession'], managedFlights: ManagerFlightPlannerResponse[]): AirlineProfileDraft {
  const firstFlight = managedFlights[0]
  return {
    displayName: managerSession?.displayName ?? '',
    companyName: firstFlight ? getFlightDetailsPlannerAirlineDisplayNameByCode(firstFlight.airlineCode, firstFlight.airlineName) : '',
    airlineCode: firstFlight?.airlineCode ?? '',
    logoPath: firstFlight ? getFlightDetailsPlannerAirlineLogoPathByCode(firstFlight.airlineCode, firstFlight.airlineLogoPath) ?? '' : '',
  }
}

// 把航班时间格式化成页面可读的时分展示。
// 把航班时间格式化成页面展示的时分。
export function formatFlightClock(isoDateTime: string | null): string {
  if (!isoDateTime) {
    return '--:--'
  }

  return new Date(isoDateTime).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

// 统一不同来源的舱位名称写法，便于后续比较。
// 统一不同来源的舱位名称写法。
export function normalizeCabinKey(value: string): string {
  const normalized = value.trim().toUpperCase().replaceAll('-', '_')
  if (normalized === 'PREMIUMECONOMY' || normalized === 'PREMIUM_ECONOMY') {
    return 'PREMIUM_ECONOMY'
  }
  if (normalized === 'BUSINESS') {
    return 'BUSINESS'
  }
  if (normalized === 'FIRST') {
    return 'FIRST'
  }
  return 'ECONOMY'
}

// 把订单里的旅客名单拼成一行简短摘要。
// 把订单里的旅客列表拼成简短摘要。
export function formatManagerFlightTravelers(order: ManagerFlightOrderResponse): string {
  if (order.travelers.length > 0) {
    return order.travelers.map(traveler => `${traveler.fullName} (${traveler.documentNumber})`).join('、')
  }
  if (order.travelerIds.length > 0) {
    return order.travelerIds.join('、')
  }
  return '未选择出行人'
}

// 把旅客的座位和餐食偏好压缩成摘要文案。
// 把旅客座位和餐食偏好拼成摘要。
export function formatTravelerPreferenceSummary(traveler: ManagerFlightOrderResponse['travelers'][number]): string {
  const seatLabels: Record<string, string> = {
    Window: '靠窗',
    Aisle: '靠过道',
    Middle: '中间座',
    NoPreference: '无座位偏好',
    window: '靠窗',
    aisle: '靠过道',
    middle: '中间座',
    none: '无座位偏好',
  }
  const mealLabels: Record<string, string> = {
    Standard: '标准餐',
    Vegetarian: '素食餐',
    Vegan: '纯素餐',
    Halal: '清真餐',
    Kosher: '犹太餐',
    ChildMeal: '儿童餐',
    NoPreference: '无餐食偏好',
    standard: '标准餐',
    vegetarian: '素食餐',
    vegan: '纯素餐',
    halal: '清真餐',
  }
  return [
    seatLabels[traveler.preferenceInfo.seatPreference] ?? traveler.preferenceInfo.seatPreference,
    mealLabels[traveler.preferenceInfo.mealPreference] ?? traveler.preferenceInfo.mealPreference,
    traveler.preferenceInfo.quietSeatPreferred ? '安静座位' : '',
  ].filter(Boolean).join(' / ')
}

// 把旅客的特殊需求压缩成摘要文案。
// 把旅客特殊需求拼成摘要。
export function formatTravelerRequirementSummary(traveler: ManagerFlightOrderResponse['travelers'][number]): string {
  const requirement = traveler.specialRequirementInfo
  return [
    requirement.assistanceType && requirement.assistanceType !== 'none' ? requirement.assistanceType : '',
    requirement.requirementNote ?? '',
    requirement.hasLargeLuggage ? '大件行李' : '',
    requirement.luggageNote ?? '',
  ].filter(Boolean).join(' / ') || '无特殊要求'
}

// 根据订单状态返回对应的状态徽标样式。
// 根据订单状态返回对应的徽标样式。
export function statusBadgeClassName(status: string): string {
  if (status === 'Refunded') {
    return 'inline-flex min-h-10 items-center justify-center border border-sky-200 bg-sky-50 px-4 text-base font-black text-sky-700'
  }
  if (status === 'Confirmed' || status === 'Paid' || status === 'Booked') {
    return 'inline-flex min-h-10 items-center justify-center border border-emerald-200 bg-emerald-50 px-4 text-base font-black text-emerald-700'
  }
  return 'inline-flex min-h-10 items-center justify-center border border-amber-200 bg-amber-50 px-4 text-base font-black text-amber-700'
}

// 去掉重复值，避免下拉列表重复展示。
// 去掉重复值，避免下拉选项重复展示。
export function unique(values: string[]): string[] {
  return [...new Set(values.filter(Boolean))]
}
