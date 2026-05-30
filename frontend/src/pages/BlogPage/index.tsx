import type { BlogPageProps } from './objects'
import { sidebarItems } from './functions'
import { useBlogPageController } from './hooks'
import {
  BlogHomeSection,
  BlogMineSection,
  BlogNotificationsSection,
  BlogProfileOverlay,
  BlogPublishSection,
  BlogSelectedPostDialog,
} from './components'

export function BlogPage(props: BlogPageProps) {
  const controller = useBlogPageController(props)
  const { activeTab, profileOverlayOpen, selectedPost } = controller
  const { signedInUser, translate } = props

  return (
    <section className="grid min-h-[calc(100vh-10rem)] grid-cols-[13rem_minmax(0,1fr)] gap-8 bg-white p-6 text-slate-950">
      <aside className="sticky top-6 grid h-fit gap-3 border-r border-slate-200 pr-5">
        <strong className="mb-2 text-3xl font-black text-pink-500">flybara 社区</strong>
        {sidebarItems.map(item => (
          <button
            key={item.key}
            type="button"
            className={`min-h-12 border px-4 text-left text-lg font-black transition ${
              activeTab === item.key ? 'border-pink-500 bg-pink-500 text-white' : 'border-transparent bg-white hover:border-slate-300'
            }`}
            onClick={() => controller.setActiveTab(item.key)}
          >
            {item.label}
          </button>
        ))}
      </aside>

      <main className="min-w-0">
        {activeTab === 'home' ? <BlogHomeSection controller={controller} /> : null}
        {activeTab === 'publish' ? <BlogPublishSection controller={controller} translate={translate} /> : null}
        {activeTab === 'notifications' ? <BlogNotificationsSection controller={controller} /> : null}
        {activeTab === 'mine' ? <BlogMineSection controller={controller} signedInUser={signedInUser} /> : null}
      </main>

      {profileOverlayOpen ? (
        <BlogProfileOverlay controller={controller} signedInUser={signedInUser} onClose={controller.closeProfileOverlay} />
      ) : null}

      {selectedPost ? (
        <BlogSelectedPostDialog controller={controller} signedInUser={signedInUser} onClose={() => controller.setSelectedPost(null)} />
      ) : null}
    </section>
  )
}
