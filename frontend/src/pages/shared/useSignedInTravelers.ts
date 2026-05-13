import { useEffect, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { TravelerResponse, UserResponse } from '@/lib/mvp-types/index'

export function useSignedInTravelers(signedInUser: UserResponse | null) {
  const [travelers, setTravelers] = useState<TravelerResponse[]>([])

  async function reloadTravelers() {
    if (!signedInUser) {
      setTravelers([])
      return
    }

    const travelerListResponse = await travelMvpApiClient.listTravelers(signedInUser.userId)
    setTravelers(travelerListResponse.travelers)
  }

  useEffect(() => {
    void reloadTravelers()
  }, [signedInUser?.userId])

  return {
    travelers,
    reloadTravelers,
  }
}
