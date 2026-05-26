export type HealthResponse = {
  status: string
  service: string
  timestamp?: string
  backendPort?: number
}
export const healthResponseFromJson = (json: string): HealthResponse =>
  JSON.parse(json) as HealthResponse

export const healthResponseToJson = (value: HealthResponse): string =>
  JSON.stringify(value)
