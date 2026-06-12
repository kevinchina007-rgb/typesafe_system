// 本文件定义 order 模块的 `PaymentLinkResponse`，作为响应数据并提供 JSON 编解码。

export type PaymentLinkResponse = {
  paymentUrl: string
  expiresAt: string
}
export const paymentLinkResponseFromJson = (json: string): PaymentLinkResponse =>
  JSON.parse(json) as PaymentLinkResponse

export const paymentLinkResponseToJson = (value: PaymentLinkResponse): string =>
  JSON.stringify(value)
