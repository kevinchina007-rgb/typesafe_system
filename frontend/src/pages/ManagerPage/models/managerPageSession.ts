import type { CurrentManagerSessionResponse, ManagerSessionResponse } from '@/lib/mvp-types/index'

// 把新的管理员会话结构转换成旧页面还在使用的会话结构。
export function toLegacyManagerSession(session: CurrentManagerSessionResponse): ManagerSessionResponse {
  return {
    managerId: session.managerId,
    managerType: session.managerType,
    email: session.email,
    displayName: session.displayName,
    status: session.status,
    scopeId: session.scopeId,
    logoAssetPath: session.logoAssetPath,
    createdAt: session.createdAt,
  }
}

// 把后端返回的管理员类型字符串统一映射成前端使用的小写 key。
export function toManagerTypeKey(managerType: string): 'airline' | 'hotel' | 'train' | 'attraction' | 'siteAdmin' {
  switch (managerType) {
    case 'Airline':
      return 'airline'
    case 'Hotel':
      return 'hotel'
    case 'Train':
      return 'train'
    case 'Attraction':
      return 'attraction'
    case 'SiteAdmin':
      return 'siteAdmin'
    default:
      return 'airline'
  }
}
