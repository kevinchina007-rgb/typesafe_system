// 本文件定义 advertising 模块的 `AdvertisementImageCandidateResponse`，用于图片候选项响应并提供 JSON 编解码。

export type AdvertisementImageCandidateResponse = {
  assetId: string
  publicUrl: string
  prompt: string
  mimeType: string
  seed: number
}

export const advertisementImageCandidateResponseFromJson = (json: string): AdvertisementImageCandidateResponse =>
  JSON.parse(json) as AdvertisementImageCandidateResponse

export const advertisementImageCandidateResponseToJson = (value: AdvertisementImageCandidateResponse): string =>
  JSON.stringify(value)
