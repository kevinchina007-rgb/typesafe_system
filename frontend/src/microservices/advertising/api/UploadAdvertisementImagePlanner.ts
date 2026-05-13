import type { AdvertisementImageUploadResponse } from '@/microservices/advertising/objects/AdvertisementImageUploadResponse'






import { createSingleFileFormData, executeMultipartApiRequest } from '@/microservices/common/api/ApiTransport'

export const uploadAdvertisementImage = (imageFile: File): Promise<AdvertisementImageUploadResponse> =>
    executeMultipartApiRequest('/advertisements/images', 'POST', createSingleFileFormData('image', imageFile))
