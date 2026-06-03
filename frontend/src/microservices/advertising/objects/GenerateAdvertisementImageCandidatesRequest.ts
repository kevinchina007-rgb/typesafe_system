export type GenerateAdvertisementImageCandidatesRequest = {
  prompt: string
  supportingCopy?: string | null
  tone?: string | null
  resourceLabel?: string | null
  advertisementKind?: string | null
  imageFactoryKind?: string | null
  transparentBackground?: boolean | null
  width?: number | null
  height?: number | null
  candidateCount?: number | null
  avoidText?: string | null
}

export const generateAdvertisementImageCandidatesRequestFromJson = (json: string): GenerateAdvertisementImageCandidatesRequest =>
  JSON.parse(json) as GenerateAdvertisementImageCandidatesRequest

export const generateAdvertisementImageCandidatesRequestToJson = (value: GenerateAdvertisementImageCandidatesRequest): string =>
  JSON.stringify(value)
