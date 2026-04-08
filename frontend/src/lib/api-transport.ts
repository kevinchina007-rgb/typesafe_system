import { getTravelBackendOrigin } from './runtime-config'
import type { ApiErrorResponse } from './mvp-types'

// 所有真正的网络 IO 都收口在这个文件里：
// fetch / headers / multipart / status code / 错误格式化 统一在这里处理。
const travelBackendOrigin = getTravelBackendOrigin()
const travelMvpApiBaseUrl = `${travelBackendOrigin}/api`

function formatApiErrorMessage(apiErrorResponse: ApiErrorResponse, status: number): string {
  return `${apiErrorResponse.code}|${apiErrorResponse.message}|HTTP ${status}`
}

export function isUnauthorizedApiError(error: unknown): boolean {
  if (!(error instanceof Error)) {
    return false
  }

  return error.message.includes('|HTTP 401') || error.message.startsWith('auth_error|')
}

export function createQueryString(queryEntries: Record<string, string | number | boolean | null | undefined>): string {
  // 统一忽略 null / undefined / 空字符串，避免各业务调用点重复写清洗逻辑。
  const searchParams = new URLSearchParams()

  Object.entries(queryEntries).forEach(([key, value]) => {
    if (value === null || value === undefined) {
      return
    }

    const normalizedValue = `${value}`.trim()
    if (normalizedValue.length === 0) {
      return
    }

    searchParams.set(key, normalizedValue)
  })

  const queryString = searchParams.toString()
  return queryString.length > 0 ? `?${queryString}` : ''
}

export function createSingleFileFormData(fieldName: string, file: File): FormData {
  const formData = new FormData()
  formData.set(fieldName, file)
  return formData
}

export async function executeApiRequest<TResponse>(path: string, options?: RequestInit): Promise<TResponse> {
  // multipart 不能强塞 application/json header，这里统一做分支。
  const isMultipartBody = typeof FormData !== 'undefined' && options?.body instanceof FormData
  const response = await fetch(`${travelMvpApiBaseUrl}${path}`, {
    credentials: 'include',
    headers: isMultipartBody
      ? {
          ...(options?.headers ?? {}),
        }
      : {
          'Content-Type': 'application/json',
          ...(options?.headers ?? {}),
        },
    ...options,
  })

  if (!response.ok) {
    // 后端统一错误形状是 { code, message }，这里转成前端统一 Error 文本。
    let responseBodyText = ''
    try {
      responseBodyText = await response.text()
      const parsedApiError = JSON.parse(responseBodyText) as Partial<ApiErrorResponse>
      if (parsedApiError.code && parsedApiError.message) {
        throw new Error(formatApiErrorMessage(parsedApiError as ApiErrorResponse, response.status))
      }
    } catch (parsingError) {
      if (parsingError instanceof Error && parsingError.message.includes('|')) {
        throw parsingError
      }
    }

    throw new Error(responseBodyText || `HTTP ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as TResponse
  }

  return (await response.json()) as TResponse
}

export function executeJsonApiRequest<TResponse>(path: string, method: string, payload?: unknown): Promise<TResponse> {
  // 业务 API 只需要传 typed payload，不需要直接接触 fetch 或 JSON.stringify。
  return executeApiRequest(path, {
    method,
    body: payload === undefined ? undefined : JSON.stringify(payload),
  })
}

export function executeMultipartApiRequest<TResponse>(path: string, method: string, formData: FormData): Promise<TResponse> {
  return executeApiRequest(path, {
    method,
    body: formData,
  })
}
