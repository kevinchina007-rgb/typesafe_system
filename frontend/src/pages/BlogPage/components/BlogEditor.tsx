// BlogEditor：博客页面博客编辑器组件。

import { useEffect, useState } from 'react'

import type { ContentImagePlannerResponse } from '@/lib/mvp-types/index'
import { ContentImageUploader } from '@/pages/shared/content/ContentImageUploader'

// Blog 编辑器参数，负责文章标题、摘要、正文和配图。
type BlogEditorProps = {
  isBusy: boolean
  mode?: 'create' | 'edit'
  initialValue?: {
    title: string
    summary: string
    content: string
    images: ContentImagePlannerResponse[]
  } | null
  translate: (translationKey: string) => string
  onUploadImage: (imageFile: File) => Promise<ContentImagePlannerResponse>
  onSubmit: (payload: {
    title: string
    summary: string
    content: string
    images: ContentImagePlannerResponse[]
  }) => Promise<void>
  onCancel?: () => void
}

// Blog 编辑器，负责输入内容并提交草稿或正式发布。
export function BlogEditor({
  isBusy,
  mode = 'create',
  initialValue,
  translate,
  onUploadImage,
  onSubmit,
  onCancel,
}: BlogEditorProps) {
  // 标题输入值。
  const [title, setTitle] = useState(initialValue?.title ?? '')
  // 摘要输入值。
  const [summary, setSummary] = useState(initialValue?.summary ?? '')
  // 正文输入值。
  const [content, setContent] = useState(initialValue?.content ?? '')
  // 上传图片列表。
  const [images, setImages] = useState<ContentImagePlannerResponse[]>(initialValue?.images ?? [])

  useEffect(() => {
    setTitle(initialValue?.title ?? '')
    setSummary(initialValue?.summary ?? '')
    setContent(initialValue?.content ?? '')
    setImages(initialValue?.images ?? [])
  }, [initialValue?.title, initialValue?.summary, initialValue?.content, initialValue?.images])

  return (
    <form
      className="grid gap-4 grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
      onSubmit={async event => {
        event.preventDefault()
        await onSubmit({
          title,
          summary,
          content,
          images,
        })
        if (mode === 'create') {
          setTitle('')
          setSummary('')
          setContent('')
          setImages([])
        }
      }}
    >
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('blog.editorEyebrow')}</p>
          <h3>{mode === 'edit' ? translate('blog.editorEditTitle') : translate('blog.editorTitle')}</h3>
        </div>
      </div>

      <label>
        {translate('blog.title')}
        <input value={title} onChange={event => setTitle(event.target.value)} disabled={isBusy} />
      </label>

      <label>
        {translate('blog.summary')}
        <textarea value={summary} onChange={event => setSummary(event.target.value)} disabled={isBusy} rows={3} />
      </label>

      <label>
        {translate('blog.content')}
        <textarea value={content} onChange={event => setContent(event.target.value)} disabled={isBusy} rows={8} />
      </label>

      <ContentImageUploader
        images={images}
        isBusy={isBusy}
        translate={translate}
        onUploadImage={onUploadImage}
        onChangeImages={setImages}
      />

      <div className="flex flex-wrap items-center gap-3">
        <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
          {mode === 'edit' ? translate('blog.save') : translate('blog.publish')}
        </button>
        {onCancel ? (
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onCancel}>
            {translate('tourGroups.cancel')}
          </button>
        ) : null}
      </div>
    </form>
  )
}
