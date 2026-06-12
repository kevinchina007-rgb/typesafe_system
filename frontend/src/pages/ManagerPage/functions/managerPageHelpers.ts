import type { AppViewKey } from '@/lib/mvp-types/index'
import type { LoginManagerType } from '@/pages/ManagerPage/objects'

// 管理员页面统一使用的基础样式类名，避免各子页面自己散写。
export const managerPageShellClassName = 'grid gap-6 px-6 py-8 text-slate-950'
export const managerHeaderClassName = 'grid gap-2'
export const managerEyebrowClassName = 'text-sm font-bold text-slate-500'
export const managerTitleClassName = 'm-0 text-3xl font-bold leading-tight text-slate-950'
export const managerCopyClassName = 'm-0 max-w-3xl text-base leading-7 text-slate-600'
export const managerEntryGridClassName = 'grid gap-10 md:grid-cols-2 xl:grid-cols-4'
export const managerAuthCardClassName = 'mx-auto grid w-full gap-5 self-start border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/70 md:w-1/2 md:max-w-3xl'
export const managerActionsClassName = 'flex flex-wrap items-center gap-3'
export const managerFormGridClassName = 'grid gap-4'
export const managerFormTitleClassName = 'm-0 text-xl font-bold text-slate-950'
export const managerLabelClassName = 'grid gap-2 text-sm font-medium text-slate-700'
export const managerInputClassName =
  'min-h-11 border border-slate-300 bg-white px-3 py-2 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-black focus:ring-2 focus:ring-slate-200'
export const managerSecondaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55'
export const managerAuthPrimaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55'

export const managerBusinessEntries: Array<{
  managerType: Exclude<LoginManagerType, 'siteAdmin'>
  titleKey: string
  shortTitle: string
  accentClassName: string
  imageSrc: string
}> = [
  // 航司入口卡片。
  {
    managerType: 'airline',
    titleKey: 'manager.type.airline',
    shortTitle: 'AIR',
    accentClassName: 'bg-sky-500',
    imageSrc: '/images/manager-entry/airline.jpg',
  },
  // 酒店入口卡片。
  {
    managerType: 'hotel',
    titleKey: 'manager.type.hotel',
    shortTitle: 'HOTEL',
    accentClassName: 'bg-cyan-500',
    imageSrc: '/images/home-hero-candidates/01_大海_葡萄牙Praia da Marinha_海与岩壁在这里相爱.jpg',
  },
  // 火车入口卡片。
  {
    managerType: 'train',
    titleKey: 'manager.type.train',
    shortTitle: 'TRAIN',
    accentClassName: 'bg-indigo-500',
    imageSrc: '/images/manager-entry/train.jpg',
  },
  // 景点入口卡片。
  {
    managerType: 'attraction',
    titleKey: 'manager.type.attraction',
    shortTitle: 'VIEW',
    accentClassName: 'bg-emerald-500',
    imageSrc: '/images/home-hero-candidates/04_大山_瑞士Oeschinensee_湖光把山色轻轻收藏.jpg',
  },
].map((entry): {
  managerType: Exclude<LoginManagerType, 'siteAdmin'>
  titleKey: string
  shortTitle: string
  accentClassName: string
  imageSrc: string
} => {
  if (entry.managerType === 'hotel') {
    return {
      ...entry,
      managerType: entry.managerType as Exclude<LoginManagerType, 'siteAdmin'>,
      imageSrc: '/images/home-hero-candidates/09_白昼都市_日本东京_在白昼的楼宇间重新出发.jpg',
    }
  }

  if (entry.managerType === 'attraction') {
    return {
      ...entry,
      managerType: entry.managerType as Exclude<LoginManagerType, 'siteAdmin'>,
      imageSrc: '/images/home-hero-candidates/04_大山_瑞士Oeschinensee_湖光把山色轻轻收藏.jpg',
    }
  }

  return {
    ...entry,
    managerType: entry.managerType as Exclude<LoginManagerType, 'siteAdmin'>,
  }
})

// 根据管理员类型返回页面标题。
export function getManagerEntryTitle(managerType: LoginManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'airline') return translate('manager.type.airline')
  if (managerType === 'hotel') return translate('manager.type.hotel')
  if (managerType === 'train') return translate('manager.type.train')
  if (managerType === 'siteAdmin') return translate('manager.type.siteAdmin')
  return translate('manager.type.attraction')
}

// 根据管理员类型返回注册页标题。
export function getManagerRegisterTitle(managerType: LoginManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'airline') return translate('manager.registerAirline')
  if (managerType === 'hotel') return translate('manager.registerHotel')
  if (managerType === 'train') return translate('trainAdmin.registerManager')
  if (managerType === 'siteAdmin') return translate('manager.siteAdmin.register')
  return translate('attractionAdmin.registerTitle')
}

// 根据管理员类型返回登录页标题。
export function getManagerLoginTitle(managerType: LoginManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'airline') return translate('manager.loginAirline')
  if (managerType === 'hotel') return translate('manager.loginHotel')
  if (managerType === 'train') return translate('trainAdmin.login')
  if (managerType === 'siteAdmin') return translate('manager.siteAdmin.login')
  return translate('attractionAdmin.loginTitle')
}

// 根据管理员类型返回注册表单字段。
export function getManagerRegisterFields(managerType: LoginManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'airline') {
    return [
      { label: translate('manager.displayName'), name: 'displayName' },
      { label: translate('manager.email'), name: 'email', type: 'email' },
      { label: translate('manager.airlineName'), name: 'airlineName' },
      { label: translate('manager.airlineCode'), name: 'airlineCode' },
    ]
  }

  if (managerType === 'hotel') {
    return [
      { label: translate('manager.displayName'), name: 'displayName' },
      { label: translate('manager.email'), name: 'email', type: 'email' },
      { label: translate('manager.hotelName'), name: 'hotelName' },
      { label: translate('manager.hotelLocation'), name: 'location' },
    ]
  }

  if (managerType === 'train') {
    return [
      { label: translate('trainAdmin.operatorCode'), name: 'operatorCode' },
      { label: translate('trainAdmin.displayName'), name: 'displayName' },
      { label: translate('trainAdmin.email'), name: 'email', type: 'email' },
    ]
  }

  if (managerType === 'siteAdmin') {
    return [
      { label: translate('manager.displayName'), name: 'displayName' },
      { label: translate('manager.email'), name: 'email', type: 'email' },
    ]
  }

  return [
    { label: translate('attractionAdmin.displayName'), name: 'displayName' },
    { label: translate('attractionAdmin.email'), name: 'email', type: 'email' },
  ]
}

// 把当前视图映射成管理后台实际要展示的模块页签。
export function toActiveSection(currentViewKey: AppViewKey) {
  if (currentViewKey === 'managerFeedback') return 'feedback'
  if (currentViewKey === 'managerAdvertising') return 'advertising'
  if (currentViewKey === 'siteAdminBlogAudit') return 'blogAudit'
  if (
    currentViewKey === 'siteAdminAdvertisingReview' ||
    currentViewKey === 'siteAdminHotelAdvertisingReview' ||
    currentViewKey === 'siteAdminTrainAdvertisingReview' ||
    currentViewKey === 'siteAdminAttractionAdvertisingReview'
  ) return 'advertisingReview'
  if (currentViewKey === 'siteAdminFeedback') return 'siteAdminFeedback'
  return 'workspace'
}

// 根据当前视图，决定航空管理后台的初始子页。
export function toInitialAirlineSection(currentViewKey: AppViewKey) {
  if (currentViewKey === 'managerCreateFlight') return 'createFlight' as const
  if (currentViewKey === 'managerFeedback') return 'userFeedback' as const
  if (currentViewKey === 'managerProfile') return 'managerProfile' as const
  return 'flightManagement' as const
}
