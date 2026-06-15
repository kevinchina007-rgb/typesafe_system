// 本文件定义 GetHealthPlanner，负责 auth 模块的获取编排和接口入口。

import type { HealthResponse } from '@/shared-kernel/objects/HealthResponse'
import { executeApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getHealth = (): Promise<HealthResponse> => executeApiRequest('/health')
