// 本文件定义 GetHealthPlanner，负责 auth 模块的获取编排和接口入口。

import type { HealthResponse } from '@/microservices/common/objects/HealthResponse'
import { executeApiRequest } from '@/microservices/common/api/ApiTransport'

export const getHealth = (): Promise<HealthResponse> => executeApiRequest('/health')
