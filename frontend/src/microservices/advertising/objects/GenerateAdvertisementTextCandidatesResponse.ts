// 本文件定义 advertising 模块的 `GenerateAdvertisementTextCandidatesResponse`，作为文案候选响应并提供 JSON 编解码。

import type { AdvertisementTextCandidateResponse } from './AdvertisementTextCandidateResponse'

export type GenerateAdvertisementTextCandidatesResponse = {
  candidates: AdvertisementTextCandidateResponse[]
}

export const generateAdvertisementTextCandidatesResponseFromJson = (json: string): GenerateAdvertisementTextCandidatesResponse =>
  JSON.parse(json) as GenerateAdvertisementTextCandidatesResponse

export const generateAdvertisementTextCandidatesResponseToJson = (value: GenerateAdvertisementTextCandidatesResponse): string =>
  JSON.stringify(value)
