import { cursorForResizeDirection, resizeDirectionFromPointer } from './AdvertisementSubmissionWorkspaceSupport'
import type { VisualStyleKey } from './AdvertisementSubmissionWorkspaceUtils'

export type AdvertisementSubmissionWorkspaceCanvasEditorProps = {
  controller: Record<string, unknown>
}

export function AdvertisementSubmissionWorkspaceCanvasEditor({ controller }: AdvertisementSubmissionWorkspaceCanvasEditorProps) {
  const c = controller as any
  return (
            <div
              ref={c.splitContainerRef}
              className="grid gap-0"
              style={{ gridTemplateColumns: `${c.factoryPanelWidth}px 12px minmax(0, 1fr)` }}
            >
              <aside className="grid content-start gap-4 border border-slate-200 bg-slate-50 p-4">
                <section className="hidden">
                </section>

                <section className="hidden">
                  <label className="grid gap-2">
                    <strong>焦点提示</strong>
                    <input value={c.focusPrompt} onChange={event => c.setFocusPrompt(event.target.value)} placeholder="例如：突出卖点、品牌调性、活动氛围" />
                  </label>
                  <label className="grid gap-2">
                    <strong>视觉元素</strong>
                    <input value={c.visualElementsPrompt} onChange={event => c.setVisualElementsPrompt(event.target.value)} placeholder="例如：人物、场景、灯光、道具" />
                  </label>
                  <label className="grid gap-2">
                    <strong>避免内容</strong>
                    <input value={c.avoidPrompt} onChange={event => c.setAvoidPrompt(event.target.value)} placeholder="例如：不要水印、不要错误文字、不要模糊图片" />
                  </label>
                </section>

                <section className="grid gap-3">
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      type="button"
                      className={`inline-flex min-h-10 items-center justify-center border px-3 text-sm font-semibold ${c.factoryMode === 'text' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-200 bg-white text-slate-950'}`}
                      onClick={() => c.setFactoryMode('text')}
                    >
                      {c.translate('advertising.factory.text')}
                    </button>
                    <button
                      type="button"
                      className={`inline-flex min-h-10 items-center justify-center border px-3 text-sm font-semibold ${c.factoryMode === 'image' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-200 bg-white text-slate-950'}`}
                      onClick={() => c.setFactoryMode('image')}
                    >
                      {c.translate('advertising.factory.image')}
                    </button>
                  </div>

                  {c.factoryMode === 'text' ? (
                    <div className="grid gap-3">
                      <strong>{c.translate('advertising.factory.text')}</strong>
                      <textarea rows={3} value={c.textPrompt} onChange={event => c.setTextPrompt(event.target.value)} placeholder={c.translate('advertising.factory.textPlaceholder')} />
                      <label className="grid gap-2">
                        <span className="text-sm font-semibold text-slate-700">风格偏好</span>
                        <div className="flex flex-wrap gap-2">
                          {(Object.keys(c.visualStyleLabels) as VisualStyleKey[]).map((styleKey: VisualStyleKey) => (
                            <button
                              key={styleKey}
                              type="button"
                              className={`inline-flex min-h-9 items-center justify-center border px-3 text-sm font-semibold ${c.textVisualStyles.includes(styleKey) ? 'border-pink-500 bg-pink-50 text-pink-700' : 'border-slate-200 bg-white text-slate-700'}`}
                              onClick={() => c.toggleStyleSelection(c.textVisualStyles, styleKey, c.setTextVisualStyles)}
                            >
                              {c.visualStyleLabels[styleKey]}
                            </button>
                          ))}
                        </div>
                      </label>
                      <label className="grid gap-2">
                        <span className="text-sm font-semibold text-slate-700">自定义风格偏好</span>
                        <input
                          value={c.textStylePreference}
                          onChange={event => c.setTextStylePreference(event.target.value)}
                          placeholder="例如：海报感、杂志封面风、克制高级感"
                        />
                      </label>
                      <div className="grid grid-cols-2 gap-2">
                        <button
                          type="button"
                          className="inline-flex min-h-10 items-center justify-center border border-black bg-black px-3 text-sm font-semibold text-white disabled:opacity-60"
                          disabled={c.isGeneratingText}
                          onClick={() => void c.generateTextStyles()}
                        >
                          {c.isGeneratingText ? c.translate('search.loading') : c.translate('advertising.factory.generateText')}
                        </button>
                        <button
                          type="button"
                          className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-3 text-sm font-semibold text-slate-950 disabled:opacity-60"
                          disabled={c.isGeneratingText}
                          onClick={() => {
                            void c.generateTextStyles()
                          }}
                        >
                          重新生成
                        </button>
                      </div>
                      <div className="grid gap-2">
                        {c.textCandidates.map((candidate: any) => (
                          <button
                            key={candidate.id}
                            type="button"
                            draggable
                            className="grid gap-1 border border-slate-200 bg-white p-3 text-left shadow-sm transition hover:border-slate-950"
                            onDragStart={() => c.setDragTemplate(candidate)}
                            onClick={() => c.addTemplateToCanvas(candidate)}
                          >
                            <span className="text-xs font-bold text-slate-500">{c.translate('advertising.factory.preview')}</span>
                            {candidate.src ? (
                              <img
                                src={candidate.src}
                                alt={candidate.text}
                                className="max-h-44 w-full rounded border border-slate-100 bg-white object-contain"
                              />
                            ) : null}
                            <strong
                              className="max-w-full whitespace-pre-wrap break-words text-left leading-snug"
                              style={{ color: candidate.color === '#ffffff' ? c.tonePalettes[c.tone].bg : candidate.color, fontSize: candidate.fontSize ? `${Math.max(24, Math.min(candidate.fontSize, 44))}px` : '34px' }}
                            >
                              {candidate.text}
                            </strong>
                            <span className="text-xs text-slate-500">可拖拽到画布</span>
                          </button>
                        ))}
                      </div>
                    </div>
                  ) : (
                    <div className="grid gap-3">
                      <strong>{c.translate('advertising.factory.image')}</strong>
                      <div className="grid grid-cols-2 gap-2">
                        <button
                          type="button"
                          className={`inline-flex min-h-10 items-center justify-center border px-3 text-sm font-semibold ${c.imageFactoryKind === 'background' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-200 bg-white text-slate-950'}`}
                          onClick={() => c.setImageFactoryKind('background')}
                        >
                          背景
                        </button>
                        <button
                          type="button"
                          className={`inline-flex min-h-10 items-center justify-center border px-3 text-sm font-semibold ${c.imageFactoryKind === 'element' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-200 bg-white text-slate-950'}`}
                          onClick={() => c.setImageFactoryKind('element')}
                        >
                          元素
                        </button>
                      </div>
                      <textarea rows={3} value={c.imagePrompt} onChange={event => c.setImagePrompt(event.target.value)} placeholder={c.translate('advertising.factory.imagePlaceholder')} />
                      {c.imageFactoryKind === 'element' ? (
                        <>
                          <label className="grid gap-2">
                            <span className="text-sm font-semibold text-slate-700">是否裁切成元素图</span>
                            <select value={c.shouldCutoutImageElement ? 'yes' : 'no'} onChange={event => c.setShouldCutoutImageElement(event.target.value === 'yes')}>
                              <option value="yes">是</option>
                              <option value="no">否</option>
                            </select>
                          </label>
                          <label className="grid gap-2">
                            <span className="text-sm font-semibold text-slate-700">视觉风格</span>
                            <div className="flex flex-wrap gap-2">
                          {(Object.keys(c.visualStyleLabels) as VisualStyleKey[]).map((styleKey: VisualStyleKey) => (
                                <button
                                  key={styleKey}
                                  type="button"
                                  className={`inline-flex min-h-9 items-center justify-center border px-3 text-sm font-semibold ${c.imageVisualStyles.includes(styleKey) ? 'border-pink-500 bg-pink-50 text-pink-700' : 'border-slate-200 bg-white text-slate-700'}`}
                                  onClick={() => c.toggleStyleSelection(c.imageVisualStyles, styleKey, c.setImageVisualStyles)}
                                >
                                  {c.visualStyleLabels[styleKey]}
                                </button>
                              ))}
                            </div>
                          </label>
                        </>
                      ) : (
                        <>
                          <label className="grid gap-2">
                            <span className="text-sm font-semibold text-slate-700">视觉风格</span>
                            <div className="flex flex-wrap gap-2">
                          {(Object.keys(c.visualStyleLabels) as VisualStyleKey[]).map((styleKey: VisualStyleKey) => (
                                <button
                                  key={styleKey}
                                  type="button"
                                  className={`inline-flex min-h-9 items-center justify-center border px-3 text-sm font-semibold ${c.imageVisualStyles.includes(styleKey) ? 'border-pink-500 bg-pink-50 text-pink-700' : 'border-slate-200 bg-white text-slate-700'}`}
                                  onClick={() => c.toggleStyleSelection(c.imageVisualStyles, styleKey, c.setImageVisualStyles)}
                                >
                                  {c.visualStyleLabels[styleKey]}
                                </button>
                              ))}
                            </div>
                          </label>
                          <label className="grid gap-2">
                            <span className="text-sm font-semibold text-slate-700">是否让背景自动适配创意内容</span>
                            <select value={c.backgroundAutoFit ? 'yes' : 'no'} onChange={event => c.setBackgroundAutoFit(event.target.value === 'yes')}>
                              <option value="yes">是</option>
                              <option value="no">否</option>
                            </select>
                          </label>
                        </>
                      )}
                      <div className="grid grid-cols-2 gap-2">
                        <button type="button" className="inline-flex min-h-10 items-center justify-center border border-black bg-black px-3 text-sm font-semibold text-white disabled:opacity-60" disabled={c.isGeneratingImages} onClick={() => void c.generateImageStyles()}>
                          {c.isGeneratingImages ? c.translate('search.loading') : c.translate('advertising.factory.generateImage')}
                        </button>
                        <button
                          type="button"
                          className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-3 text-sm font-semibold text-slate-950 disabled:opacity-60"
                          disabled={c.isGeneratingImages}
                          onClick={() => {
                            void c.generateImageStyles()
                          }}
                        >
                          重新生成
                        </button>
                      </div>
                      <div className="grid gap-2">
                        {c.imageCandidates.map((candidate: any) => (
                          <button key={candidate.id} type="button" draggable className="overflow-hidden border border-slate-200 bg-white text-left transition hover:border-slate-950" onDragStart={() => c.setDragTemplate(candidate)} onClick={() => c.addTemplateToCanvas(candidate)}>
                            {candidate.src ? <img src={candidate.src} alt="" className={`aspect-video w-full ${candidate.contentMode === 'contain' ? 'object-contain bg-slate-50' : 'object-cover'}`} /> : null}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </section>
              </aside>

              <div
                aria-label={c.translate('advertising.resizeFactoryPanel')}
                role="separator"
                className={`group flex cursor-col-resize items-stretch justify-center px-1 ${c.isResizingFactoryPanel ? 'bg-slate-100' : ''}`}
                onMouseDown={event => {
                  event.preventDefault()
                  c.setIsResizingFactoryPanel(true)
                }}
              >
                <span className={`my-1 w-1 rounded bg-slate-200 transition group-hover:bg-pink-400 ${c.isResizingFactoryPanel ? 'bg-pink-500' : ''}`} />
              </div>

              <main className="grid content-start gap-4">
                <div className="grid gap-3 border border-slate-200 bg-white p-4">
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-sm font-bold text-slate-500">{c.canvasWidth} x {c.canvasHeight}</span>
                    <span className="text-sm text-slate-500">未启用资源绑定</span>
                  </div>
                  <div
                    ref={c.canvasRef}
                    className="relative overflow-hidden border border-slate-300"
                    style={{ width: '100%', aspectRatio: `${c.canvasWidth} / ${c.canvasHeight}`, backgroundColor: c.creative.backgroundColor }}
                    onContextMenu={event => c.openCanvasContextMenu(event, null)}
                    onDragOver={event => event.preventDefault()}
                    onDrop={event => {
                      event.preventDefault()
                      if (!c.dragTemplate || !c.canvasRef.current) return
                      const rect = c.canvasRef.current.getBoundingClientRect()
                      const scale = c.canvasWidth / rect.width
                      c.addTemplateToCanvas(c.dragTemplate, {
                        x: Math.round((event.clientX - rect.left) * scale),
                        y: Math.round((event.clientY - rect.top) * scale),
                      })
                      c.setDragTemplate(null)
                    }}
                  >
                    {c.creative.elements.map((element: any) => {
                      const scale = 100 / c.canvasWidth
                      const hoverResizeDirection = c.hoverResizeDirectionByElementId[element.id]
                      const style = {
                        left: `${element.x * scale}%`,
                        top: `${element.y / c.canvasHeight * 100}%`,
                        width: `${element.width * scale}%`,
                        height: `${element.height / c.canvasHeight * 100}%`,
                        color: element.color,
                        backgroundColor: element.backgroundColor,
                        opacity: element.opacity,
                        borderRadius: element.borderRadius,
                        fontSize: `${Math.max(10, element.fontSize * 0.42)}px`,
                        fontWeight: element.fontWeight,
                        cursor: hoverResizeDirection
                          ? cursorForResizeDirection(hoverResizeDirection)
                          : 'move',
                      }
                      return (
                        <div
                          key={element.id}
                          className="group absolute overflow-hidden border border-transparent text-left transition hover:border-white hover:ring-2 hover:ring-pink-500"
                          style={style}
                          onMouseMove={event => {
                            if (c.dragState || c.resizeState) return
                            if ((event.target as HTMLElement).closest('[data-remove-handle="true"]')) {
                              c.setHoverResizeDirectionByElementId((current: any) => current[element.id] ? { ...current, [element.id]: null } : current)
                              return
                            }
                            const direction = resizeDirectionFromPointer(event, event.currentTarget.getBoundingClientRect())
                            c.setHoverResizeDirectionByElementId((current: any) =>
                              current[element.id] === direction ? current : { ...current, [element.id]: direction },
                            )
                          }}
                          onMouseLeave={() => {
                            c.setHoverResizeDirectionByElementId((current: any) => current[element.id] ? { ...current, [element.id]: null } : current)
                          }}
                          onContextMenu={event => c.openCanvasContextMenu(event, element.id)}
                          onMouseDown={event => {
                            if (!c.canvasRef.current) return
                            if ((event.target as HTMLElement).closest('[data-remove-handle="true"]')) {
                              return
                            }
                            event.preventDefault()
                            const elementRect = event.currentTarget.getBoundingClientRect()
                            const resizeDirection = hoverResizeDirection ?? resizeDirectionFromPointer(event, elementRect)
                            if (resizeDirection) {
                              c.setResizeState({
                                elementId: element.id,
                                startClientX: event.clientX,
                                startClientY: event.clientY,
                                startX: element.x,
                                startY: element.y,
                                startWidth: element.width,
                                startHeight: element.height,
                                direction: resizeDirection,
                              })
                              return
                            }
                            const rect = c.canvasRef.current.getBoundingClientRect()
                            c.setDragState({ elementId: element.id, offsetX: event.clientX - rect.left - element.x / c.canvasWidth * rect.width, offsetY: event.clientY - rect.top - element.y / c.canvasHeight * rect.height })
                          }}
                        >
                          <button
                            type="button"
                            data-remove-handle="true"
                            className="absolute right-1 top-1 z-20 grid h-6 w-6 place-items-center rounded-full bg-black/70 text-xs font-bold text-white opacity-0 transition-opacity group-hover:opacity-100 focus:opacity-100"
                            onClick={() => c.removeElement(element.id)}
                          >
                            删
                          </button>
                          {element.src ? (
                            <img src={element.src} alt="" className={`h-full w-full ${element.contentMode === 'contain' ? 'object-contain' : 'object-cover'}`} />
                          ) : (
                            <div className="grid h-full w-full place-items-center px-3 text-center">{element.text}</div>
                          )}
                          <div className="pointer-events-none absolute inset-0 border border-transparent transition group-hover:border-white/70" />
                        </div>
                      )
                    })}
                    {c.canvasContextMenu ? (
                      <div
                        className="fixed z-50 min-w-32 border border-slate-200 bg-white p-1 text-sm font-semibold text-slate-950 shadow-xl shadow-slate-900/15"
                        style={{ left: c.canvasContextMenu.x, top: c.canvasContextMenu.y }}
                        onClick={event => event.stopPropagation()}
                        onContextMenu={event => event.preventDefault()}
                      >
                        {c.canvasContextMenu.targetElementId ? (
                          <>
                            <button
                              type="button"
                              className="block w-full px-3 py-2 text-left hover:bg-slate-100"
                              onClick={() => c.copyCanvasElement(c.canvasContextMenu.targetElementId!)}
                            >
                              复制
                            </button>
                            <button
                              type="button"
                              className="block w-full px-3 py-2 text-left hover:bg-slate-100"
                              onClick={() => c.cutCanvasElement(c.canvasContextMenu.targetElementId!)}
                            >
                              剪切
                            </button>
                          </>
                        ) : null}
                        <button
                          type="button"
                          className="block w-full px-3 py-2 text-left hover:bg-slate-100 disabled:cursor-not-allowed disabled:text-slate-400 disabled:hover:bg-white"
                          disabled={!c.copiedElement}
                          onClick={c.pasteCanvasElement}
                        >
                          粘贴
                        </button>
                      </div>
                    ) : null}
                  </div>
                  <p className="m-0 text-sm leading-6 text-slate-500">{c.translate('advertising.factory.canvasHint')}</p>
                </div>
              </main>
            </div>
  )
}
