export type PaymentLinkResponse = {
  paymentUrl: string
  expiresAt: string
}
export const paymentLinkResponseFromJson = (json: string): PaymentLinkResponse =>
  JSON.parse(json) as PaymentLinkResponse

export const paymentLinkResponseToJson = (value: PaymentLinkResponse): string =>
  JSON.stringify(value)
