import type { ContentImageResponse } from '@/microservices/content/objects/ContentImageResponse'
import type { ReviewResponse } from '@/microservices/content/objects/ReviewResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createReview = (payload: {
  userId: string
  orderId: string
  orderItemId: string
  rating: number
  title: string
  content: string
  images: ContentImageResponse[]
}): Promise<ReviewResponse> =>
  executeJsonApiRequest('/CreateReviewPlanner', 'POST', payload)

export const updateReview = (
  reviewId: string,
  payload: {
    userId: string
    rating: number
    title: string
    content: string
    images: ContentImageResponse[]
  },
): Promise<ReviewResponse> =>
  executeJsonApiRequest('/UpdateReviewPlanner', 'POST', { reviewId, ...payload })

export const uploadReviewImage = async (userId: string, imageFile: File): Promise<ContentImageResponse> => {
  const bytes = new Uint8Array(await imageFile.arrayBuffer())
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  return executeJsonApiRequest('/UploadReviewImagePlanner', 'POST', {
    userId,
    originalFileName: imageFile.name,
    contentType: imageFile.type || 'application/octet-stream',
    base64Content: window.btoa(binary),
  })
}

export const deleteReview = (reviewId: string, payload: { userId: string }): Promise<void> =>
  executeJsonApiRequest('/DeleteReviewPlanner', 'POST', { reviewId, userId: payload.userId })

