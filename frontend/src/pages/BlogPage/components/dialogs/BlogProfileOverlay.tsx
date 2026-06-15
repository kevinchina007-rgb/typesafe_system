import { ArrowLeft, X } from 'lucide-react'

import type { BlogPageController } from '../../objects'
import { BlogProfilePanel } from '../panels/BlogProfilePanel'

// 个人主页浮层参数，包含 controller、登录用户和关闭回调。
type BlogProfileOverlayProps = {
  controller: BlogPageController
  signedInUser: { userId: string; nickname?: string | null; avatarUrl?: string | null } | null
  onClose: () => void
}

// 个人主页浮层，用于在当前页面上覆盖展示完整主页信息。
export function BlogProfileOverlay({ controller, signedInUser, onClose }: BlogProfileOverlayProps) {
  return (
    <div className="fixed inset-0 z-[60] overflow-y-auto bg-white/95 px-6 py-6 text-slate-950 backdrop-blur-sm">
      <div className="mx-auto grid max-w-6xl gap-6">
        <div className="flex items-center justify-between">
          <button className="inline-flex items-center gap-2 border border-slate-200 bg-white px-4 py-2 font-black hover:border-pink-400" type="button" onClick={onClose}>
            <ArrowLeft className="h-5 w-5" aria-hidden="true" />
            返回
          </button>
          <button className="grid h-10 w-10 place-items-center border border-slate-200 bg-white hover:border-pink-400" type="button" onClick={onClose} title="关闭">
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>
        <BlogProfilePanel controller={controller} signedInUser={signedInUser} variant="overlay" />
      </div>
    </div>
  )
}
