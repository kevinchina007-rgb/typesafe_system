import { useEffect, useState } from 'react'

import type { ContentImageResponse } from '../lib/mvp-types'
import { ContentImageUploader } from './ContentImageUploader'

type BlogEditorProps = {
  isBusy: boolean
  mode?: 'create' | 'edit'
  initialValue?: {
    title: string
    summary: string
    content: string
    images: ContentImageResponse[]
  } | null
  translate: (translationKey: string) => string
  onUploadImage: (imageFile: File) => Promise<ContentImageResponse>
  onSubmit: (payload: {
    title: string
    summary: string
    content: string
    images: ContentImageResponse[]
  }) => Promise<void>
  onCancel?: () => void
}

export function BlogEditor({
  isBusy,
  mode = 'create',
  initialValue,
  translate,
  onUploadImage,
  onSubmit,
  onCancel,
}: BlogEditorProps) {
  const [title, setTitle] = useState(initialValue?.title ?? '')
  const [summary, setSummary] = useState(initialValue?.summary ?? '')
  const [content, setContent] = useState(initialValue?.content ?? '')
  const [images, setImages] = useState<ContentImageResponse[]>(initialValue?.images ?? [])

  useEffect(() => {
    setTitle(initialValue?.title ?? '')
    setSummary(initialValue?.summary ?? '')
    setContent(initialValue?.content ?? '')
    setImages(initialValue?.images ?? [])
  }, [initialValue?.title, initialValue?.summary, initialValue?.content, initialValue?.images])

  return (
    <form
      className="stack-form panel-card"
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
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('blog.editorEyebrow')}</p>
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

      <div className="manager-task-actions">
        <button type="submit" disabled={isBusy}>
          {mode === 'edit' ? translate('blog.save') : translate('blog.publish')}
        </button>
        {mode === 'edit' && onCancel ? (
          <button type="button" className="secondary-button" disabled={isBusy} onClick={onCancel}>
            {translate('tourGroups.cancel')}
          </button>
        ) : null}
      </div>
    </form>
  )
}
