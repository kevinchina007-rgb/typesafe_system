// 本文件定义页面级动作包装逻辑，负责统一处理确认、请求调用和提示反馈。

import { useState } from 'react'

import type { AppLanguage, AppNotice } from '@/lib/mvp-types/index'
import { mapTechnicalErrorToFriendlyMessage } from '@/lib/presenters/view-models'

// 页面通知处理器，统一由页面层传入。
export type PageNoticeHandler = (
  kind: AppNotice['kind'],
  title: string,
  description: string,
  technicalMessage?: string,
) => void

// 页面动作钩子，统一处理忙碌态和成功/失败提示。
export function usePageActions(
  currentLanguage: AppLanguage,
  translate: (translationKey: string) => string,
  onShowNotice: PageNoticeHandler,
) {
  // 页面级统一忙碌状态，避免重复点击。
  const [isBusy, setIsBusy] = useState(false)

  // 只需要成功提示的页面动作。
  async function runPageAction(action: () => Promise<void>, successTitle: string, successDescription: string) {
    setIsBusy(true)
    try {
      await action()
      onShowNotice('success', successTitle, successDescription)
    } catch (error) {
      const technicalMessage = error instanceof Error ? error.message : 'Unknown error'
      onShowNotice(
        'error',
        translate('error.friendly.default'),
        mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage),
        technicalMessage,
      )
    } finally {
      setIsBusy(false)
    }
  }

  // 需要返回结果的页面动作，成功后把结果继续传回调用方。
  async function runPageActionWithResult<TValue>(
    action: () => Promise<TValue>,
    successTitle: string,
    successDescription: string,
  ): Promise<TValue> {
    setIsBusy(true)
    try {
      const result = await action()
      onShowNotice('success', successTitle, successDescription)
      return result
    } catch (error) {
      const technicalMessage = error instanceof Error ? error.message : 'Unknown error'
      onShowNotice(
        'error',
        translate('error.friendly.default'),
        mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage),
        technicalMessage,
      )
      throw error
    } finally {
      setIsBusy(false)
    }
  }

  return {
    isBusy,
    runPageAction,
    runPageActionWithResult,
  }
}
