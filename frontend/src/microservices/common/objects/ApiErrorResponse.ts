export type ApiErrorResponse = {
  code: string
  message: string
}
export const apiErrorResponseFromJson = (json: string): ApiErrorResponse =>
  JSON.parse(json) as ApiErrorResponse

export const apiErrorResponseToJson = (value: ApiErrorResponse): string =>
  JSON.stringify(value)
