// 本文件定义 advertising 模块的 `GenerateAdvertisementImageCandidatesResponse`，作为图片候选响应并提供 JSON 编解码。

import type { AdvertisementImageCandidateResponse } from './AdvertisementImageCandidateResponse'

export type GenerateAdvertisementImageCandidatesResponse = {
  candidates: AdvertisementImageCandidateResponse[]
}

export const generateAdvertisementImageCandidatesResponseFromJson = (json: string): GenerateAdvertisementImageCandidatesResponse =>
  JSON.parse(json) as GenerateAdvertisementImageCandidatesResponse

export const generateAdvertisementImageCandidatesResponseToJson = (value: GenerateAdvertisementImageCandidatesResponse): string =>
  JSON.stringify(value)
