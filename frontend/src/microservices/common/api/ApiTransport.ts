import { getTravelBackendOrigin } from '@/lib/config/runtime-config'
import type { ApiErrorResponse } from '@/lib/mvp-types/index'

// 鎵€鏈夌湡姝ｇ殑缃戠粶 IO 閮芥敹鍙ｅ湪杩欎釜鏂囦欢閲岋細
// fetch / headers / multipart / status code / 閿欒鏍煎紡鍖?缁熶竴鍦ㄨ繖閲屽鐞嗐€?
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
  // 缁熶竴蹇界暐 null / undefined / 绌哄瓧绗︿覆锛岄伩鍏嶅悇涓氬姟璋冪敤鐐归噸澶嶅啓娓呮礂閫昏緫銆?
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
  // multipart 涓嶈兘寮哄 application/json header锛岃繖閲岀粺涓€鍋氬垎鏀€?
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
    // 鍚庣缁熶竴閿欒褰㈢姸鏄?{ code, message }锛岃繖閲岃浆鎴愬墠绔粺涓€ Error 鏂囨湰銆?
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
  // 涓氬姟 API 鍙渶瑕佷紶 typed payload锛屼笉闇€瑕佺洿鎺ユ帴瑙?fetch 鎴?JSON.stringify銆?
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
