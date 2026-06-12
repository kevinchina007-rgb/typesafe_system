// 本文件定义 order 模块的 `PaymentResponse`，作为响应数据并提供 JSON 编解码。

export type PaymentResponse = {
  paymentId: string
  paymentAmount: string
  paymentCurrency: string
  paymentMethod: string
  paymentStatus: string
  authorizedAt: string
  capturedAt: string | null
}
export const paymentResponseFromJson = (json: string): PaymentResponse =>
  JSON.parse(json) as PaymentResponse

export const paymentResponseToJson = (value: PaymentResponse): string =>
  JSON.stringify(value)
