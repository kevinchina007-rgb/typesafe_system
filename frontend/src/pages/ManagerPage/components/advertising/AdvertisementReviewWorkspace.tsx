import { useEffect, useMemo, useState } from 'react'

import { useAdvertisingStore } from '@/app/stores/advertising-store'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

// 广告审核工作区，站点管理员在这里完成广告审核、排序和投放配置。
type AdvertisingBusinessModule = 'flight' | 'hotel' | 'train' | 'attraction'
type PlacementValue = 'FlightBookingPage' | 'HotelBookingPage' | 'TrainBookingPage' | 'AttractionBookingPage'

type AdvertisementReviewWorkspaceProps = {
  businessModule: AdvertisingBusinessModule
  translate: (translationKey: string) => string
}

const moduleConfig: Record<
  AdvertisingBusinessModule,
  {
    placement: PlacementValue
    ownerType: string
    targetResourceType: string
    title: string
  }
> = {
  flight: { placement: 'FlightBookingPage', ownerType: 'Airline', targetResourceType: 'Flight', title: '航班预订广告' },
  hotel: { placement: 'HotelBookingPage', ownerType: 'HotelManager', targetResourceType: 'Hotel', title: '酒店预订广告' },
  train: { placement: 'TrainBookingPage', ownerType: 'Train', targetResourceType: 'Train', title: '火车票预订广告' },
  attraction: { placement: 'AttractionBookingPage', ownerType: 'AttractionManager', targetResourceType: 'Attraction', title: '景点预订广告' },
}

const statusPriority: Record<string, number> = {
  PendingReview: 0,
  Approved: 1,
  Rejected: 2,
}

function advertisementBelongsToModule(
  advertisement: AdvertisementResponse,
  config: (typeof moduleConfig)[AdvertisingBusinessModule],
) {
  return (
    advertisement.placement === config.placement ||
    advertisement.targetResourceType === config.targetResourceType ||
    advertisement.ownerType === config.ownerType
  )
}

function mergeAdvertisements(...groups: AdvertisementResponse[][]) {
  const byId = new Map<string, AdvertisementResponse>()
  groups.flat().forEach(advertisement => byId.set(advertisement.advertisementId, advertisement))
  return [...byId.values()].sort((left, right) => {
    const statusDelta = (statusPriority[left.reviewStatus] ?? 9) - (statusPriority[right.reviewStatus] ?? 9)
    if (statusDelta !== 0) return statusDelta
    if (left.reviewStatus === 'Approved' && right.reviewStatus === 'Approved') {
      const slotDelta = (left.slotIndex ?? 999) - (right.slotIndex ?? 999)
      if (slotDelta !== 0) return slotDelta
    }
    return Date.parse(right.updatedAt) - Date.parse(left.updatedAt)
  })
}

function statusLabel(advertisement: AdvertisementResponse) {
  if (advertisement.reviewStatus === 'PendingReview') return '待审核'
  if (advertisement.reviewStatus === 'Approved') {
    return advertisement.deliveryStatus === 'Active' && advertisement.slotIndex ? '已通过，展示中' : '已通过，未展示'
  }
  if (advertisement.reviewStatus === 'Rejected') return '已驳回'
  return advertisement.reviewStatus
}

export function AdvertisementReviewWorkspace({ businessModule }: AdvertisementReviewWorkspaceProps) {
  const pendingReviewAdvertisements = useAdvertisingStore(state => state.pendingReviewAdvertisements)
  const reviewedAdvertisements = useAdvertisingStore(state => state.reviewedAdvertisements)
  const deliverySettingsByPlacement = useAdvertisingStore(state => state.deliverySettingsByPlacement)
  const loadPendingReviewAdvertisements = useAdvertisingStore(state => state.loadPendingReviewAdvertisements)
  const loadReviewedAdvertisements = useAdvertisingStore(state => state.loadReviewedAdvertisements)
  const loadAdvertisementDeliverySettings = useAdvertisingStore(state => state.loadAdvertisementDeliverySettings)
  const saveAdvertisementDeliverySettings = useAdvertisingStore(state => state.saveAdvertisementDeliverySettings)
  const approveAdvertisement = useAdvertisingStore(state => state.approveAdvertisement)
  const rejectAdvertisement = useAdvertisingStore(state => state.rejectAdvertisement)
  const assignAdvertisementSlot = useAdvertisingStore(state => state.assignAdvertisementSlot)
  const pauseAdvertisementDisplay = useAdvertisingStore(state => state.pauseAdvertisementDisplay)

  const config = moduleConfig[businessModule]
  const settings = deliverySettingsByPlacement[config.placement]
  const [rotationIntervalSeconds, setRotationIntervalSeconds] = useState(5)
  const [rejectingAdvertisementId, setRejectingAdvertisementId] = useState<string | null>(null)
  const [rejectReason, setRejectReason] = useState('')
  const [isSorting, setIsSorting] = useState(false)
  const [sortSelection, setSortSelection] = useState<string[]>([])
  const [busyAdvertisementId, setBusyAdvertisementId] = useState<string | null>(null)

  useEffect(() => {
    void Promise.all([
      loadPendingReviewAdvertisements(),
      loadReviewedAdvertisements(),
      loadAdvertisementDeliverySettings(config.placement),
    ])
  }, [config.placement, loadAdvertisementDeliverySettings, loadPendingReviewAdvertisements, loadReviewedAdvertisements])

  useEffect(() => {
    if (settings) setRotationIntervalSeconds(settings.rotationIntervalSeconds)
  }, [settings])

  const advertisements = useMemo(
    () =>
      mergeAdvertisements(pendingReviewAdvertisements, reviewedAdvertisements).filter(advertisement =>
        advertisementBelongsToModule(advertisement, config),
      ),
    [config, pendingReviewAdvertisements, reviewedAdvertisements],
  )

  async function reloadAdvertisements() {
    await Promise.all([loadPendingReviewAdvertisements(), loadReviewedAdvertisements()])
  }

  async function handleApprove(advertisement: AdvertisementResponse) {
    setBusyAdvertisementId(advertisement.advertisementId)
    try {
      await approveAdvertisement(advertisement.advertisementId, { reviewNote: null })
      await reloadAdvertisements()
    } finally {
      setBusyAdvertisementId(null)
    }
  }

  async function handleReject(advertisement: AdvertisementResponse) {
    const reason = rejectReason.trim()
    if (!reason) {
      window.alert('请先填写驳回原因。')
      return
    }
    setBusyAdvertisementId(advertisement.advertisementId)
    try {
      await rejectAdvertisement(advertisement.advertisementId, { reviewNote: reason })
      setRejectingAdvertisementId(null)
      setRejectReason('')
      await reloadAdvertisements()
    } finally {
      setBusyAdvertisementId(null)
    }
  }

  async function handlePauseDisplay(advertisement: AdvertisementResponse) {
    setBusyAdvertisementId(advertisement.advertisementId)
    try {
      await pauseAdvertisementDisplay(advertisement.advertisementId, '网站管理者取消展示')
      setSortSelection(current => current.filter(id => id !== advertisement.advertisementId))
      await reloadAdvertisements()
    } finally {
      setBusyAdvertisementId(null)
    }
  }

  function toggleSortSelection(advertisementId: string) {
    setSortSelection(current => {
      if (current.includes(advertisementId)) return current.filter(id => id !== advertisementId)
      if (current.length >= 4) return current
      return [...current, advertisementId]
    })
  }

  async function confirmSort() {
    setBusyAdvertisementId('sorting')
    try {
      for (const [index, advertisementId] of sortSelection.entries()) {
        await assignAdvertisementSlot(advertisementId, { slotIndex: index + 1 })
      }
      setIsSorting(false)
      setSortSelection([])
      await reloadAdvertisements()
    } finally {
      setBusyAdvertisementId(null)
    }
  }

  return (
    <section className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div>
          <p className="text-sm font-bold text-slate-500">网站管理者工作台</p>
          <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{config.title}</h2>
        </div>

        <div className="grid max-w-sm gap-2">
          <label className="text-sm font-bold text-slate-700" htmlFor="advertisement-rotation-interval">
            轮播间隔（秒）
          </label>
          <input
            id="advertisement-rotation-interval"
            className="min-h-12 border border-slate-300 px-3 text-base font-semibold text-slate-950"
            type="number"
            min={1}
            value={rotationIntervalSeconds}
            onChange={event => setRotationIntervalSeconds(Number(event.target.value))}
          />
        </div>

        <button
          type="button"
          className="inline-flex min-h-11 w-fit items-center justify-center border border-black bg-black px-5 py-2 text-sm font-semibold text-white hover:bg-white hover:text-black"
          onClick={() => {
            void saveAdvertisementDeliverySettings({
              placement: config.placement,
              rotationIntervalSeconds,
              playOrder: 'Manual',
              startAt: null,
              endAt: null,
            })
          }}
        >
          保存轮播设置
        </button>
      </section>

      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div>
          <p className="text-sm font-bold text-slate-500">广告陈列栏</p>
          <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">广告栏</h3>
        </div>
        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-950 hover:border-black hover:bg-black hover:text-white"
            onClick={() => {
              setIsSorting(value => !value)
              setSortSelection([])
            }}
          >
            {isSorting ? '退出排序' : '播放排序'}
          </button>
          {isSorting ? (
            <button
              type="button"
              className="inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-5 py-2 text-sm font-semibold text-white disabled:opacity-50"
              disabled={sortSelection.length === 0 || busyAdvertisementId === 'sorting'}
              onClick={() => void confirmSort()}
            >
              确认排序
            </button>
          ) : null}
        </div>

        {advertisements.length === 0 ? (
          <p className="m-0 text-sm leading-6 text-slate-500">当前没有广告。</p>
        ) : (
          <div className="grid gap-3">
            {advertisements.map(advertisement => {
              const isPending = advertisement.reviewStatus === 'PendingReview'
              const isApproved = advertisement.reviewStatus === 'Approved'
              const isRejected = advertisement.reviewStatus === 'Rejected'
              const isDisplaying = isApproved && advertisement.deliveryStatus === 'Active' && Boolean(advertisement.slotIndex)
              const sortIndex = sortSelection.indexOf(advertisement.advertisementId)
              const isBusy = busyAdvertisementId === advertisement.advertisementId

              return (
                <div key={advertisement.advertisementId} className={isSorting && isApproved ? 'grid grid-cols-[56px_1fr] gap-3' : ''}>
                  {isSorting && isApproved ? (
                    <button
                      type="button"
                      className="min-h-14 border border-pink-500 bg-white text-lg font-black text-pink-600"
                      onClick={() => toggleSortSelection(advertisement.advertisementId)}
                    >
                      {sortIndex >= 0 ? sortIndex + 1 : advertisement.slotIndex ?? '+'}
                    </button>
                  ) : null}

                  <article className="grid gap-4 border border-slate-200 bg-white p-4 md:grid-cols-[320px_1fr]">
                    <AdvertisementPreview advertisement={advertisement} />
                    <div className="grid content-start gap-4">
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div>
                          <p className="m-0 text-sm font-bold text-slate-500">航空公司名称</p>
                          <strong className="text-xl text-slate-950">{advertisement.ownerDisplayName}</strong>
                        </div>
                        <span
                          className={`inline-flex w-fit border px-3 py-1 text-sm font-bold ${
                            isRejected
                              ? 'border-rose-200 bg-rose-50 text-rose-700'
                              : isPending
                                ? 'border-amber-200 bg-amber-50 text-amber-700'
                                : isDisplaying
                                  ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
                                  : 'border-slate-200 bg-slate-50 text-slate-600'
                          }`}
                        >
                          {statusLabel(advertisement)}
                        </span>
                      </div>

                      {isRejected ? (
                        <p className="m-0 text-sm leading-6 text-rose-700">
                          驳回原因：{advertisement.rejectionNote || '未填写'}
                        </p>
                      ) : null}

                      {rejectingAdvertisementId === advertisement.advertisementId ? (
                        <label className="grid gap-2 text-sm font-bold text-slate-700">
                          驳回原因
                          <textarea
                            rows={3}
                            className="border border-slate-300 p-3 text-base font-medium text-slate-950"
                            value={rejectReason}
                            onChange={event => setRejectReason(event.target.value)}
                            autoFocus
                          />
                        </label>
                      ) : null}

                      <div className="flex flex-wrap gap-3">
                        {isPending ? (
                          <>
                            <button
                              type="button"
                              className="min-h-11 border border-black bg-black px-5 py-2 font-bold text-white hover:bg-white hover:text-black disabled:opacity-50"
                              disabled={isBusy}
                              onClick={() => void handleApprove(advertisement)}
                            >
                              通过
                            </button>
                            {rejectingAdvertisementId === advertisement.advertisementId ? (
                              <>
                                <button
                                  type="button"
                                  className="min-h-11 border border-pink-500 bg-pink-500 px-5 py-2 font-bold text-white hover:bg-white hover:text-pink-600 disabled:opacity-50"
                                  disabled={isBusy}
                                  onClick={() => void handleReject(advertisement)}
                                >
                                  确认驳回
                                </button>
                                <button
                                  type="button"
                                  className="min-h-11 border border-slate-300 bg-white px-5 py-2 font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white"
                                  onClick={() => {
                                    setRejectingAdvertisementId(null)
                                    setRejectReason('')
                                  }}
                                >
                                  取消
                                </button>
                              </>
                            ) : (
                              <button
                                type="button"
                                className="min-h-11 border border-slate-300 bg-white px-5 py-2 font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white"
                                onClick={() => {
                                  setRejectingAdvertisementId(advertisement.advertisementId)
                                  setRejectReason('')
                                }}
                              >
                                驳回
                              </button>
                            )}
                          </>
                        ) : null}

                        {isDisplaying ? (
                          <button
                            type="button"
                            className="min-h-11 border border-slate-300 bg-white px-5 py-2 font-bold text-slate-950 hover:border-rose-500 hover:bg-rose-500 hover:text-white disabled:opacity-50"
                            disabled={isBusy}
                            onClick={() => void handlePauseDisplay(advertisement)}
                          >
                            取消展示
                          </button>
                        ) : null}
                      </div>
                    </div>
                  </article>
                </div>
              )
            })}
          </div>
        )}
      </section>
    </section>
  )
}

function AdvertisementPreview({ advertisement }: { advertisement: AdvertisementResponse }) {
  if (advertisement.imageUrl) {
    return (
      <BackendAssetImage
        assetUrl={advertisement.imageUrl}
        alt="展示稿件"
        className="aspect-[4/1] w-full border border-slate-200 object-cover"
        fallbackContent="展示稿件"
      />
    )
  }

  return (
    <div className="flex aspect-[4/1] items-center justify-center border border-slate-200 bg-slate-100 text-sm font-bold text-slate-500">
      展示稿件
    </div>
  )
}
