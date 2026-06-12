// 本文件定义登录用户出行人列表加载逻辑，供预订面板和选择弹窗复用。

import { useEffect, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { TravelerResponse, UserResponse } from '@/lib/mvp-types/index'

// 当前登录用户对应的出行人列表钩子。
export function useSignedInTravelers(signedInUser: UserResponse | null) {
  const [travelers, setTravelers] = useState<TravelerResponse[]>([])

  // 重新拉取当前登录用户的出行人列表。
  async function reloadTravelers() {
    if (!signedInUser) {
      setTravelers([])
      return
    }

    const travelerListResponse = await travelMvpApiClient.listTravelers(signedInUser.userId)
    setTravelers(travelerListResponse.travelers)
  }

  // 用户登录态变化时自动刷新出行人列表。
  useEffect(() => {
    void reloadTravelers()
  }, [signedInUser?.userId])

  return {
    travelers,
    reloadTravelers,
  }
}
