// 本文件定义 advertising 模块的 `GenerateAdvertisementTextCandidatesRequest`，作为请求参数并提供 JSON 编解码。

export type GenerateAdvertisementTextCandidatesRequest = {
  prompt: string
  sourceText?: string | null
  styleRequirement?: string | null
  focus?: string | null
  tone?: string | null
  resourceLabel?: string | null
  advertisementKind?: string | null
  candidateCount?: number | null
  avoidText?: string | null
}

export const generateAdvertisementTextCandidatesRequestFromJson = (json: string): GenerateAdvertisementTextCandidatesRequest =>
  JSON.parse(json) as GenerateAdvertisementTextCandidatesRequest

export const generateAdvertisementTextCandidatesRequestToJson = (value: GenerateAdvertisementTextCandidatesRequest): string =>
  JSON.stringify(value)
