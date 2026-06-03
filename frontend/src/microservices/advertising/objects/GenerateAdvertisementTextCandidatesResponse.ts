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
