import { useEffect } from 'react'

import type { AppNotice } from '../lib/mvp-types'

type ToastNoticeProps = {
  notice: AppNotice | null
  onDismiss: () => void
}

export function ToastNotice({ notice, onDismiss }: ToastNoticeProps) {
  useEffect(() => {
    if (!notice) {
      return
    }

    const timeoutId = window.setTimeout(() => {
      onDismiss()
    }, 4000)

    return () => window.clearTimeout(timeoutId)
  }, [notice, onDismiss])

  if (!notice) {
    return null
  }

  return (
    <aside className={`toast-notice toast-${notice.kind}`}>
      <div className="toast-copy">
        <strong>{notice.title}</strong>
        <p>{notice.description}</p>
        {notice.technicalMessage ? <small>{notice.technicalMessage}</small> : null}
      </div>
      <button type="button" className="secondary-button" onClick={onDismiss}>
        OK
      </button>
    </aside>
  )
}
