import { useEffect } from 'react'

import type { AppNotice } from '@/lib/mvp-types/index'

type ToastNoticeProps = {
  notice: AppNotice | null
  onDismiss: () => void
}

export function ToastNotice({ notice, onDismiss }: ToastNoticeProps) {
  useEffect(() => {
    if (!notice) {
      return
    }

    if (notice.kind === 'error') {
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

  if (notice.kind === 'error') {
    return (
      <div className="fixed inset-0 z-[90] grid place-items-center bg-slate-950/25 px-4" role="presentation" onClick={onDismiss}>
        <section
          className="w-full max-w-md border border-slate-200 bg-white px-8 py-9 text-center text-slate-950 shadow-2xl shadow-slate-950/20"
          role="dialog"
          aria-modal="true"
          aria-labelledby="app-error-title"
          onClick={event => event.stopPropagation()}
        >
          <img className="mx-auto h-28 w-28 object-contain" src="/images/fly-pig-error.png" alt="" />
          <h2 id="app-error-title" className="mt-4 text-2xl font-bold">
            出错了
          </h2>
          <p className="mx-auto mt-3 max-w-xs text-base leading-7 text-slate-700">
            {notice.description || '未知错误。小猪还没定位到摔在哪一步。'}
          </p>
          <button
            type="button"
            className="mt-7 inline-flex min-h-11 items-center justify-center border border-black bg-black px-6 py-2 text-sm font-semibold text-white transition hover:bg-white hover:text-black"
            onClick={onDismiss}
          >
            知道了
          </button>
        </section>
      </div>
    )
  }

  return (
    <aside
      className={`fixed right-6 bottom-6 z-[80] flex w-[min(28rem,calc(100vw-3rem))] items-center gap-5 border bg-white px-6 py-5 text-slate-950 shadow-2xl shadow-slate-950/15 ${
        notice.kind === 'success' ? 'border-emerald-300' : 'border-sky-300'
      }`}
    >
      <div className="min-w-0 flex-1 space-y-1">
        <strong>{notice.title}</strong>
        <p className="text-sm leading-6 text-slate-700">{notice.description}</p>
        {notice.technicalMessage ? <small className="block text-xs text-slate-500">{notice.technicalMessage}</small> : null}
      </div>
      <button
        type="button"
        className="inline-flex min-h-11 min-w-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-900 transition hover:border-black hover:bg-black hover:text-white"
        onClick={onDismiss}
      >
        OK
      </button>
    </aside>
  )
}
