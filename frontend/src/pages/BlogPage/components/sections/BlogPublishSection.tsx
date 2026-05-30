import { BlogEditor } from '../BlogEditor'
import type { BlogPageController } from '../../objects'
import { emptyDraft, tagGroups, travelCities } from '../../functions'

type BlogPublishSectionProps = {
  controller: BlogPageController
  translate: (translationKey: string) => string
}

export function BlogPublishSection({ controller, translate }: BlogPublishSectionProps) {
  const {
    isBusy,
    drafts,
    draft,
    setDraft,
    setActiveTab,
    toggleDraftCity,
    toggleDraftTag,
    uploadDraftImage,
    saveDraft,
  } = controller

  return (
    <section className="grid gap-6">
      <div className="flex flex-wrap gap-3">
        <button className="border border-pink-500 bg-pink-500 px-5 py-3 font-black text-white" type="button" onClick={() => setDraft(emptyDraft)}>
          新建草稿
        </button>
        <button className="border border-slate-300 bg-white px-4 py-3 font-bold" type="button" onClick={() => setActiveTab('home')}>
          返回首页
        </button>
        {drafts.map(item => (
          <button
            key={item.postId}
            className="border border-slate-300 bg-white px-4 py-3 font-bold"
            type="button"
            onClick={() =>
              setDraft({
                postId: item.postId,
                title: item.title,
                summary: item.summary,
                coverText: item.coverText || item.summary,
                content: '',
                travelCities: item.travelCities ?? (item.travelCity ? [item.travelCity] : []),
                images: item.images ?? [],
                tags: item.tags ?? [],
              })
            }
          >
            {item.title || '未命名草稿'}
          </button>
        ))}
      </div>

      <BlogEditor
        isBusy={isBusy}
        translate={translate}
        initialValue={{
          title: draft.title,
          summary: draft.summary,
          content: draft.content,
          images: draft.images,
        }}
        onUploadImage={uploadDraftImage}
        onSubmit={async payload => {
          setDraft(current => ({ ...current, title: payload.title, summary: payload.summary, content: payload.content, images: payload.images }))
          await saveDraft('publish')
        }}
        onCancel={() => setActiveTab('home')}
      />

      <div className="grid gap-4 border border-slate-200 bg-white p-5">
        <div className="flex flex-wrap items-center gap-3">
          <span className="w-24 text-sm font-black text-slate-500">旅行城市</span>
          <button className="border border-slate-200 bg-white px-4 py-2 font-bold" type="button" onClick={() => setDraft(current => ({ ...current, travelCities: [] }))}>
            清空
          </button>
          {travelCities.map(city => (
            <button
              key={city}
              className={`border px-4 py-2 font-bold ${draft.travelCities.includes(city) ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`}
              type="button"
              onClick={() => toggleDraftCity(city)}
            >
              {city}
            </button>
          ))}
        </div>
        {tagGroups.map(group => (
          <div key={group.type} className="flex flex-wrap items-center gap-3">
            <span className="w-24 text-sm font-black text-slate-500">{group.label}</span>
            {group.values.map(value => {
              const active = draft.tags.some(tag => tag.tagType === group.type && tag.tagValue === value)
              return (
                <button
                  key={value}
                  className={`border px-4 py-2 font-bold ${active ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`}
                  type="button"
                  onClick={() => toggleDraftTag({ tagType: group.type, tagValue: value })}
                >
                  {value}
                </button>
              )
            })}
          </div>
        ))}
      </div>
    </section>
  )
}
