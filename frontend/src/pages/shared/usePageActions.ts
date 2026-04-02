import { useState } from 'react'

import type { AppLanguage, AppNotice } from '../../lib/mvp-types'
import { mapTechnicalErrorToFriendlyMessage } from '../../lib/view-models'

export type PageNoticeHandler = (
  kind: AppNotice['kind'],
  title: string,
  description: string,
  technicalMessage?: string,
) => void

export function usePageActions(
  currentLanguage: AppLanguage,
  translate: (translationKey: string) => string,
  onShowNotice: PageNoticeHandler,
) {
  const [isBusy, setIsBusy] = useState(false)

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
