import { useEffect, useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type {
  AppLanguage,
  AppViewKey,
  TourGroupDetailsResponse,
  UserResponse,
} from '@/lib/mvp-types/index'
import { pickCreatedPlanItem } from '@/pages/TourGroupsPage/functions'
import { TourGroupPlanComposer } from '@/pages/TourGroupsPage/components/TourGroupPlanComposer'
import { usePageActions } from '@/pages/shared/usePageActions'

type TourGroupPlanBuilderPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: (kind: 'success' | 'error' | 'info', title: string, description: string, technicalMessage?: string) => void
}

function getGroupIdFromUrl(): string | null {
  if (typeof window === 'undefined') return null
  const groupId = new URLSearchParams(window.location.search).get('groupId')
  return groupId && groupId.trim().length > 0 ? groupId : null
}

export function TourGroupPlanBuilderPage({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: TourGroupPlanBuilderPageProps) {
  const { isBusy, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)
  const [groupId] = useState(() => getGroupIdFromUrl())
  const [details, setDetails] = useState<TourGroupDetailsResponse | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)

  useEffect(() => {
    if (!groupId) {
      setLoadError('missing_group_id')
      return
    }

    let disposed = false
    void (async () => {
      try {
        const nextDetails = await travelMvpApiClient.getTourGroup(groupId)
        if (disposed) return
        setDetails(nextDetails)
        setLoadError(null)
      } catch (error) {
        if (disposed) return
        setLoadError(error instanceof Error ? error.message : String(error))
      }
    })()

    return () => {
      disposed = true
    }
  }, [groupId])

  const isOrganizer = useMemo(
    () => signedInUser !== null && details?.group.organizerUserId === signedInUser.userId,
    [details?.group.organizerUserId, signedInUser],
  )

  function goBackToTourGroup() {
    onNavigate('tourGroups')
  }

  const backButton = (
    <button
      type="button"
      className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
      onClick={goBackToTourGroup}
    >
      回到旅游团
    </button>
  )

  async function onCreatePlanItem(payload: {
    itemType: string
    title: string
    description: string
    scheduledAt: string
    endsAt?: string | null
    sequenceNo: number
  }) {
    if (!details || !signedInUser || !groupId) {
      return null
    }
    const previousIds = new Set(details.planItems.map(planItem => planItem.planItemId))
    const nextDetails = await runPageActionWithResult(
      () => travelMvpApiClient.createTourGroupPlanItem(groupId, { organizerUserId: signedInUser.userId, ...payload }),
      translate('tourGroups.createPlanItem'),
      translate('notice.actionSuccess'),
    )
    setDetails(nextDetails)
    return pickCreatedPlanItem(nextDetails, previousIds)
  }

  async function onCreateOptionForPlanItem(
    planItemId: string,
    payload: {
      resourceType: string
      resourceId: string
      resourceVariantCode?: string | null
      resourceContext?: string | null
      label: string
      description: string
      defaultQuantity: number
    },
  ) {
    if (!details || !signedInUser || !groupId) {
      return
    }
    const nextDetails = await runPageActionWithResult(
      () => travelMvpApiClient.createTourGroupPlanOption(planItemId, groupId, { organizerUserId: signedInUser.userId, ...payload }),
      translate('tourGroups.createOption'),
      translate('notice.actionSuccess'),
    )
    setDetails(nextDetails)
  }

  const searchFlights = async (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => {
    const response = await travelMvpApiClient.searchFlightsPlanner(payload)
    return response.flights
  }

  const searchHotels = async (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => {
    const response = await travelMvpApiClient.searchHotelsPlanner(payload)
    return response.hotels
  }

  const searchTrains = async (payload: { fromStation?: string; toStation?: string; date?: string }) => {
    const response = await travelMvpApiClient.listTrains(payload)
    return response.trains
  }

  const searchAttractions = async (payload: { city?: string }) => {
    const response = await travelMvpApiClient.listAttractions(payload)
    const detailedAttractions = await Promise.all(
      response.attractions.map(async attractionSummary => {
        try {
          return await travelMvpApiClient.getAttraction(attractionSummary.attractionId)
        } catch {
          return attractionSummary
        }
      }),
    )
    return detailedAttractions
  }

  if (!groupId) {
    return (
      <section className="grid gap-4 px-6 pb-8 pt-6">
        <div className="grid gap-3 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/50">
          <h1 className="text-2xl font-bold">{translate('nav.tourGroupPlanBuilder')}</h1>
          <p className="text-sm leading-6 text-slate-500">未找到旅游团编号，请从旅游团详情页进入。</p>
          {backButton}
        </div>
      </section>
    )
  }

  if (loadError) {
    return (
      <section className="grid gap-4 px-6 pb-8 pt-6">
        <div className="grid gap-3 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/50">
          <h1 className="text-2xl font-bold">{translate('nav.tourGroupPlanBuilder')}</h1>
          <p className="text-sm leading-6 text-slate-500">行程项页面加载失败，请返回旅游团详情重新进入。</p>
          {backButton}
        </div>
      </section>
    )
  }

  if (!details) {
    return (
      <section className="grid gap-4 px-6 pb-8 pt-6">
        <div className="grid gap-3 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/50">
          <h1 className="text-2xl font-bold">{translate('nav.tourGroupPlanBuilder')}</h1>
          <p className="text-sm leading-6 text-slate-500">正在加载旅游团信息...</p>
          {backButton}
        </div>
      </section>
    )
  }

  if (!isOrganizer) {
    return (
      <section className="grid gap-4 px-6 pb-8 pt-6">
        <div className="grid gap-3 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/50">
          <h1 className="text-2xl font-bold">{translate('nav.tourGroupPlanBuilder')}</h1>
          <p className="text-sm leading-6 text-slate-500">只有团长可以添加行程项。</p>
          {backButton}
        </div>
      </section>
    )
  }

  return (
    <section className="grid gap-4 px-6 pb-8 pt-6">
      <div className="grid gap-3 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/50">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="grid gap-1">
            <p className="text-sm font-bold text-slate-500">{translate('nav.tourGroupPlanBuilder')}</p>
            <h1 className="text-2xl font-bold tracking-tight text-slate-950">{details.group.title}</h1>
            <p className="text-sm leading-6 text-slate-500">
              {details.group.destination} / {details.group.startDate} - {details.group.endDate}
            </p>
          </div>
          {backButton}
        </div>
      </div>

      <TourGroupPlanComposer
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        existingPlanItems={details.planItems}
        translate={translate}
        onSearchFlights={searchFlights}
        onSearchHotels={searchHotels}
        onSearchTrains={searchTrains}
        onSearchAttractions={searchAttractions}
        onCreatePlanItem={onCreatePlanItem}
        onCreateOptionForPlanItem={onCreateOptionForPlanItem}
      />
    </section>
  )
}
