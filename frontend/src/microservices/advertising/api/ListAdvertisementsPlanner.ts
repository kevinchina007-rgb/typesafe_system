// 本文件定义 advertising 模块的 `ListAdvertisementsPlanner`，负责列表查询编排和接口入口。

import type { ListAdvertisementsRequest } from '@/microservices/advertising/objects/ListAdvertisementsRequest'
import type { ListAdvertisementsResponse } from '@/microservices/advertising/objects/ListAdvertisementsResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

const listAdvertisements = (payload: ListAdvertisementsRequest): Promise<ListAdvertisementsResponse> =>
  executeJsonApiRequest('/ListAdvertisementsPlanner', 'POST', payload)

export const listMyAdvertisements = (ownerManagerId: string, ownerType: string): Promise<ListAdvertisementsResponse> =>
  listAdvertisements({
    placement: undefined,
    reviewStatus: undefined,
    reviewStatuses: undefined,
    ownerManagerId,
    ownerType,
    deliverableOnly: undefined,
    currentTime: undefined,
  })

export const listPendingAdvertisements = (): Promise<ListAdvertisementsResponse> =>
  listAdvertisements({
    placement: undefined,
    reviewStatus: 'PendingReview',
    reviewStatuses: undefined,
    ownerManagerId: undefined,
    ownerType: undefined,
    deliverableOnly: undefined,
    currentTime: undefined,
  })

export const listReviewedAdvertisements = (): Promise<ListAdvertisementsResponse> =>
  listAdvertisements({
    placement: undefined,
    reviewStatus: undefined,
    reviewStatuses: ['Approved', 'Rejected'],
    ownerManagerId: undefined,
    ownerType: undefined,
    deliverableOnly: undefined,
    currentTime: undefined,
  })

export const listDeliverableAdvertisements = (placement: string): Promise<ListAdvertisementsResponse> =>
  listAdvertisements({
    placement,
    reviewStatus: 'Approved',
    reviewStatuses: undefined,
    ownerManagerId: undefined,
    ownerType: undefined,
    deliverableOnly: true,
    currentTime: undefined,
  })
