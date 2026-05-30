import type { BlogPageController } from '../../objects'
import { BlogProfilePanel } from '../panels/BlogProfilePanel'

type BlogMineSectionProps = {
  controller: BlogPageController
  signedInUser: { userId: string; nickname?: string | null; avatarUrl?: string | null } | null
}

export function BlogMineSection({ controller, signedInUser }: BlogMineSectionProps) {
  return (
    <section className="grid gap-6">
      <BlogProfilePanel controller={controller} signedInUser={signedInUser} variant="inline" />
    </section>
  )
}
