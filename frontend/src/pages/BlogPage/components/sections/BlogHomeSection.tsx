import { Search } from 'lucide-react'

import { BlogPostCard } from '../BlogPostCard'
import type { BlogPageController } from '../../objects'
import { tagGroups, travelCities } from '../../functions'

// 首页区块参数，只接收 controller。
type BlogHomeSectionProps = {
  controller: BlogPageController
}

// Blog 首页，负责展示搜索、筛选和帖子列表。
export function BlogHomeSection({ controller }: BlogHomeSectionProps) {
  const {
    posts,
    query,
    selectedTag,
    selectedCities,
    setQuery,
    setSelectedTag,
    setSelectedCities,
    toggleCity,
    selectedPost,
    openPost,
    openProfile,
    likePost,
  } = controller

  return (
    <section className="grid gap-6">
      <div className="grid gap-4">
        <label className="grid gap-2">
          <span className="text-sm font-black text-slate-500">搜索帖子</span>
          <span className="flex min-h-14 items-center border-2 border-slate-300 bg-white px-4 focus-within:border-pink-500">
            <Search className="mr-3 h-6 w-6 text-slate-500" aria-hidden="true" />
            <input
              className="min-w-0 flex-1 appearance-none !border-0 !bg-transparent !p-0 text-lg font-bold !shadow-none outline-none focus:ring-0"
              value={query}
              onChange={event => setQuery(event.target.value)}
              placeholder="输入城市、标题、攻略关键词或旅行灵感"
            />
          </span>
        </label>
        <div className="grid gap-3 bg-slate-50 p-4">
          {tagGroups.map(group => (
            <div key={group.type} className="flex flex-wrap items-center gap-3">
              <span className="w-24 text-sm font-black text-slate-500">{group.label}</span>
              <button className="border border-slate-200 bg-white px-4 py-2 font-bold" type="button" onClick={() => setSelectedTag(null)}>
                全部
              </button>
              {group.values.map(value => (
                <button
                  key={value}
                  className={`border px-4 py-2 font-bold ${selectedTag?.tagType === group.type && selectedTag.tagValue === value ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`}
                  type="button"
                  onClick={() => setSelectedTag({ tagType: group.type, tagValue: value })}
                >
                  {value}
                </button>
              ))}
            </div>
          ))}
          <div className="flex flex-wrap items-center gap-3">
            <span className="w-24 text-sm font-black text-slate-500">旅行城市</span>
            <button className="border border-slate-200 bg-white px-4 py-2 font-bold" type="button" onClick={() => setSelectedCities([])}>
              全部
            </button>
            {travelCities.map(city => (
              <button
                key={city}
                className={`border px-4 py-2 font-bold ${selectedCities.includes(city) ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`}
                type="button"
                onClick={() => toggleCity(city)}
              >
                {city}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-5 md:grid-cols-3 xl:grid-cols-4">
        {posts.map(post => (
          <BlogPostCard
            key={post.postId}
            post={post}
            onOpenPost={id => void openPost(id)}
            onOpenProfile={id => void openProfile(id)}
            onLike={nextPost => void likePost(nextPost)}
            isActive={selectedPost?.post.postId === post.postId}
          />
        ))}
        {posts.length === 0 ? <p className="text-slate-500">暂无帖子，先去发布一篇旅行灵感吧。</p> : null}
      </div>
    </section>
  )
}
