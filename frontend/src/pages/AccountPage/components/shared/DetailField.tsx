// 本文件定义 AccountPage 页面的页面组件。

import type { ReactNode } from 'react'

type DetailFieldProps = {
  label: string
  children: ReactNode
}

export function DetailField({ label, children }: DetailFieldProps) {
  return (
    <div>
      <span className="mb-1 block text-sm text-slate-500">{label}</span>
      <strong className="text-slate-950">{children}</strong>
    </div>
  )
}
