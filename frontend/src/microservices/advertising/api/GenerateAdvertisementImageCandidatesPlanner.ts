import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

import type { GenerateAdvertisementImageCandidatesRequest } from '@/microservices/advertising/objects/GenerateAdvertisementImageCandidatesRequest'
import type { GenerateAdvertisementImageCandidatesResponse } from '@/microservices/advertising/objects/GenerateAdvertisementImageCandidatesResponse'

export async function generateAdvertisementImageCandidates(payload: GenerateAdvertisementImageCandidatesRequest): Promise<GenerateAdvertisementImageCandidatesResponse> {
  return executeJsonApiRequest('/GenerateAdvertisementImageCandidatesPlanner', 'POST', payload)
}
