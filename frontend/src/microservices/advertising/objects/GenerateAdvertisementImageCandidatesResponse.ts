// 本文件定义 advertising 模块的 `GenerateAdvertisementImageCandidatesResponse`，作为响应数据并提供 JSON 编解码。

export type AdvertisementImageCandidateResponse = {
  assetId: string
  publicUrl: string
  prompt: string
  mimeType: string
  seed: number
}

export type GenerateAdvertisementImageCandidatesResponse = {
  candidates: AdvertisementImageCandidateResponse[]
}

export const generateAdvertisementImageCandidatesResponseFromJson = (json: string): GenerateAdvertisementImageCandidatesResponse =>
  JSON.parse(json) as GenerateAdvertisementImageCandidatesResponse

export const generateAdvertisementImageCandidatesResponseToJson = (value: GenerateAdvertisementImageCandidatesResponse): string =>
  JSON.stringify(value)
