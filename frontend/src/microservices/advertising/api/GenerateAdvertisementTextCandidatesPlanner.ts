// 本文件定义 GenerateAdvertisementTextCandidatesPlanner，负责 advertising 模块的生成编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

import type { GenerateAdvertisementTextCandidatesRequest } from '@/microservices/advertising/objects/GenerateAdvertisementTextCandidatesRequest'
import type { GenerateAdvertisementTextCandidatesResponse } from '@/microservices/advertising/objects/GenerateAdvertisementTextCandidatesResponse'

export async function generateAdvertisementTextCandidates(payload: GenerateAdvertisementTextCandidatesRequest): Promise<GenerateAdvertisementTextCandidatesResponse> {
  return executeJsonApiRequest('/GenerateAdvertisementTextCandidatesPlanner', 'POST', payload)
}
