import type {
  AdvertisementImageUploadResponse,
  AdvertisementListResponse,
  AdvertisementResponse,
  AdvertisementReviewDecisionRequest,
  CreateAdvertisementRequest,
  UpdateAdvertisementRequest,
} from '../api-dtos/advertising'
import { createQueryString, createSingleFileFormData, executeApiRequest, executeJsonApiRequest, executeMultipartApiRequest } from '../api-transport'

export const advertisingApiClient = {
  createAdvertisement: (payload: CreateAdvertisementRequest): Promise<AdvertisementResponse> =>
    executeJsonApiRequest('/advertisements', 'POST', payload),

  uploadAdvertisementImage: (imageFile: File): Promise<AdvertisementImageUploadResponse> =>
    executeMultipartApiRequest('/advertisements/images', 'POST', createSingleFileFormData('image', imageFile)),

  listMyAdvertisements: (): Promise<AdvertisementListResponse> =>
    executeApiRequest('/advertisements/mine'),

  updateAdvertisement: (advertisementId: string, payload: UpdateAdvertisementRequest): Promise<AdvertisementResponse> =>
    executeJsonApiRequest(`/advertisements/${advertisementId}`, 'PUT', payload),

  submitAdvertisementForReview: (advertisementId: string): Promise<AdvertisementResponse> =>
    executeJsonApiRequest(`/advertisements/${advertisementId}/submit-review`, 'POST'),

  pauseAdvertisement: (advertisementId: string): Promise<AdvertisementResponse> =>
    executeJsonApiRequest(`/advertisements/${advertisementId}/pause`, 'POST'),

  listPendingAdvertisements: (): Promise<AdvertisementListResponse> =>
    executeApiRequest('/advertisements/review/pending'),

  listReviewedAdvertisements: (): Promise<AdvertisementListResponse> =>
    executeApiRequest('/advertisements/review/history'),

  approveAdvertisement: (advertisementId: string, payload: AdvertisementReviewDecisionRequest): Promise<AdvertisementResponse> =>
    executeJsonApiRequest(`/advertisements/${advertisementId}/approve`, 'POST', payload),

  rejectAdvertisement: (advertisementId: string, payload: AdvertisementReviewDecisionRequest): Promise<AdvertisementResponse> =>
    executeJsonApiRequest(`/advertisements/${advertisementId}/reject`, 'POST', payload),

  listDeliverableAdvertisements: (placement: string): Promise<AdvertisementListResponse> =>
    executeApiRequest(`/advertisements/delivery${createQueryString({ placement })}`),
}
