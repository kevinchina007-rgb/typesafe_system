import { getTravelBackendOrigin } from '@/lib/config/runtime-config'
import type { ApiErrorResponse } from '@/lib/mvp-types/index'

// 所有真正的网络 IO 都收口在这个文件里：
// fetch / headers / multipart / status code / 错误格式统一在这里处理。
const travelBackendOrigin = getTravelBackendOrigin()
const travelMvpApiBaseUrl = `${travelBackendOrigin}/api`
const defaultRequestTimeoutMs = 180000

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
  const abortController = new AbortController()
  const timeoutHandle = window.setTimeout(() => abortController.abort(), defaultRequestTimeoutMs)

  let response: Response
  try {
    response = await fetch(`${travelMvpApiBaseUrl}${path}`, {
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
      signal: abortController.signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error(`request_timeout|The request took too long to finish.|HTTP 408`)
    }
    throw error
  } finally {
    window.clearTimeout(timeoutHandle)
  }

  if (!response.ok) {
    // 兼容旧接口的 { code, message } 和 PlannerRouter 的 { error }。
    let responseBodyText = ''
    try {
      responseBodyText = await response.text()
      const parsedApiError = JSON.parse(responseBodyText) as Partial<ApiErrorResponse> & { error?: string }
      if (parsedApiError.code && parsedApiError.message) {
        throw new Error(formatApiErrorMessage(parsedApiError as ApiErrorResponse, response.status))
      }
      if (parsedApiError.error?.trim()) {
        throw new Error(`planner_error|${parsedApiError.error}|HTTP ${response.status}`)
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
