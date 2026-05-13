import type { PaymentLinkResponse } from '@/microservices/order/objects/PaymentLinkResponse'



import { createQueryString, executeApiRequest } from '@/microservices/common/api/ApiTransport'

export const createPaymentLink = (orderId: string, paymentMethod: string, language: 'en' | 'zh'): Promise<PaymentLinkResponse> =>
    executeApiRequest(`/orders/${orderId}/payment-link${createQueryString({ paymentMethod, lang: language })}`)
