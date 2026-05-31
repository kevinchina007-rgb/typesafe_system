import { useEffect, useMemo, useRef, useState } from 'react'

import { useAdvertisingStore } from '@/app/stores/advertising-store'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'

type AdvertisementKind = 'ResourcePromotion' | 'CompanyPromotion'
type CanvasElementType = 'text' | 'image' | 'shape' | 'field'
type PlacementValue = 'FlightBookingPage' | 'HotelBookingPage' | 'TrainBookingPage' | 'AttractionBookingPage'
type TargetResourceType = 'Flight' | 'Hotel' | 'Train' | 'Attraction'
type WorkflowStep = 'kind' | 'resource' | 'creative'
type ToneKey = 'clean' | 'premium' | 'energetic' | 'warm'

type AdvertisementSubmissionWorkspaceProps = {
  defaultPlacement: PlacementValue
  defaultTargetResourceType: TargetResourceType
  resourceOptions: Array<{ value: string; label: string }>
  translate: (translationKey: string) => string
  onOpenResource: (resourceId: string) => void
  onShowNotice?: (kind: 'success' | 'error', title: string, description: string) => void
}

type CreativeElement = {
  id: string
  type: CanvasElementType
  text: string
  src?: string
  x: number
  y: number
  width: number
  height: number
  fontSize: number
  fontWeight: number
  color: string
  backgroundColor: string
  opacity: number
  borderRadius: number
  effect: 'none' | 'fadeIn' | 'slideUp' | 'pulse'
}

type CreativeState = {
  width: number
  height: number
  backgroundColor: string
  elements: CreativeElement[]
}

const canvasWidth = 960
const canvasHeight = 240
const timeWindows = ['00:00-03:59', '04:00-07:59', '08:00-11:59', '12:00-15:59', '16:00-19:59', '20:00-23:59']
const flightCityOptions = ['北京', '上海', '武汉', '南京', '杭州', '深圳', '重庆', '广州', '成都', '长沙', '厦门', '西安', '天津', '青岛']
const cityAirportCodes: Record<string, string[]> = {
  北京: ['PKX', 'PEK'],
  上海: ['PVG', 'SHA'],
  武汉: ['WUH'],
  南京: ['NKG'],
  杭州: ['HGH'],
  深圳: ['SZX'],
  重庆: ['CKG'],
  广州: ['CAN'],
  成都: ['TFU'],
  长沙: ['CSX'],
  厦门: ['XMN'],
  西安: ['XIY'],
  天津: ['TSN'],
  青岛: ['TAO'],
}

const tonePalettes: Record<ToneKey, { label: string; bg: string; fg: string; accent: string; soft: string }> = {
  clean: { label: '清爽', bg: '#075985', fg: '#ffffff', accent: '#38bdf8', soft: '#dbeafe' },
  premium: { label: '高级', bg: '#111827', fg: '#f8fafc', accent: '#d4af37', soft: '#e5e7eb' },
  energetic: { label: '活力', bg: '#be185d', fg: '#ffffff', accent: '#fb923c', soft: '#fce7f3' },
  warm: { label: '温暖', bg: '#166534', fg: '#ffffff', accent: '#facc15', soft: '#dcfce7' },
}

const defaultCreative: CreativeState = {
  width: canvasWidth,
  height: canvasHeight,
  backgroundColor: tonePalettes.clean.bg,
  elements: [],
}

function cloneCreative(creative: CreativeState): CreativeState {
  return { ...creative, elements: creative.elements.map(element => ({ ...element })) }
}

function defaultWindow() {
  const startAt = new Date()
  const endAt = new Date()
  endAt.setDate(endAt.getDate() + 30)
  return { startAt: startAt.toISOString(), endAt: endAt.toISOString() }
}

function inferResourceLabel(resourceOptions: Array<{ value: string; label: string }>, resourceId: string) {
  return resourceOptions.find(option => option.value === resourceId)?.label ?? resourceId
}

function escapeSvgText(value: string) {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function makeImageDataUrl(prompt: string, tone: ToneKey, index: number) {
  const palette = tonePalettes[tone]
  const label = escapeSvgText(prompt.trim() || 'travel visual')
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="960" height="240" viewBox="0 0 960 240">
    <defs>
      <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
        <stop stop-color="${palette.bg}" offset="0"/>
        <stop stop-color="${palette.accent}" offset="1"/>
      </linearGradient>
    </defs>
    <rect width="960" height="240" fill="url(#g)"/>
    <circle cx="${720 + index * 24}" cy="${64 + index * 12}" r="92" fill="rgba(255,255,255,0.16)"/>
    <path d="M80 ${178 - index * 8} C250 108, 390 218, 560 ${120 + index * 12} S820 76, 920 ${138 - index * 6}" fill="none" stroke="rgba(255,255,255,0.38)" stroke-width="12" stroke-linecap="round"/>
    <text x="64" y="132" font-family="Arial, sans-serif" font-size="34" font-weight="800" fill="${palette.fg}">${label}</text>
  </svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

function buildTextCandidates(prompt: string, tone: ToneKey, resourceLabel: string): CreativeElement[] {
  const palette = tonePalettes[tone]
  const baseText = prompt.trim() || `${resourceLabel} 即刻出发`
  const snippets = [
    baseText,
    `${resourceLabel}，把旅程安排得更漂亮`,
    `${baseText} / ${palette.label}出行`,
    `选择 ${resourceLabel}`,
  ]

  return snippets.map((text, index) => ({
    id: `text-candidate-${Date.now()}-${index}`,
    type: 'text',
    text,
    x: index % 2 === 0 ? 56 : 520,
    y: index < 2 ? 48 : 138,
    width: index % 2 === 0 ? 460 : 340,
    height: index < 2 ? 58 : 46,
    fontSize: index < 2 ? 34 : 24,
    fontWeight: index === 1 ? 900 : 800,
    color: index === 2 ? palette.soft : palette.fg,
    backgroundColor: index === 3 ? 'rgba(255,255,255,0.16)' : 'transparent',
    opacity: 1,
    borderRadius: index === 3 ? 4 : 0,
    effect: index === 1 ? 'slideUp' : 'none',
  }))
}

function buildImageCandidates(prompt: string, tone: ToneKey): CreativeElement[] {
  return [0, 1, 2, 3].map(index => ({
    id: `image-candidate-${Date.now()}-${index}`,
    type: 'image',
    text: prompt.trim() || tonePalettes[tone].label,
    src: makeImageDataUrl(prompt, tone, index),
    x: index % 2 === 0 ? 0 : 520,
    y: index < 2 ? 0 : 72,
    width: index % 2 === 0 ? 960 : 360,
    height: index % 2 === 0 ? 240 : 132,
    fontSize: 18,
    fontWeight: 700,
    color: '#ffffff',
    backgroundColor: 'transparent',
    opacity: index % 2 === 0 ? 0.95 : 0.85,
    borderRadius: index % 2 === 0 ? 0 : 6,
    effect: 'fadeIn',
  }))
}

function renderCreativeSvg(creative: CreativeState) {
  const elements = creative.elements.map(element => {
    const opacity = Math.max(0, Math.min(1, element.opacity))
    if (element.type === 'image' && element.src) {
      return `<image href="${element.src}" x="${element.x}" y="${element.y}" width="${element.width}" height="${element.height}" preserveAspectRatio="xMidYMid slice" opacity="${opacity}" />`
    }

    if (element.backgroundColor !== 'transparent') {
      return `<rect x="${element.x}" y="${element.y}" width="${element.width}" height="${element.height}" rx="${element.borderRadius}" fill="${element.backgroundColor}" opacity="${opacity}" />
        <text x="${element.x + 18}" y="${element.y + element.height / 2 + element.fontSize / 3}" fill="${element.color}" font-size="${element.fontSize}" font-weight="${element.fontWeight}" font-family="Arial, sans-serif">${escapeSvgText(element.text)}</text>`
    }

    return `<text x="${element.x}" y="${element.y + element.fontSize}" fill="${element.color}" font-size="${element.fontSize}" font-weight="${element.fontWeight}" font-family="Arial, sans-serif" opacity="${opacity}">${escapeSvgText(element.text)}</text>`
  }).join('')

  return `<svg xmlns="http://www.w3.org/2000/svg" width="${creative.width}" height="${creative.height}" viewBox="0 0 ${creative.width} ${creative.height}"><rect width="100%" height="100%" fill="${creative.backgroundColor}" />${elements}</svg>`
}

function svgToFile(svg: string) {
  return new File([new Blob([svg], { type: 'image/svg+xml' })], `advertisement-${Date.now()}.svg`, { type: 'image/svg+xml' })
}

function getPrimaryCopy(creative: CreativeState) {
  const textElements = creative.elements.filter(element => element.type !== 'image').map(element => element.text.trim()).filter(Boolean)
  return {
    title: textElements[0] ?? '广告创意',
    subtitle: textElements[1] ?? textElements[0] ?? '精选推荐',
    ctaLabel: textElements.find(text => text.length <= 8) ?? '查看详情',
  }
}

export function AdvertisementSubmissionWorkspace({
  defaultPlacement,
  defaultTargetResourceType,
  resourceOptions,
  translate,
  onOpenResource,
  onShowNotice,
}: AdvertisementSubmissionWorkspaceProps) {
  const ownerAdvertisements = useAdvertisingStore(state => state.ownerAdvertisements)
  const loadOwnerAdvertisements = useAdvertisingStore(state => state.loadOwnerAdvertisements)
  const createAdvertisement = useAdvertisingStore(state => state.createAdvertisement)
  const uploadAdvertisementImage = useAdvertisingStore(state => state.uploadAdvertisementImage)
  const submitAdvertisementForReview = useAdvertisingStore(state => state.submitAdvertisementForReview)
  const pauseAdvertisement = useAdvertisingStore(state => state.pauseAdvertisement)
  const isLoading = useAdvertisingStore(state => state.isLoading)

  const [step, setStep] = useState<WorkflowStep>('kind')
  const [advertisementKind, setAdvertisementKind] = useState<AdvertisementKind>('ResourcePromotion')
  const [targetResourceId, setTargetResourceId] = useState(resourceOptions[0]?.value ?? '')
  const [creative, setCreative] = useState<CreativeState>(() => cloneCreative(defaultCreative))
  const [tone, setTone] = useState<ToneKey>('clean')
  const [textPrompt, setTextPrompt] = useState('')
  const [imagePrompt, setImagePrompt] = useState('')
  const [textCandidates, setTextCandidates] = useState<CreativeElement[]>([])
  const [imageCandidates, setImageCandidates] = useState<CreativeElement[]>([])
  const [dragTemplate, setDragTemplate] = useState<CreativeElement | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [dragState, setDragState] = useState<{ elementId: string; offsetX: number; offsetY: number } | null>(null)
  const [factoryPanelWidth, setFactoryPanelWidth] = useState(320)
  const [isResizingFactoryPanel, setIsResizingFactoryPanel] = useState(false)
  const splitContainerRef = useRef<HTMLDivElement | null>(null)
  const canvasRef = useRef<HTMLDivElement | null>(null)

  const [searchDraft, setSearchDraft] = useState({ departureCity: '', arrivalCity: '', departureDate: '', timeRange: 'all' })
  const selectedResourceLabel = inferResourceLabel(resourceOptions, targetResourceId)
  const filteredFlightOptions = useMemo(() => {
    if (defaultTargetResourceType !== 'Flight') return resourceOptions
    const departureCodes = searchDraft.departureCity ? cityAirportCodes[searchDraft.departureCity] ?? [] : []
    const arrivalCodes = searchDraft.arrivalCity ? cityAirportCodes[searchDraft.arrivalCity] ?? [] : []
    return resourceOptions.filter(option => {
      const label = option.label.toUpperCase()
      const matchesDeparture = departureCodes.length === 0 || departureCodes.some(code => label.includes(code))
      const matchesArrival = arrivalCodes.length === 0 || arrivalCodes.some(code => label.includes(code))
      return matchesDeparture && matchesArrival
    })
  }, [defaultTargetResourceType, resourceOptions, searchDraft.arrivalCity, searchDraft.departureCity])
  const reviewQueue = useMemo(
    () => ownerAdvertisements.filter(item => item.reviewStatus === 'PendingReview'),
    [ownerAdvertisements],
  )

  useEffect(() => {
    void loadOwnerAdvertisements()
  }, [loadOwnerAdvertisements])

  useEffect(() => {
    if (!targetResourceId && resourceOptions[0]) setTargetResourceId(resourceOptions[0].value)
  }, [resourceOptions, targetResourceId])

  useEffect(() => {
    if (!isResizingFactoryPanel) return

    function handleMouseMove(event: MouseEvent) {
      if (!splitContainerRef.current) return
      const rect = splitContainerRef.current.getBoundingClientRect()
      const nextWidth = Math.round(event.clientX - rect.left)
      const maxWidth = Math.max(300, Math.min(620, rect.width * 0.55))
      setFactoryPanelWidth(Math.max(260, Math.min(maxWidth, nextWidth)))
    }

    function handleMouseUp() {
      setIsResizingFactoryPanel(false)
    }

    window.addEventListener('mousemove', handleMouseMove)
    window.addEventListener('mouseup', handleMouseUp)
    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'

    return () => {
      window.removeEventListener('mousemove', handleMouseMove)
      window.removeEventListener('mouseup', handleMouseUp)
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
    }
  }, [isResizingFactoryPanel])

  function addTemplateToCanvas(template: CreativeElement, placement?: { x: number; y: number }) {
    const nextElement = {
      ...template,
      id: `${template.type}-${Date.now()}`,
      x: placement?.x ?? template.x,
      y: placement?.y ?? template.y,
    }
    setCreative(current => ({ ...current, backgroundColor: tonePalettes[tone].bg, elements: [...current.elements, nextElement] }))
  }

  function moveElement(elementId: string, x: number, y: number) {
    setCreative(current => ({
      ...current,
      elements: current.elements.map(element => element.id === elementId ? { ...element, x, y } : element),
    }))
  }

  async function submitAdvertisement() {
    setIsSubmitting(true)
    try {
      const svg = renderCreativeSvg(creative)
      const uploadedImage = await uploadAdvertisementImage(svgToFile(svg))
      const { startAt, endAt } = defaultWindow()
      const copy = getPrimaryCopy(creative)
      const targetId = advertisementKind === 'CompanyPromotion' ? `company-${defaultTargetResourceType.toLowerCase()}` : targetResourceId
      const resourceSummaryTitle = advertisementKind === 'CompanyPromotion' ? selectedResourceLabel || copy.title : selectedResourceLabel
      const createdAdvertisement = await createAdvertisement({
        advertisementKind,
        title: copy.title,
        subtitle: copy.subtitle,
        description: `${copy.title} ${copy.subtitle}`,
        imageUrl: uploadedImage.publicUrl,
        ctaLabel: copy.ctaLabel,
        targetResourceType: defaultTargetResourceType,
        targetResourceId: targetId,
        resourceSummaryTitle,
        landingTarget: null,
        placement: defaultPlacement,
        creativeJson: JSON.stringify(creative),
        creativeWidth: creative.width,
        creativeHeight: creative.height,
        priority: 50,
        startAt,
        endAt,
      })
      await submitAdvertisementForReview(createdAdvertisement.advertisementId)
      await loadOwnerAdvertisements()
      onShowNotice?.('success', translate('advertising.createSuccess'), translate('advertising.createSuccessDescription'))
      setStep('kind')
      setCreative(cloneCreative(defaultCreative))
      setTextCandidates([])
      setImageCandidates([])
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.submitEyebrow')}</p>
          <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.submitTitle')}</h2>
        </div>

        {step === 'kind' ? (
          <div className="grid gap-4 md:grid-cols-2">
            <button type="button" className="grid gap-3 border border-slate-200 bg-white p-6 text-left transition hover:border-slate-950" onClick={() => { setAdvertisementKind('ResourcePromotion'); setStep('resource') }}>
              <strong className="text-2xl">{translate('advertising.kind.resource')}</strong>
              <span className="text-sm leading-6 text-slate-500">{translate('advertising.kind.resourceDescription')}</span>
            </button>
            <button type="button" className="grid gap-3 border border-slate-200 bg-white p-6 text-left transition hover:border-slate-950" onClick={() => { setAdvertisementKind('CompanyPromotion'); setStep('creative') }}>
              <strong className="text-2xl">{translate('advertising.kind.company')}</strong>
              <span className="text-sm leading-6 text-slate-500">{translate('advertising.kind.companyDescription')}</span>
            </button>
          </div>
        ) : null}

        {step === 'resource' ? (
          <div className="grid gap-5">
            {defaultTargetResourceType === 'Flight' ? (
              <section className="grid gap-5 bg-slate-100 p-5">
                <div className="grid gap-4 xl:grid-cols-[1fr_1fr_1fr_1fr]">
                  <label>{translate('advertising.flight.departureCity')}<select value={searchDraft.departureCity} onChange={event => setSearchDraft(current => ({ ...current, departureCity: event.target.value }))}><option value="">{translate('advertising.flight.allCities')}</option>{flightCityOptions.map(city => <option key={city} value={city}>{city}</option>)}</select></label>
                  <label>{translate('advertising.flight.arrivalCity')}<select value={searchDraft.arrivalCity} onChange={event => setSearchDraft(current => ({ ...current, arrivalCity: event.target.value }))}><option value="">{translate('advertising.flight.allCities')}</option>{flightCityOptions.map(city => <option key={city} value={city}>{city}</option>)}</select></label>
                  <label>{translate('advertising.flight.departureDate')}<input type="date" value={searchDraft.departureDate} onChange={event => setSearchDraft(current => ({ ...current, departureDate: event.target.value }))} /></label>
                  <label>{translate('advertising.flight.timeRange')}<select value={searchDraft.timeRange} onChange={event => setSearchDraft(current => ({ ...current, timeRange: event.target.value }))}><option value="all">{translate('advertising.flight.allDay')}</option>{timeWindows.map(window => <option key={window} value={window}>{window}</option>)}</select></label>
                </div>
                <div className="grid gap-3">
                  {filteredFlightOptions.map(option => (
                    <button key={option.value} type="button" className={`grid gap-1 border p-4 text-left transition ${targetResourceId === option.value ? 'border-pink-500 bg-pink-50' : 'border-slate-200 bg-white hover:border-slate-950'}`} onClick={() => setTargetResourceId(option.value)}>
                      <strong>{option.label}</strong>
                      <span className="text-sm text-slate-500">{option.value}</span>
                    </button>
                  ))}
                </div>
              </section>
            ) : (
              <label>{translate('advertising.field.targetResource')}<select value={targetResourceId} onChange={event => setTargetResourceId(event.target.value)}>{resourceOptions.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}</select></label>
            )}
            <div className="flex flex-wrap gap-3">
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" onClick={() => setStep('kind')}>{translate('manager.back')}</button>
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600 disabled:opacity-50" disabled={!targetResourceId} onClick={() => setStep('creative')}>{translate('advertising.continueCreative')}</button>
            </div>
          </div>
        ) : null}

        {step === 'creative' ? (
          <div className="grid gap-5">
            <div
              ref={splitContainerRef}
              className="grid gap-0"
              style={{ gridTemplateColumns: `${factoryPanelWidth}px 12px minmax(0, 1fr)` }}
            >
              <aside className="grid content-start gap-4 border border-slate-200 bg-slate-50 p-4">
                <section className="grid gap-3">
                  <strong>{translate('advertising.factory.tone')}</strong>
                  <div className="grid grid-cols-2 gap-2">
                    {(Object.keys(tonePalettes) as ToneKey[]).map(key => (
                      <button key={key} type="button" className={`min-h-10 border px-3 text-sm font-semibold ${tone === key ? 'border-pink-500 bg-pink-50' : 'border-slate-200 bg-white'}`} onClick={() => setTone(key)}>{tonePalettes[key].label}</button>
                    ))}
                  </div>
                </section>

                <section className="grid gap-3 border-t border-slate-200 pt-4">
                  <strong>{translate('advertising.factory.text')}</strong>
                  <textarea rows={3} value={textPrompt} onChange={event => setTextPrompt(event.target.value)} placeholder={translate('advertising.factory.textPlaceholder')} />
                  <button type="button" className="inline-flex min-h-10 items-center justify-center border border-black bg-black px-3 text-sm font-semibold text-white" onClick={() => setTextCandidates(buildTextCandidates(textPrompt, tone, selectedResourceLabel))}>{translate('advertising.factory.generateText')}</button>
                  <div className="grid gap-2">
                    {textCandidates.map(candidate => (
                      <button key={candidate.id} type="button" draggable className="grid gap-1 border border-slate-200 bg-white p-3 text-left shadow-sm transition hover:border-slate-950" onDragStart={() => setDragTemplate(candidate)} onClick={() => addTemplateToCanvas(candidate)}>
                        <span className="text-xs font-bold text-slate-500">{translate('advertising.factory.preview')}</span>
                        <strong style={{ color: candidate.color === '#ffffff' ? tonePalettes[tone].bg : candidate.color }}>{candidate.text}</strong>
                      </button>
                    ))}
                  </div>
                </section>

                <section className="grid gap-3 border-t border-slate-200 pt-4">
                  <strong>{translate('advertising.factory.image')}</strong>
                  <textarea rows={3} value={imagePrompt} onChange={event => setImagePrompt(event.target.value)} placeholder={translate('advertising.factory.imagePlaceholder')} />
                  <button type="button" className="inline-flex min-h-10 items-center justify-center border border-black bg-black px-3 text-sm font-semibold text-white" onClick={() => setImageCandidates(buildImageCandidates(imagePrompt, tone))}>{translate('advertising.factory.generateImage')}</button>
                  <div className="grid gap-2">
                    {imageCandidates.map(candidate => (
                      <button key={candidate.id} type="button" draggable className="overflow-hidden border border-slate-200 bg-white text-left transition hover:border-slate-950" onDragStart={() => setDragTemplate(candidate)} onClick={() => addTemplateToCanvas(candidate)}>
                        {candidate.src ? <img src={candidate.src} alt="" className="aspect-video w-full object-cover" /> : null}
                      </button>
                    ))}
                  </div>
                </section>
              </aside>

              <div
                aria-label={translate('advertising.resizeFactoryPanel')}
                role="separator"
                className={`group flex cursor-col-resize items-stretch justify-center px-1 ${isResizingFactoryPanel ? 'bg-slate-100' : ''}`}
                onMouseDown={event => {
                  event.preventDefault()
                  setIsResizingFactoryPanel(true)
                }}
              >
                <span className={`my-1 w-1 rounded bg-slate-200 transition group-hover:bg-pink-400 ${isResizingFactoryPanel ? 'bg-pink-500' : ''}`} />
              </div>

              <main className="grid content-start gap-4">
                <div className="grid gap-3 border border-slate-200 bg-white p-4">
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-sm font-bold text-slate-500">{canvasWidth} x {canvasHeight}</span>
                    <span className="text-sm text-slate-500">{advertisementKind === 'CompanyPromotion' ? translate('advertising.kind.company') : selectedResourceLabel}</span>
                  </div>
                  <div
                    ref={canvasRef}
                    className="relative overflow-hidden border border-slate-300"
                    style={{ width: '100%', aspectRatio: `${canvasWidth} / ${canvasHeight}`, backgroundColor: creative.backgroundColor }}
                    onDragOver={event => event.preventDefault()}
                    onDrop={event => {
                      event.preventDefault()
                      if (!dragTemplate || !canvasRef.current) return
                      const rect = canvasRef.current.getBoundingClientRect()
                      const scale = canvasWidth / rect.width
                      addTemplateToCanvas(dragTemplate, {
                        x: Math.max(0, Math.round((event.clientX - rect.left) * scale)),
                        y: Math.max(0, Math.round((event.clientY - rect.top) * scale)),
                      })
                      setDragTemplate(null)
                    }}
                    onMouseMove={event => {
                      if (!dragState || !canvasRef.current) return
                      const rect = canvasRef.current.getBoundingClientRect()
                      const scale = canvasWidth / rect.width
                      moveElement(dragState.elementId, Math.max(0, Math.round((event.clientX - rect.left - dragState.offsetX) * scale)), Math.max(0, Math.round((event.clientY - rect.top - dragState.offsetY) * scale)))
                    }}
                    onMouseUp={() => setDragState(null)}
                    onMouseLeave={() => setDragState(null)}
                  >
                    {creative.elements.map(element => {
                      const scale = 100 / canvasWidth
                      const style = {
                        left: `${element.x * scale}%`,
                        top: `${element.y / canvasHeight * 100}%`,
                        width: `${element.width * scale}%`,
                        height: `${element.height / canvasHeight * 100}%`,
                        color: element.color,
                        backgroundColor: element.backgroundColor,
                        opacity: element.opacity,
                        borderRadius: element.borderRadius,
                        fontSize: `${Math.max(10, element.fontSize * 0.42)}px`,
                        fontWeight: element.fontWeight,
                      }
                      return (
                        <button
                          key={element.id}
                          type="button"
                          className="absolute grid place-items-center overflow-hidden border border-transparent text-left transition hover:border-white hover:ring-2 hover:ring-pink-500"
                          style={style}
                          onMouseDown={event => {
                            if (!canvasRef.current) return
                            const rect = canvasRef.current.getBoundingClientRect()
                            setDragState({ elementId: element.id, offsetX: event.clientX - rect.left - element.x / canvasWidth * rect.width, offsetY: event.clientY - rect.top - element.y / canvasHeight * rect.height })
                          }}
                        >
                          {element.type === 'image' && element.src ? <img src={element.src} alt="" className="h-full w-full object-cover" /> : element.text}
                        </button>
                      )
                    })}
                  </div>
                  <p className="m-0 text-sm leading-6 text-slate-500">{translate('advertising.factory.canvasHint')}</p>
                </div>
              </main>
            </div>

            <div className="flex flex-wrap gap-3">
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" onClick={() => setStep(advertisementKind === 'CompanyPromotion' ? 'kind' : 'resource')}>{translate('manager.back')}</button>
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-5 py-2 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600 disabled:opacity-50" disabled={isLoading || isSubmitting || creative.elements.length === 0} onClick={() => void submitAdvertisement()}>{isSubmitting ? translate('advertising.submitting') : translate('advertising.create')}</button>
            </div>
          </div>
        ) : null}
      </section>

      <AdvertisementListSection advertisements={ownerAdvertisements} reviewQueue={reviewQueue} translate={translate} onOpenResource={onOpenResource} onPause={pauseAdvertisement} onSubmitReview={submitAdvertisementForReview} />
    </section>
  )
}

type AdvertisementListSectionProps = {
  advertisements: AdvertisementResponse[]
  reviewQueue: AdvertisementResponse[]
  translate: (translationKey: string) => string
  onOpenResource: (resourceId: string) => void
  onPause: (advertisementId: string) => Promise<AdvertisementResponse>
  onSubmitReview: (advertisementId: string) => Promise<AdvertisementResponse>
}

function AdvertisementListSection({ advertisements, reviewQueue, translate, onOpenResource, onPause, onSubmitReview }: AdvertisementListSectionProps) {
  return (
    <>
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.myListEyebrow')}</p>
          <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.myListTitle')}</h3>
        </div>
        {advertisements.length === 0 ? <p className="text-sm leading-6 text-slate-500">{translate('advertising.empty')}</p> : (
          <div className="grid gap-3">
            {advertisements.map(advertisement => <AdvertisementAdminCard key={advertisement.advertisementId} advertisement={advertisement} translate={translate} onOpenResource={onOpenResource} onPause={onPause} onSubmitReview={onSubmitReview} />)}
          </div>
        )}
      </section>
      {reviewQueue.length > 0 ? (
        <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('advertising.pendingEyebrow')}</p>
            <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.pendingTitle')}</h3>
          </div>
          <div className="grid gap-3">
            {reviewQueue.map(advertisement => <AdvertisementAdminCard key={advertisement.advertisementId} advertisement={advertisement} translate={translate} onOpenResource={onOpenResource} onPause={onPause} onSubmitReview={onSubmitReview} />)}
          </div>
        </section>
      ) : null}
    </>
  )
}

type AdvertisementAdminCardProps = {
  advertisement: AdvertisementResponse
  translate: (translationKey: string) => string
  onOpenResource: (resourceId: string) => void
  onPause: (advertisementId: string) => Promise<AdvertisementResponse>
  onSubmitReview: (advertisementId: string) => Promise<AdvertisementResponse>
}

function AdvertisementAdminCard({ advertisement, translate, onOpenResource, onPause, onSubmitReview }: AdvertisementAdminCardProps) {
  return (
    <article className="grid gap-4 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50 md:grid-cols-[160px_1fr]">
      {advertisement.imageUrl ? <img src={advertisement.imageUrl} alt={advertisement.title} className="aspect-video w-full object-cover" /> : null}
      <div className="grid gap-3">
        <div className="grid gap-1">
          <strong>{advertisement.title}</strong>
          <span className="text-sm text-slate-500">{advertisement.resourceSummaryTitle}</span>
          <span className="text-sm font-semibold text-slate-700">{advertisement.advertisementKind} / {advertisement.reviewStatus} / {advertisement.deliveryStatus}</span>
          {advertisement.rejectionNote ? <span className="text-sm font-medium text-slate-500">{`${translate('advertising.rejectionNote')}: ${advertisement.rejectionNote}`}</span> : null}
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" onClick={() => onOpenResource(advertisement.targetResourceId)}>{translate('advertising.openResource')}</button>
          {advertisement.reviewStatus === 'Draft' || advertisement.reviewStatus === 'Rejected' ? <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" type="button" onClick={() => void onSubmitReview(advertisement.advertisementId)}>{translate('advertising.submitReview')}</button> : null}
          {advertisement.deliveryStatus === 'Active' ? <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" onClick={() => void onPause(advertisement.advertisementId)}>{translate('advertising.pause')}</button> : null}
        </div>
      </div>
    </article>
  )
}
