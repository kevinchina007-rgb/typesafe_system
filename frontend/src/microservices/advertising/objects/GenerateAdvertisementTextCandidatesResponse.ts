// 本文件定义 advertising 模块的 `GenerateAdvertisementTextCandidatesResponse`，作为响应数据并提供 JSON 编解码。

export type AdvertisementTextCandidateResponse = {
  text: string
  emphasis: string
  seed: number
}

export type GenerateAdvertisementTextCandidatesResponse = {
  candidates: AdvertisementTextCandidateResponse[]
}

export const generateAdvertisementTextCandidatesResponseFromJson = (json: string): GenerateAdvertisementTextCandidatesResponse =>
  JSON.parse(json) as GenerateAdvertisementTextCandidatesResponse

export const generateAdvertisementTextCandidatesResponseToJson = (value: GenerateAdvertisementTextCandidatesResponse): string =>
  JSON.stringify(value)
