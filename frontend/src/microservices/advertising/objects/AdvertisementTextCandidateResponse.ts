// 本文件定义 advertising 模块的 `AdvertisementTextCandidateResponse`，用于文案候选项响应并提供 JSON 编解码。

export type AdvertisementTextCandidateResponse = {
  text: string
  emphasis: string
  seed: number
}

export const advertisementTextCandidateResponseFromJson = (json: string): AdvertisementTextCandidateResponse =>
  JSON.parse(json) as AdvertisementTextCandidateResponse

export const advertisementTextCandidateResponseToJson = (value: AdvertisementTextCandidateResponse): string =>
  JSON.stringify(value)
