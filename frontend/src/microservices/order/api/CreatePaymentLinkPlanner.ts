// 本文件定义 CreatePaymentLinkPlanner，负责 order 模块的创建编排和接口入口。

import type { PaymentLinkResponse } from '@/microservices/order/objects/PaymentLinkResponse'

import { getTravelBackendOrigin } from '@/lib/config/runtime-config'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createPaymentLink = (orderId: string, userId: string, paymentMethod: string, language: 'zh'): Promise<PaymentLinkResponse> =>
    executeJsonApiRequest('/CreatePaymentLinkPlanner', 'POST', {
        orderId,
        userId,
        paymentMethod,
        language,
        publicBackendOrigin: getTravelBackendOrigin(),
    })
