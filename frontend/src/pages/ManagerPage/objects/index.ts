// 管理后台页面的数据结构和 codec 统一从这里导出。
export { toLegacyManagerSession, toManagerTypeKey } from '../models/managerPageSession'
export type {
  AdvertisementResourceOption,
  BusinessManagerType,
  LoginManagerType,
  ManagerAuthCardProps,
  ManagerAuthMode,
  ManagerEntryCardProps,
  ManagerPageController,
  ManagerPageProps,
} from './ManagerPageModels'
