import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

import type { GenerateAdvertisementTextCandidatesRequest } from '@/microservices/advertising/objects/GenerateAdvertisementTextCandidatesRequest'
import type { GenerateAdvertisementTextCandidatesResponse } from '@/microservices/advertising/objects/GenerateAdvertisementTextCandidatesResponse'

export async function generateAdvertisementTextCandidates(payload: GenerateAdvertisementTextCandidatesRequest): Promise<GenerateAdvertisementTextCandidatesResponse> {
  return executeJsonApiRequest('/GenerateAdvertisementTextCandidatesPlanner', 'POST', payload)
}
