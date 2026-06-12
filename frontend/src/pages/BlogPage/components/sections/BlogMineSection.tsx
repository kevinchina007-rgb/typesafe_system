import type { BlogPageController } from '../../objects'
import { BlogProfilePanel } from '../panels/BlogProfilePanel'

// 我的页面参数，包含 controller 和当前登录用户。
type BlogMineSectionProps = {
  controller: BlogPageController
  signedInUser: { userId: string; nickname?: string | null; avatarUrl?: string | null } | null
}

// 我的页面区块，直接复用个人主页面板。
export function BlogMineSection({ controller, signedInUser }: BlogMineSectionProps) {
  return (
    <section className="grid gap-6">
      <BlogProfilePanel controller={controller} signedInUser={signedInUser} variant="inline" />
    </section>
  )
}
