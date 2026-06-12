import { useEffect, useMemo, useRef, useState } from 'react'

import { useAdvertisingStore } from '@/app/stores/advertising-store'
import { toBackendAssetUrl } from '@/lib/presenters/view-models'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

type CanvasElementType = 'text' | 'image' | 'shape' | 'field'
type PlacementValue = 'FlightBookingPage' | 'HotelBookingPage' | 'TrainBookingPage' | 'AttractionBookingPage'
type TargetResourceType = 'Flight' | 'Hotel' | 'Train' | 'Attraction'
type WorkspaceTab = 'create' | 'drafts'
type ToneKey = 'clean' | 'premium' | 'energetic' | 'warm'
type FactoryMode = 'text' | 'image'
type VisualStyleKey =
  | 'cartoon'
  | 'realistic'
  | 'exaggerated'
  | 'minimal'
  | 'retro'
  | 'luxury'
  | 'futuristic'
  | 'dreamy'
  | 'editorial'
  | 'playful'
  | 'cinematic'
  | 'tech'
  | 'travel'
  | 'fashion'
type ImageFactoryKind = 'background' | 'element'
type ResizeDirection = 'n' | 'e' | 's' | 'w' | 'ne' | 'nw' | 'se' | 'sw'
type CanvasContextMenuState = {
  x: number
  y: number
  targetElementId: string | null
  canvasX: number | null
  canvasY: number | null
} | null

// 广告创作工作台组件参数，包含默认投放位、可选资源和回调函数。
type AdvertisementSubmissionWorkspaceProps = {
  defaultPlacement: PlacementValue
  defaultTargetResourceType: TargetResourceType
  resourceOptions: Array<{
    value: string
    label: string
    description?: string
    departureCity?: string
    arrivalCity?: string
    departureDate?: string
    timeRange?: string
  }>
  translate: (translationKey: string) => string
  onOpenResource: (resourceId: string) => void
  onShowNotice?: (kind: 'success' | 'error', title: string, description: string) => void
}

// 画布元素的统一数据结构，文本、图片、形状和字段都复用这一套。
type CreativeElement = {
  id: string
  type: CanvasElementType
  text: string
  src?: string
  contentMode?: 'cover' | 'contain'
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

// 当前创意画布的完整状态，包含尺寸、背景色和所有元素。
type CreativeState = {
  width: number
  height: number
  backgroundColor: string
  elements: CreativeElement[]
}

// 画布和候选数据的基础配置。
const canvasWidth = 960
const canvasHeight = 240
const timeWindows = ['00:00-03:59', '04:00-07:59', '08:00-11:59', '12:00-15:59', '16:00-19:59', '20:00-23:59']
const flightCityOptions = ['北京', '上海', '武汉', '南京', '杭州', '深圳', '重庆', '广州', '成都', '长沙', '厦门', '西安', '天津', '青岛']
// 画风色板，控制工作台生成文案和示意图的整体气质。
const tonePalettes: Record<ToneKey, { label: string; bg: string; fg: string; accent: string; soft: string }> = {
  clean: { label: '清爽', bg: '#075985', fg: '#ffffff', accent: '#38bdf8', soft: '#dbeafe' },
  premium: { label: '高级', bg: '#111827', fg: '#f8fafc', accent: '#d4af37', soft: '#e5e7eb' },
  energetic: { label: '活力', bg: '#be185d', fg: '#ffffff', accent: '#fb923c', soft: '#fce7f3' },
  warm: { label: '温暖', bg: '#166534', fg: '#ffffff', accent: '#facc15', soft: '#dcfce7' },
}

// 画风名称映射，用于页面下拉框和生成提示。
const visualStyleLabels: Record<VisualStyleKey, string> = {
  cartoon: '卡通',
  realistic: '写实',
  exaggerated: '夸张',
  minimal: '极简',
  retro: '复古',
  luxury: '高级感',
  futuristic: '未来感',
  dreamy: '梦幻',
  editorial: '杂志感',
  playful: '活泼',
  cinematic: '电影感',
  tech: '科技感',
  travel: '旅行感',
  fashion: '时尚',
}

// 默认创意内容，进入工作台时直接复用这份空白状态。
const defaultCreative: CreativeState = {
  width: canvasWidth,
  height: canvasHeight,
  backgroundColor: tonePalettes.clean.bg,
  elements: [],
}

// 深拷贝一份创意状态，避免直接修改原对象。
function cloneCreative(creative: CreativeState): CreativeState {
  return { ...creative, elements: creative.elements.map(element => ({ ...element })) }
}

// 把草稿 JSON 恢复成工作台可编辑的创意状态。
function parseCreativeJson(creativeJson: string | null | undefined): CreativeState | null {
  if (!creativeJson) {
    return null
  }

  try {
    const parsed = JSON.parse(creativeJson) as Partial<CreativeState>
    if (!parsed || !Array.isArray(parsed.elements)) {
      return null
    }
    return {
      width: typeof parsed.width === 'number' ? parsed.width : canvasWidth,
      height: typeof parsed.height === 'number' ? parsed.height : canvasHeight,
      backgroundColor: typeof parsed.backgroundColor === 'string' ? parsed.backgroundColor : tonePalettes.clean.bg,
      elements: parsed.elements.map((element, index) => ({
        id: typeof element.id === 'string' ? element.id : `restored-${Date.now()}-${index}`,
        type: element.type ?? 'text',
        text: element.text ?? '',
        src: element.src,
        contentMode: element.contentMode === 'contain' ? 'contain' : 'cover',
        x: typeof element.x === 'number' ? element.x : 0,
        y: typeof element.y === 'number' ? element.y : 0,
        width: typeof element.width === 'number' ? element.width : 240,
        height: typeof element.height === 'number' ? element.height : 80,
        fontSize: typeof element.fontSize === 'number' ? element.fontSize : 28,
        fontWeight: typeof element.fontWeight === 'number' ? element.fontWeight : 700,
        color: typeof element.color === 'string' ? element.color : '#ffffff',
        backgroundColor: typeof element.backgroundColor === 'string' ? element.backgroundColor : 'transparent',
        opacity: typeof element.opacity === 'number' ? element.opacity : 1,
        borderRadius: typeof element.borderRadius === 'number' ? element.borderRadius : 0,
        effect: element.effect ?? 'none',
      })),
    }
  } catch {
    return null
  }
}

// 默认投放窗口，起始时间从当前时间开始，结束时间往后推 30 天。
function defaultWindow() {
  const startAt = new Date()
  const endAt = new Date()
  endAt.setDate(endAt.getDate() + 30)
  return { startAt: startAt.toISOString(), endAt: endAt.toISOString() }
}

// 根据资源 ID 推断页面上展示的资源名称。
function inferResourceLabel(
  resourceOptions: Array<{
    value: string
    label: string
  }>,
  resourceId: string,
) {
  return resourceOptions.find(option => option.value === resourceId)?.label ?? resourceId
}

// 把普通文本转成可安全塞进 SVG 的内容。
function escapeSvgText(value: string) {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

// 把文本转成 SVG 属性可用的安全字符串。
function escapeSvgAttribute(value: string) {
  return escapeSvgText(value).replace(/"/g, '&quot;')
}

// 把 Blob 转成 data URL，供本地预览使用。
function blobToDataUrl(blob: Blob) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(typeof reader.result === 'string' ? reader.result : '')
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(blob)
  })
}

// 把远程图片地址转换成可直接嵌进画布的图片源。
async function makeEmbeddableImageSource(imageSource: string) {
  const normalizedSource = imageSource.trim()
  if (!normalizedSource) {
    return ''
  }

  if (normalizedSource.startsWith('data:image/')) {
    return normalizedSource
  }

  try {
    const response = await fetch(normalizedSource)
    if (!response.ok) {
      return ''
    }
    return await blobToDataUrl(await response.blob())
  } catch {
    return ''
  }
}

// 根据当前画风和背景模式生成一张 SVG 示例图。
function makeImageDataUrl(_prompt: string, tone: ToneKey, index: number, transparentBackground = false) {
  const palette = tonePalettes[tone]
  const backgroundLayer = transparentBackground ? '' : `<rect width="960" height="240" fill="url(#g)"/>`
  const subjectLayer = transparentBackground
    ? `<ellipse cx="480" cy="124" rx="130" ry="74" fill="${palette.accent}" fill-opacity="0.92"/>
    <path d="M360 136 C418 74, 540 76, 610 132" fill="none" stroke="rgba(255,255,255,0.68)" stroke-width="12" stroke-linecap="round"/>`
    : `<circle cx="${720 + index * 24}" cy="${64 + index * 12}" r="92" fill="rgba(255,255,255,0.16)"/>
    <path d="M80 ${178 - index * 8} C250 108, 390 218, 560 ${120 + index * 12} S820 76, 920 ${138 - index * 6}" fill="none" stroke="rgba(255,255,255,0.38)" stroke-width="12" stroke-linecap="round"/>`
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="960" height="240" viewBox="0 0 960 240">
    <defs>
      <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
        <stop stop-color="${palette.bg}" offset="0"/>
        <stop stop-color="${palette.accent}" offset="1"/>
      </linearGradient>
    </defs>
    ${backgroundLayer}
    ${subjectLayer}
  </svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

// 根据文字和画风生成一张独立的 SVG 艺术字示意图。
function makeTextArtDataUrl(text: string, tone: ToneKey) {
  const palette = tonePalettes[tone]
  const safeText = escapeSvgText(text.trim() || '广告标题')
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="720" height="220" viewBox="0 0 720 220">
    <defs>
      <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
        <feDropShadow dx="0" dy="8" stdDeviation="10" flood-color="${palette.bg}" flood-opacity="0.28"/>
      </filter>
    </defs>
    <g filter="url(#shadow)">
      <text x="360" y="120" text-anchor="middle" font-size="72" font-weight="900" font-family="Arial, PingFang SC, Microsoft YaHei, sans-serif" fill="${palette.fg}" stroke="${palette.accent}" stroke-width="6" paint-order="stroke fill">${safeText}</text>
      <text x="360" y="120" text-anchor="middle" font-size="72" font-weight="900" font-family="Arial, PingFang SC, Microsoft YaHei, sans-serif" fill="${palette.fg}">${safeText}</text>
    </g>
  </svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

// 生成文本候选元素，供工作台直接拖进画布。
function buildTextCandidates(prompt: string, tone: ToneKey, resourceLabel: string): CreativeElement[] {
  const baseText = prompt.trim() || `${resourceLabel} 即刻出发`
  return [{
    id: `text-candidate-${Date.now()}`,
    type: 'image',
    text: baseText,
    src: makeTextArtDataUrl(baseText, tone),
    contentMode: 'contain',
    x: 96,
    y: 44,
    width: 520,
    height: 160,
    fontSize: 34,
    fontWeight: 900,
    color: '#ffffff',
    backgroundColor: 'transparent',
    opacity: 1,
    borderRadius: 0,
    effect: 'fadeIn',
  }]
}

// 生成图片候选元素，背景图和独立元素共用。
function buildImageCandidates(prompt: string, tone: ToneKey, imageFactoryKind: ImageFactoryKind, transparentBackground = false): CreativeElement[] {
  return [0].map(index => ({
    id: `image-candidate-${Date.now()}-${index}`,
    type: 'image',
    text: prompt.trim() || tonePalettes[tone].label,
    src: makeImageDataUrl(prompt, tone, index, transparentBackground),
    contentMode: imageFactoryKind === 'background' ? 'cover' : 'contain',
    x: imageFactoryKind === 'background' ? (index % 2 === 0 ? 0 : 520) : (index % 2 === 0 ? 72 : 456),
    y: imageFactoryKind === 'background' ? (index < 2 ? 0 : 72) : (index < 2 ? 28 : 116),
    width: imageFactoryKind === 'background' ? (index % 2 === 0 ? 960 : 360) : 240,
    height: imageFactoryKind === 'background' ? (index % 2 === 0 ? 240 : 132) : 240,
    fontSize: 18,
    fontWeight: 700,
    color: '#ffffff',
    backgroundColor: 'transparent',
    opacity: index % 2 === 0 ? 0.95 : 0.85,
    borderRadius: index % 2 === 0 ? 0 : 6,
    effect: 'fadeIn',
  }))
}

// 本地生成图片失败时，用输入内容拼一个兜底标签。
function makeLocalImageFallbackLabel(input: {
  imagePrompt: string
  visualElementsPrompt: string
  focusPrompt: string
  selectedResourceLabel: string
  imageFactoryKind: ImageFactoryKind
}) {
  const primary = input.imagePrompt.trim() || input.visualElementsPrompt.trim() || input.focusPrompt.trim() || input.selectedResourceLabel.trim()
  if (!primary) return input.imageFactoryKind === 'element' ? '广告元素' : '广告背景'
  return primary.slice(0, 24)
}

// 把远程图片候选转换成画布可直接使用的元素。
function buildRemoteImageCandidates(
  candidates: Array<{ assetId: string; publicUrl: string; prompt: string; seed: number }>,
  tone: ToneKey,
  imageFactoryKind: ImageFactoryKind,
): CreativeElement[] {
  return candidates.map((candidate, index) => ({
    id: `image-candidate-${candidate.assetId}-${index}`,
    type: 'image',
    text: candidate.prompt,
    src: toBackendAssetUrl(candidate.publicUrl),
    contentMode: imageFactoryKind === 'background' ? 'cover' : 'contain',
    x: imageFactoryKind === 'background' ? (index % 2 === 0 ? 0 : 520) : (index % 2 === 0 ? 72 : 456),
    y: imageFactoryKind === 'background' ? (index < 2 ? 0 : 72) : (index < 2 ? 28 : 116),
    width: imageFactoryKind === 'background' ? (index % 2 === 0 ? 960 : 360) : 240,
    height: imageFactoryKind === 'background' ? (index % 2 === 0 ? 240 : 132) : 240,
    fontSize: 18,
    fontWeight: 700,
    color: '#ffffff',
    backgroundColor: 'transparent',
    opacity: index % 2 === 0 ? 0.95 : 0.85,
    borderRadius: index % 2 === 0 ? 0 : 6,
    effect: tone === 'premium' ? 'pulse' : 'fadeIn',
  }))
}

// 把远程文本候选转换成可直接编辑的图片元素。
function buildRemoteTextCandidates(
  candidates: Array<{ assetId: string; publicUrl: string; prompt: string; seed: number }>,
  sourceText: string,
): CreativeElement[] {
  return candidates.slice(0, 1).map(candidate => ({
    id: `text-candidate-${candidate.assetId}`,
    type: 'image',
    text: sourceText,
    src: toBackendAssetUrl(candidate.publicUrl),
    contentMode: 'contain',
    x: 96,
    y: 44,
    width: 520,
    height: 160,
    fontSize: 34,
    fontWeight: 900,
    color: '#ffffff',
    backgroundColor: 'transparent',
    opacity: 1,
    borderRadius: 0,
    effect: 'fadeIn',
  }))
}

async function renderCreativeSvg(creative: CreativeState) {
  const elements = await Promise.all(creative.elements.map(async element => {
    const opacity = Math.max(0, Math.min(1, element.opacity))
    if (element.type === 'image' && element.src) {
      const embeddedSource = await makeEmbeddableImageSource(element.src)
      if (!embeddedSource) {
        return ''
      }
      const preserveAspectRatio = element.contentMode === 'contain' ? 'xMidYMid meet' : 'xMidYMid slice'
      return `<image href="${escapeSvgAttribute(embeddedSource)}" x="${element.x}" y="${element.y}" width="${element.width}" height="${element.height}" preserveAspectRatio="${preserveAspectRatio}" opacity="${opacity}" />`
    }

    if (element.backgroundColor !== 'transparent') {
      return `<rect x="${element.x}" y="${element.y}" width="${element.width}" height="${element.height}" rx="${element.borderRadius}" fill="${element.backgroundColor}" opacity="${opacity}" />
        <text x="${element.x + 18}" y="${element.y + element.height / 2 + element.fontSize / 3}" fill="${element.color}" font-size="${element.fontSize}" font-weight="${element.fontWeight}" font-family="Arial, sans-serif">${escapeSvgText(element.text)}</text>`
    }

    return `<text x="${element.x}" y="${element.y + element.fontSize}" fill="${element.color}" font-size="${element.fontSize}" font-weight="${element.fontWeight}" font-family="Arial, sans-serif" opacity="${opacity}">${escapeSvgText(element.text)}</text>`
  }))

  return `<svg xmlns="http://www.w3.org/2000/svg" width="${creative.width}" height="${creative.height}" viewBox="0 0 ${creative.width} ${creative.height}"><rect width="100%" height="100%" fill="${escapeSvgAttribute(creative.backgroundColor)}" />${elements.join('')}</svg>`
}

function svgToFile(svg: string) {
  return new File([new Blob([svg], { type: 'image/svg+xml' })], `advertisement-${Date.now()}.svg`, { type: 'image/svg+xml' })
}

function getPrimaryCopy(creative: CreativeState) {
  const textElements = creative.elements.filter(element => element.type !== 'image').map(element => element.text.trim()).filter(Boolean)
  const imageTextFallback = creative.elements
    .find(element => element.type === 'image' && element.contentMode === 'contain' && element.text.trim())
    ?.text.trim()
  const primaryText = textElements[0] ?? imageTextFallback
  const secondaryText = textElements[1] ?? primaryText
  return {
    title: primaryText ?? '广告创意',
    subtitle: secondaryText ?? '精选推荐',
    ctaLabel: textElements.find(text => text.length <= 8) ?? '查看详情',
  }
}

function limitText(value: string, maxLength: number) {
  const normalized = value.trim()
  return normalized.length > maxLength ? normalized.slice(0, maxLength) : normalized
}

function upsertLocalAdvertisement(advertisements: AdvertisementResponse[], nextAdvertisement: AdvertisementResponse) {
  const filtered = advertisements.filter(advertisement => advertisement.advertisementId !== nextAdvertisement.advertisementId)
  return [nextAdvertisement, ...filtered].sort((left, right) => Date.parse(right.updatedAt) - Date.parse(left.updatedAt))
}

function mergeAdvertisements(primary: AdvertisementResponse[], secondary: AdvertisementResponse[]) {
  const byId = new Map<string, AdvertisementResponse>()
  secondary.forEach(advertisement => byId.set(advertisement.advertisementId, advertisement))
  primary.forEach(advertisement => byId.set(advertisement.advertisementId, advertisement))
  return [...byId.values()].sort((left, right) => Date.parse(right.updatedAt) - Date.parse(left.updatedAt))
}

function cursorForResizeDirection(direction: ResizeDirection) {
  if (direction === 'n' || direction === 's') return 'ns-resize'
  if (direction === 'e' || direction === 'w') return 'ew-resize'
  if (direction === 'ne' || direction === 'sw') return 'nesw-resize'
  return 'nwse-resize'
}

function resizeDirectionFromPointer(event: React.MouseEvent<HTMLElement>, rect: DOMRect): ResizeDirection | null {
  const edgeThreshold = Math.max(10, Math.min(18, Math.min(rect.width, rect.height) * 0.18))
  const nearLeft = event.clientX <= rect.left + edgeThreshold
  const nearRight = event.clientX >= rect.right - edgeThreshold
  const nearTop = event.clientY <= rect.top + edgeThreshold
  const nearBottom = event.clientY >= rect.bottom - edgeThreshold

  if (nearTop && nearLeft) return 'nw'
  if (nearTop && nearRight) return 'ne'
  if (nearBottom && nearLeft) return 'sw'
  if (nearBottom && nearRight) return 'se'
  if (nearTop) return 'n'
  if (nearRight) return 'e'
  if (nearBottom) return 's'
  if (nearLeft) return 'w'
  return null
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
  const updateAdvertisement = useAdvertisingStore(state => state.updateAdvertisement)
  const uploadAdvertisementImage = useAdvertisingStore(state => state.uploadAdvertisementImage)
  const generateAdvertisementImageCandidates = useAdvertisingStore(state => state.generateAdvertisementImageCandidates)
  const submitAdvertisementForReview = useAdvertisingStore(state => state.submitAdvertisementForReview)
  const isLoading = useAdvertisingStore(state => state.isLoading)

  const [workspaceTab, setWorkspaceTab] = useState<WorkspaceTab>('create')
  const [editingAdvertisementId, setEditingAdvertisementId] = useState<string | null>(null)
  const [localDraftAdvertisements, setLocalDraftAdvertisements] = useState<AdvertisementResponse[]>([])
  const [draftName, setDraftName] = useState('')
  const [draftNameInput, setDraftNameInput] = useState('')
  const [isDraftNameDialogOpen, setIsDraftNameDialogOpen] = useState(false)
  const [targetResourceId, setTargetResourceId] = useState(resourceOptions[0]?.value ?? '')
  const [creative, setCreative] = useState<CreativeState>(() => cloneCreative(defaultCreative))
  const [tone] = useState<ToneKey>('clean')
  const [factoryMode, setFactoryMode] = useState<FactoryMode>('text')
  const [focusPrompt, setFocusPrompt] = useState('')
  const [visualElementsPrompt, setVisualElementsPrompt] = useState('')
  const [avoidPrompt, setAvoidPrompt] = useState('')
  const [textPrompt, setTextPrompt] = useState('')
  const [textVisualStyles, setTextVisualStyles] = useState<VisualStyleKey[]>(['realistic'])
  const [imagePrompt, setImagePrompt] = useState('')
  const [imageFactoryKind, setImageFactoryKind] = useState<ImageFactoryKind>('background')
  const [shouldCutoutImageElement, setShouldCutoutImageElement] = useState(true)
  const [imageVisualStyles, setImageVisualStyles] = useState<VisualStyleKey[]>(['realistic'])
  const [backgroundAutoFit, setBackgroundAutoFit] = useState(true)
  const [textCandidates, setTextCandidates] = useState<CreativeElement[]>([])
  const [imageCandidates, setImageCandidates] = useState<CreativeElement[]>([])
  const [isGeneratingText, setIsGeneratingText] = useState(false)
  const [isGeneratingImages, setIsGeneratingImages] = useState(false)
  const [dragTemplate, setDragTemplate] = useState<CreativeElement | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [dragState, setDragState] = useState<{ elementId: string; offsetX: number; offsetY: number } | null>(null)
  const [resizeState, setResizeState] = useState<{
    elementId: string
    startClientX: number
    startClientY: number
    startX: number
    startY: number
    startWidth: number
    startHeight: number
    direction: ResizeDirection
  } | null>(null)
  const [hoverResizeDirectionByElementId, setHoverResizeDirectionByElementId] = useState<Record<string, ResizeDirection | null>>({})
  const [copiedElement, setCopiedElement] = useState<CreativeElement | null>(null)
  const [canvasContextMenu, setCanvasContextMenu] = useState<CanvasContextMenuState>(null)
  const [factoryPanelWidth, setFactoryPanelWidth] = useState(320)
  const [isResizingFactoryPanel, setIsResizingFactoryPanel] = useState(false)
  const splitContainerRef = useRef<HTMLDivElement | null>(null)
  const canvasRef = useRef<HTMLDivElement | null>(null)

  const [hyperlinkEnabled, setHyperlinkEnabled] = useState(false)
  const [searchDraft, setSearchDraft] = useState({ departureCity: '', arrivalCity: '', departureDate: '', timeRange: 'all' })
  const [flightSearchResults, setFlightSearchResults] = useState<typeof resourceOptions>([])
  const [hasSearchedFlights, setHasSearchedFlights] = useState(false)
  const selectedResourceLabel = inferResourceLabel(resourceOptions, targetResourceId)
  const draftAdvertisements = useMemo(
    () => mergeAdvertisements(ownerAdvertisements, localDraftAdvertisements),
    [localDraftAdvertisements, ownerAdvertisements],
  )

  useEffect(() => {
    void loadOwnerAdvertisements()
  }, [loadOwnerAdvertisements])

  useEffect(() => {
    if (!targetResourceId && resourceOptions[0]) setTargetResourceId(resourceOptions[0].value)
  }, [resourceOptions, targetResourceId])

  useEffect(() => {
    if (defaultTargetResourceType !== 'Flight') {
      return
    }
    setFlightSearchResults([])
    setHasSearchedFlights(false)
  }, [resourceOptions, defaultTargetResourceType])

  useEffect(() => {
    setCreative(current => current.backgroundColor === tonePalettes[tone].bg ? current : { ...current, backgroundColor: tonePalettes[tone].bg })
  }, [tone])

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

  useEffect(() => {
    if (!dragState && !resizeState) return

    function handleMouseMove(event: MouseEvent) {
      if (!canvasRef.current) return
      const rect = canvasRef.current.getBoundingClientRect()
      const scale = canvasWidth / rect.width

      if (dragState) {
        moveElement(
          dragState.elementId,
          Math.round((event.clientX - rect.left - dragState.offsetX) * scale),
          Math.round((event.clientY - rect.top - dragState.offsetY) * scale),
        )
      }

      if (resizeState) {
        const widthDelta = Math.round((event.clientX - resizeState.startClientX) * scale)
        const heightDelta = Math.round((event.clientY - resizeState.startClientY) * scale)
        const direction = resizeState.direction
        let nextX = resizeState.startX
        let nextY = resizeState.startY
        let nextWidth = resizeState.startWidth
        let nextHeight = resizeState.startHeight

        if (direction.includes('e')) {
          nextWidth = resizeState.startWidth + widthDelta
        }
        if (direction.includes('s')) {
          nextHeight = resizeState.startHeight + heightDelta
        }
        if (direction.includes('w')) {
          nextX = resizeState.startX + widthDelta
          nextWidth = resizeState.startWidth - widthDelta
        }
        if (direction.includes('n')) {
          nextY = resizeState.startY + heightDelta
          nextHeight = resizeState.startHeight - heightDelta
        }

        const minWidth = 40
        const minHeight = 32
        if (nextWidth < minWidth) {
          if (direction.includes('w')) nextX -= minWidth - nextWidth
          nextWidth = minWidth
        }
        if (nextHeight < minHeight) {
          if (direction.includes('n')) nextY -= minHeight - nextHeight
          nextHeight = minHeight
        }

        nextWidth = Math.max(minWidth, nextWidth)
        nextHeight = Math.max(minHeight, nextHeight)

        resizeElement(
          resizeState.elementId,
          nextX,
          nextY,
          nextWidth,
          nextHeight,
        )
      }
    }

    function handleMouseUp() {
      setDragState(null)
      setResizeState(null)
    }

    window.addEventListener('mousemove', handleMouseMove)
    window.addEventListener('mouseup', handleMouseUp)
    document.body.style.userSelect = 'none'
    document.body.style.cursor = resizeState ? cursorForResizeDirection(resizeState.direction) : 'grabbing'

    return () => {
      window.removeEventListener('mousemove', handleMouseMove)
      window.removeEventListener('mouseup', handleMouseUp)
      document.body.style.userSelect = ''
      document.body.style.cursor = ''
    }
  }, [dragState, resizeState])

  useEffect(() => {
    if (!canvasContextMenu) return

    function closeContextMenu() {
      setCanvasContextMenu(null)
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') closeContextMenu()
    }

    window.addEventListener('click', closeContextMenu)
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      window.removeEventListener('click', closeContextMenu)
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [canvasContextMenu])

  // 把一个候选模板落到当前画布上。
  function addTemplateToCanvas(template: CreativeElement, placement?: { x: number; y: number }) {
    const nextElement = {
      ...template,
      id: `${template.type}-${Date.now()}`,
      x: placement?.x ?? template.x,
      y: placement?.y ?? template.y,
    }
    setCreative(current => ({ ...current, backgroundColor: tonePalettes[tone].bg, elements: [...current.elements, nextElement] }))
  }

  // 把鼠标坐标换算成画布内坐标。
  function canvasPointFromMouseEvent(event: React.MouseEvent<HTMLElement>) {
    if (!canvasRef.current) {
      return { canvasX: null, canvasY: null }
    }
    const rect = canvasRef.current.getBoundingClientRect()
    const scale = canvasWidth / rect.width
    return {
      canvasX: Math.round((event.clientX - rect.left) * scale),
      canvasY: Math.round((event.clientY - rect.top) * scale),
    }
  }

  // 打开画布右键菜单，并记录当前操作的元素。
  function openCanvasContextMenu(event: React.MouseEvent<HTMLElement>, targetElementId: string | null) {
    event.preventDefault()
    event.stopPropagation()
    const point = canvasPointFromMouseEvent(event)
    setCanvasContextMenu({
      x: event.clientX,
      y: event.clientY,
      targetElementId,
      canvasX: point.canvasX,
      canvasY: point.canvasY,
    })
  }

  // 复制当前画布里的元素。
  function copyCanvasElement(elementId: string) {
    const element = creative.elements.find(currentElement => currentElement.id === elementId)
    if (!element) return
    setCopiedElement({ ...element })
    setCanvasContextMenu(null)
  }

  // 剪切当前画布里的元素。
  function cutCanvasElement(elementId: string) {
    const element = creative.elements.find(currentElement => currentElement.id === elementId)
    if (!element) return
    setCopiedElement({ ...element })
    removeElement(elementId)
    setCanvasContextMenu(null)
  }

  // 把刚复制或剪切的元素粘贴回画布。
  function pasteCanvasElement() {
    if (!copiedElement) return
    const pastedWidth = copiedElement.width
    const pastedHeight = copiedElement.height
    const requestedX = canvasContextMenu?.canvasX ?? copiedElement.x + 24
    const requestedY = canvasContextMenu?.canvasY ?? copiedElement.y + 24
    const nextElement: CreativeElement = {
      ...copiedElement,
      id: `${copiedElement.type}-${Date.now()}`,
      width: pastedWidth,
      height: pastedHeight,
      x: requestedX,
      y: requestedY,
    }
    setCreative(current => ({ ...current, elements: [...current.elements, nextElement] }))
    setCanvasContextMenu(null)
  }

  // 拖拽后更新元素位置。
  function moveElement(elementId: string, x: number, y: number) {
    setCreative(current => ({
      ...current,
      elements: current.elements.map(element => element.id === elementId ? { ...element, x, y } : element),
    }))
  }

  // 调整元素尺寸时同步更新宽高和字体大小。
  function resizeElement(elementId: string, x: number, y: number, width: number, height: number) {
    setCreative(current => ({
      ...current,
      elements: current.elements.map(element =>
        element.id === elementId
          ? {
              ...element,
              x,
              y,
              width,
              height,
              fontSize: element.type === 'image' ? element.fontSize : Math.max(18, Math.round(height * 0.42)),
            }
          : element,
      ),
    }))
  }

  // 从画布中删除一个元素。
  function removeElement(elementId: string) {
    setCreative(current => ({
      ...current,
      elements: current.elements.filter(element => element.id !== elementId),
    }))
  }

  // 摘要当前画布内容，给图片生成接口当上下文提示。
  function describeCanvasContent() {
    const textSummary = creative.elements
      .filter(element => element.type !== 'image')
      .map(element => element.text.trim())
      .filter(Boolean)
      .slice(0, 3)
      .join(' / ')
    const imageCount = creative.elements.filter(element => element.type === 'image').length
    return [textSummary ? `current text: ${textSummary}` : null, imageCount > 0 ? `current element images: ${imageCount}` : null]
      .filter(Boolean)
      .join(', ')
  }

  // 切换一个画风标签的选中状态。
  function toggleStyleSelection(
    styles: VisualStyleKey[],
    nextStyle: VisualStyleKey,
    updateStyles: (styles: VisualStyleKey[]) => void,
  ) {
    if (styles.includes(nextStyle)) {
      const nextStyles = styles.filter(style => style !== nextStyle)
      updateStyles(nextStyles.length > 0 ? nextStyles : [nextStyle])
      return
    }

    updateStyles([...styles, nextStyle])
  }

  // 更新航班搜索条件，并重置已搜索标记。
  function updateSearchDraftField(nextField: Partial<typeof searchDraft>) {
    setSearchDraft(current => ({ ...current, ...nextField }))
    setHasSearchedFlights(false)
  }

  // 根据筛选条件搜索可跳转的航班资源。
  function runFlightSearch() {
    if (defaultTargetResourceType !== 'Flight') {
      return
    }
    const nextResults = resourceOptions.filter(option => {
      const matchesDeparture = !searchDraft.departureCity || option.departureCity === searchDraft.departureCity
      const matchesArrival = !searchDraft.arrivalCity || option.arrivalCity === searchDraft.arrivalCity
      const matchesDate = !searchDraft.departureDate || option.departureDate === searchDraft.departureDate
      const matchesTimeRange = searchDraft.timeRange === 'all' || option.timeRange === searchDraft.timeRange
      return matchesDeparture && matchesArrival && matchesDate && matchesTimeRange
    })
    setFlightSearchResults(nextResults)
    setHasSearchedFlights(true)
    if (nextResults.length > 0) {
      setTargetResourceId(current => nextResults.some(option => option.value === current) ? current : nextResults[0].value)
    }
  }

  // 把当前草稿和画布状态组装成后端广告提交参数。
  function buildAdvertisementPayload(uploadedImageUrl: string) {
    const { startAt, endAt } = defaultWindow()
    const copy = getPrimaryCopy(creative)
    const advertisementName = draftName.trim() || copy.title
    const fallbackTargetId = targetResourceId || resourceOptions[0]?.value || `${defaultTargetResourceType.toLowerCase()}-draft`
    const targetId = hyperlinkEnabled ? fallbackTargetId : fallbackTargetId
    const resourceSummaryTitle = hyperlinkEnabled ? (selectedResourceLabel || advertisementName) : advertisementName
    const landingTarget =
      hyperlinkEnabled && defaultTargetResourceType === 'Flight'
        ? `flight:${fallbackTargetId}`
        : 'disabled'

    return {
      advertisementKind: 'ResourcePromotion',
      title: limitText(advertisementName, 120),
      subtitle: limitText(copy.subtitle, 120),
      description: limitText(`${copy.title} ${copy.subtitle}`.trim(), 1200),
      imageUrl: uploadedImageUrl,
      ctaLabel: limitText(copy.ctaLabel, 24),
      targetResourceType: defaultTargetResourceType,
      targetResourceId: targetId,
      resourceSummaryTitle: limitText(resourceSummaryTitle, 160),
      landingTarget,
      placement: defaultPlacement,
      creativeJson: JSON.stringify(creative),
      creativeWidth: creative.width,
      creativeHeight: creative.height,
      priority: 50,
      startAt,
      endAt,
    }
  }

  // 清空创作区，重新回到一张白纸。
  function resetComposer() {
    setEditingAdvertisementId(null)
    setDraftName('')
    setDraftNameInput('')
    setTargetResourceId(resourceOptions[0]?.value ?? '')
    setHyperlinkEnabled(false)
    setCreative(cloneCreative(defaultCreative))
    setFactoryMode('text')
    setFocusPrompt('')
    setVisualElementsPrompt('')
    setAvoidPrompt('')
    setTextPrompt('')
    setImagePrompt('')
    setTextVisualStyles(['realistic'])
    setImageVisualStyles(['realistic'])
    setImageFactoryKind('background')
    setShouldCutoutImageElement(true)
    setBackgroundAutoFit(true)
    setTextCandidates([])
    setImageCandidates([])
    setSearchDraft({ departureCity: '', arrivalCity: '', departureDate: '', timeRange: 'all' })
    setFlightSearchResults([])
    setHasSearchedFlights(false)
  }

  // 载入一条已有草稿到当前编辑器。
  function openAdvertisementDraft(advertisement: AdvertisementResponse) {
    const restoredCreative = parseCreativeJson(advertisement.creativeJson)
    setEditingAdvertisementId(advertisement.advertisementId)
    setDraftName(advertisement.title)
    setDraftNameInput(advertisement.title)
    setTargetResourceId(advertisement.targetResourceId)
    setHyperlinkEnabled(advertisement.landingTarget !== 'disabled')
    setCreative(restoredCreative ? cloneCreative(restoredCreative) : cloneCreative(defaultCreative))
    setTextPrompt(advertisement.title)
    setImagePrompt(advertisement.landingTarget === 'disabled' ? '' : advertisement.resourceSummaryTitle)
    setFlightSearchResults([])
    setHasSearchedFlights(false)
    setWorkspaceTab('create')
  }

  // 打开新建草稿弹窗。
  function openNewDraftDialog() {
    setDraftNameInput('')
    setIsDraftNameDialogOpen(true)
  }

  // 确认创建新草稿并进入创作态。
  function confirmNewDraft() {
    const nextName = draftNameInput.trim()
    if (!nextName) {
      onShowNotice?.('error', '需要广告名字', '先给这条广告起个名字，后面草稿页会用它来展示。')
      return
    }
    resetComposer()
    setDraftName(nextName)
    setDraftNameInput(nextName)
    setIsDraftNameDialogOpen(false)
    setWorkspaceTab('create')
  }

  // 调用文案生成接口，产出可拖拽的艺术字候选。
  async function generateTextStyles() {
    const linkedResourceLabel = hyperlinkEnabled ? selectedResourceLabel : ''
    const sourceText = textPrompt.trim() || linkedResourceLabel
    const styleRequirement = [
      textVisualStyles.length > 0 ? `画风：${textVisualStyles.map(style => visualStyleLabels[style]).join('、')}` : null,
    ].filter(Boolean).join('；') || '做成适合广告横幅的艺术字体'

    const textSource = [
      `我希望得到当前文本：“${sourceText}”的艺术字体。`,
      `要求：“${styleRequirement}”。`,
      '输出内容必须包含这句完整文字。',
      '只生成独立艺术字元素，不要生成整张海报。',
      '除文字本身和紧贴文字的少量装饰笔触外，其余全部做成透明背景并抠掉。',
      '不要出现天空、地面、色块底板、相框、人物、动物、建筑、风景、按钮、贴纸边框。',
      '文字四周必须留出完整边界，不能裁切，不能超出画面。',
      '这是一个可拖拽到广告画布里的独立文字元素。',
    ].join('')
    setIsGeneratingText(true)
    try {
      const response = await generateAdvertisementImageCandidates({
        prompt: textSource,
        supportingCopy: null,
        tone: tonePalettes[tone].label,
        resourceLabel: linkedResourceLabel || sourceText,
        advertisementKind: 'ResourcePromotion',
        imageFactoryKind: 'element',
        transparentBackground: true,
        width: 720,
        height: 220,
        candidateCount: 1,
        avoidText: [avoidPrompt.trim(), '不要改写文字，不要漏字，不要错别字'].filter(Boolean).join('；'),
      })
      if (response.candidates.length > 0) {
        setTextCandidates(buildRemoteTextCandidates(response.candidates, sourceText))
        return
      }
      setTextCandidates(buildTextCandidates(sourceText, tone, linkedResourceLabel))
    } catch (error) {
      setTextCandidates(buildTextCandidates(sourceText, tone, linkedResourceLabel))
      onShowNotice?.('error', translate('advertising.factory.text'), error instanceof Error ? error.message : '文字生成失败，已切换为本地文案预览。')
    } finally {
      setIsGeneratingText(false)
    }
  }

  // 调用图片生成接口，产出可拖拽的图片候选。
  async function generateImageStyles(styleOverride?: VisualStyleKey[]) {
    const activeStyles = styleOverride ?? imageVisualStyles
    const linkedResourceLabel = hyperlinkEnabled ? selectedResourceLabel : ''
    const canvasDescription = backgroundAutoFit ? describeCanvasContent() : ''
    const fallbackLabel = makeLocalImageFallbackLabel({
      imagePrompt,
      visualElementsPrompt,
      focusPrompt,
      selectedResourceLabel: linkedResourceLabel,
      imageFactoryKind,
    })
    const primaryPrompt = [
      imagePrompt.trim() || visualElementsPrompt.trim() || focusPrompt.trim() || linkedResourceLabel,
      `styles: ${activeStyles.map(style => visualStyleLabels[style]).join(', ')}`,
      imageFactoryKind === 'element' ? 'image usage: element' : 'image usage: background',
      imageFactoryKind === 'element' ? `cutout subject: ${shouldCutoutImageElement ? 'yes' : 'no'}` : null,
      imageFactoryKind === 'element' && shouldCutoutImageElement ? 'transparent background' : null,
      imageFactoryKind === 'background' && backgroundAutoFit && canvasDescription ? `fit around ${canvasDescription}` : null,
      imageFactoryKind === 'element' && shouldCutoutImageElement
        ? 'only keep the requested subject, isolate the main object, remove every background area, output a clean transparent cutout with full subject visible'
        : null,
    ].filter(Boolean).join(', ')
    const supportingCopy = [focusPrompt.trim(), visualElementsPrompt.trim()].filter(Boolean).join('，')
    setIsGeneratingImages(true)
    try {
      const response = await generateAdvertisementImageCandidates({
        prompt: primaryPrompt,
        supportingCopy: supportingCopy || null,
        tone: tonePalettes[tone].label,
        resourceLabel: linkedResourceLabel,
        advertisementKind: 'ResourcePromotion',
        imageFactoryKind,
        transparentBackground: imageFactoryKind === 'element' ? shouldCutoutImageElement : null,
        width: canvasWidth,
        height: canvasHeight,
        candidateCount: 1,
        avoidText: avoidPrompt.trim() || '不要在图片中直接生成文字、水印和 logo',
      })
      if (response.candidates.length > 0) {
        setImageCandidates(buildRemoteImageCandidates(response.candidates, tone, imageFactoryKind))
        return
      }
      setImageCandidates(buildImageCandidates(fallbackLabel, tone, imageFactoryKind, imageFactoryKind === 'element' && shouldCutoutImageElement))
    } catch (error) {
      setImageCandidates(buildImageCandidates(fallbackLabel, tone, imageFactoryKind, imageFactoryKind === 'element' && shouldCutoutImageElement))
      onShowNotice?.('error', translate('advertising.factory.image'), error instanceof Error ? error.message : '图片生成失败，已切换为本地预览图。')
    } finally {
      setIsGeneratingImages(false)
    }
  }

  // 保存当前草稿，必要时同步提交审核。
  async function saveDraft(submitForReview = false) {
    setIsSubmitting(true)
    try {
      const svg = await renderCreativeSvg(creative)
      const uploadedImage = await uploadAdvertisementImage(svgToFile(svg))
      const payload = buildAdvertisementPayload(uploadedImage.publicUrl)
      const savedAdvertisement = editingAdvertisementId
        ? await updateAdvertisement(editingAdvertisementId, payload)
        : await createAdvertisement(payload)
      setLocalDraftAdvertisements(current => upsertLocalAdvertisement(current, savedAdvertisement))
      if (submitForReview) {
        const submittedAdvertisement = await submitAdvertisementForReview(savedAdvertisement.advertisementId)
        setLocalDraftAdvertisements(current => upsertLocalAdvertisement(current, submittedAdvertisement))
      }
      await loadOwnerAdvertisements().catch(error => {
        console.warn('Failed to refresh advertisements after saving draft', error)
      })
      onShowNotice?.(
        'success',
        submitForReview ? translate('advertising.createSuccess') : '草稿已保存',
        submitForReview ? translate('advertising.createSuccessDescription') : '你可以继续在草稿页查看、编辑或撤稿。',
      )
      setWorkspaceTab('drafts')
      setEditingAdvertisementId(savedAdvertisement.advertisementId)
    } catch (error) {
      onShowNotice?.('error', '草稿保存失败', error instanceof Error ? error.message : '保存时出现未知错误。')
      throw error
    } finally {
      setIsSubmitting(false)
    }
  }

  // 撤回一条已经提交的广告，让它回到草稿状态。
  async function withdrawAdvertisement(advertisement: AdvertisementResponse) {
    const withdrawnAdvertisement = await updateAdvertisement(advertisement.advertisementId, {
      advertisementKind: advertisement.advertisementKind,
      title: advertisement.title,
      subtitle: advertisement.subtitle,
      description: advertisement.description,
      imageUrl: advertisement.imageUrl,
      ctaLabel: advertisement.ctaLabel,
      targetResourceType: advertisement.targetResourceType,
      targetResourceId: advertisement.targetResourceId,
      resourceSummaryTitle: advertisement.resourceSummaryTitle,
      landingTarget: advertisement.landingTarget,
      placement: advertisement.placement,
      creativeJson: advertisement.creativeJson,
      creativeWidth: advertisement.creativeWidth,
      creativeHeight: advertisement.creativeHeight,
      priority: advertisement.priority,
      startAt: advertisement.startAt,
      endAt: advertisement.endAt,
    })
    await loadOwnerAdvertisements().catch(error => {
      console.warn('Failed to refresh advertisements after withdrawing draft', error)
    })
    setLocalDraftAdvertisements(current => upsertLocalAdvertisement(current, withdrawnAdvertisement))
    onShowNotice?.('success', '已撤稿', '广告已恢复为未提交状态。')
  }

  // 提交草稿给网站管理者审核。
  async function submitDraftAdvertisement(advertisementId: string) {
    const submittedAdvertisement = await submitAdvertisementForReview(advertisementId)
    setLocalDraftAdvertisements(current => upsertLocalAdvertisement(current, submittedAdvertisement))
    await loadOwnerAdvertisements().catch(error => {
      console.warn('Failed to refresh advertisements after submitting draft', error)
    })
    onShowNotice?.('success', '已提交', '广告已提交给网站管理者审核。')
    return submittedAdvertisement
  }

  return (
    <section className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.submitEyebrow')}</p>
          <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.submitTitle')}</h2>
          {workspaceTab === 'create' && draftName ? <p className="m-0 mt-2 text-sm font-semibold text-slate-500">当前草稿：{draftName}</p> : null}
        </div>

        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            className={`inline-flex min-h-11 items-center justify-center border px-4 py-2 text-sm font-semibold transition ${workspaceTab === 'create' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-300 bg-white text-slate-950'}`}
            onClick={() => setWorkspaceTab('create')}
          >
            创作
          </button>
          <button
            type="button"
            className={`inline-flex min-h-11 items-center justify-center border px-4 py-2 text-sm font-semibold transition ${workspaceTab === 'drafts' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-300 bg-white text-slate-950'}`}
            onClick={() => setWorkspaceTab('drafts')}
          >
            查看草稿
          </button>
        </div>

      {workspaceTab === 'create' ? (
          <div className="grid gap-5">
            <section className="grid gap-4 border border-slate-200 bg-slate-50 p-4">
              <div className="flex flex-wrap gap-3">
                <button
                  type="button"
                  className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
                  onClick={openNewDraftDialog}
                >
                  新建草稿
                </button>
              </div>
            </section>

            <div
              ref={splitContainerRef}
              className="grid gap-0"
              style={{ gridTemplateColumns: `${factoryPanelWidth}px 12px minmax(0, 1fr)` }}
            >
              <aside className="grid content-start gap-4 border border-slate-200 bg-slate-50 p-4">
                <section className="hidden">
                </section>

                <section className="hidden">
                  <label className="grid gap-2">
                    <strong>想突出</strong>
                    <input value={focusPrompt} onChange={event => setFocusPrompt(event.target.value)} placeholder="比如：直飞、准点、品牌感、度假氛围" />
                  </label>
                  <label className="grid gap-2">
                    <strong>画面元素</strong>
                    <input value={visualElementsPrompt} onChange={event => setVisualElementsPrompt(event.target.value)} placeholder="比如：飞机侧影、云层、城市灯光、留白区域" />
                  </label>
                  <label className="grid gap-2">
                    <strong>不要出现</strong>
                    <input value={avoidPrompt} onChange={event => setAvoidPrompt(event.target.value)} placeholder="比如：人物、水印、深色背景、图片内置文字" />
                  </label>
                </section>

                <section className="grid gap-3">
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      type="button"
                      className={`inline-flex min-h-10 items-center justify-center border px-3 text-sm font-semibold ${factoryMode === 'text' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-200 bg-white text-slate-950'}`}
                      onClick={() => setFactoryMode('text')}
                    >
                      {translate('advertising.factory.text')}
                    </button>
                    <button
                      type="button"
                      className={`inline-flex min-h-10 items-center justify-center border px-3 text-sm font-semibold ${factoryMode === 'image' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-200 bg-white text-slate-950'}`}
                      onClick={() => setFactoryMode('image')}
                    >
                      {translate('advertising.factory.image')}
                    </button>
                  </div>

                  {factoryMode === 'text' ? (
                    <div className="grid gap-3">
                      <strong>{translate('advertising.factory.text')}</strong>
                      <textarea rows={3} value={textPrompt} onChange={event => setTextPrompt(event.target.value)} placeholder={translate('advertising.factory.textPlaceholder')} />
                      <label className="grid gap-2">
                        <span className="text-sm font-semibold text-slate-700">画风</span>
                        <div className="flex flex-wrap gap-2">
                          {(Object.keys(visualStyleLabels) as VisualStyleKey[]).map(styleKey => (
                            <button
                              key={styleKey}
                              type="button"
                              className={`inline-flex min-h-9 items-center justify-center border px-3 text-sm font-semibold ${textVisualStyles.includes(styleKey) ? 'border-pink-500 bg-pink-50 text-pink-700' : 'border-slate-200 bg-white text-slate-700'}`}
                              onClick={() => toggleStyleSelection(textVisualStyles, styleKey, setTextVisualStyles)}
                            >
                              {visualStyleLabels[styleKey]}
                            </button>
                          ))}
                        </div>
                      </label>
                      <div className="grid grid-cols-2 gap-2">
                        <button type="button" className="inline-flex min-h-10 items-center justify-center border border-black bg-black px-3 text-sm font-semibold text-white disabled:opacity-60" disabled={isGeneratingText} onClick={() => void generateTextStyles()}>
                          {isGeneratingText ? translate('search.loading') : translate('advertising.factory.generateText')}
                        </button>
                        <button
                          type="button"
                          className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-3 text-sm font-semibold text-slate-950 disabled:opacity-60"
                          disabled={isGeneratingText}
                          onClick={() => {
                            void generateTextStyles()
                          }}
                        >
                          切换风格
                        </button>
                      </div>
                      <div className="grid gap-2">
                        {textCandidates.map(candidate => (
                          <button key={candidate.id} type="button" draggable className="grid gap-1 border border-slate-200 bg-white p-3 text-left shadow-sm transition hover:border-slate-950" onDragStart={() => setDragTemplate(candidate)} onClick={() => addTemplateToCanvas(candidate)}>
                            <span className="text-xs font-bold text-slate-500">{translate('advertising.factory.preview')}</span>
                            {candidate.type === 'image' && candidate.src ? (
                              <img src={candidate.src} alt={candidate.text} className="max-h-40 w-full object-contain" />
                            ) : (
                              <strong style={{ color: candidate.color === '#ffffff' ? tonePalettes[tone].bg : candidate.color }}>{candidate.text}</strong>
                            )}
                            <span className="text-xs text-slate-500">艺术字方案</span>
                          </button>
                        ))}
                      </div>
                    </div>
                  ) : (
                    <div className="grid gap-3">
                      <strong>{translate('advertising.factory.image')}</strong>
                      <div className="grid grid-cols-2 gap-2">
                        <button
                          type="button"
                          className={`inline-flex min-h-10 items-center justify-center border px-3 text-sm font-semibold ${imageFactoryKind === 'background' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-200 bg-white text-slate-950'}`}
                          onClick={() => setImageFactoryKind('background')}
                        >
                          背景
                        </button>
                        <button
                          type="button"
                          className={`inline-flex min-h-10 items-center justify-center border px-3 text-sm font-semibold ${imageFactoryKind === 'element' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-200 bg-white text-slate-950'}`}
                          onClick={() => setImageFactoryKind('element')}
                        >
                          元素
                        </button>
                      </div>
                      <textarea rows={3} value={imagePrompt} onChange={event => setImagePrompt(event.target.value)} placeholder={translate('advertising.factory.imagePlaceholder')} />
                      {imageFactoryKind === 'element' ? (
                        <>
                          <label className="grid gap-2">
                            <span className="text-sm font-semibold text-slate-700">是否把内容抠出来</span>
                            <select value={shouldCutoutImageElement ? 'yes' : 'no'} onChange={event => setShouldCutoutImageElement(event.target.value === 'yes')}>
                              <option value="yes">是</option>
                              <option value="no">否</option>
                            </select>
                          </label>
                          <label className="grid gap-2">
                            <span className="text-sm font-semibold text-slate-700">画风</span>
                            <div className="flex flex-wrap gap-2">
                              {(Object.keys(visualStyleLabels) as VisualStyleKey[]).map(styleKey => (
                                <button
                                  key={styleKey}
                                  type="button"
                                  className={`inline-flex min-h-9 items-center justify-center border px-3 text-sm font-semibold ${imageVisualStyles.includes(styleKey) ? 'border-pink-500 bg-pink-50 text-pink-700' : 'border-slate-200 bg-white text-slate-700'}`}
                                  onClick={() => toggleStyleSelection(imageVisualStyles, styleKey, setImageVisualStyles)}
                                >
                                  {visualStyleLabels[styleKey]}
                                </button>
                              ))}
                            </div>
                          </label>
                        </>
                      ) : (
                        <>
                          <label className="grid gap-2">
                            <span className="text-sm font-semibold text-slate-700">画风</span>
                            <div className="flex flex-wrap gap-2">
                              {(Object.keys(visualStyleLabels) as VisualStyleKey[]).map(styleKey => (
                                <button
                                  key={styleKey}
                                  type="button"
                                  className={`inline-flex min-h-9 items-center justify-center border px-3 text-sm font-semibold ${imageVisualStyles.includes(styleKey) ? 'border-pink-500 bg-pink-50 text-pink-700' : 'border-slate-200 bg-white text-slate-700'}`}
                                  onClick={() => toggleStyleSelection(imageVisualStyles, styleKey, setImageVisualStyles)}
                                >
                                  {visualStyleLabels[styleKey]}
                                </button>
                              ))}
                            </div>
                          </label>
                          <label className="grid gap-2">
                            <span className="text-sm font-semibold text-slate-700">是否自动适应文字、元素</span>
                            <select value={backgroundAutoFit ? 'yes' : 'no'} onChange={event => setBackgroundAutoFit(event.target.value === 'yes')}>
                              <option value="yes">是</option>
                              <option value="no">否</option>
                            </select>
                          </label>
                        </>
                      )}
                      <div className="grid grid-cols-2 gap-2">
                        <button type="button" className="inline-flex min-h-10 items-center justify-center border border-black bg-black px-3 text-sm font-semibold text-white disabled:opacity-60" disabled={isGeneratingImages} onClick={() => void generateImageStyles()}>
                          {isGeneratingImages ? translate('search.loading') : translate('advertising.factory.generateImage')}
                        </button>
                        <button
                          type="button"
                          className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-3 text-sm font-semibold text-slate-950 disabled:opacity-60"
                          disabled={isGeneratingImages}
                          onClick={() => {
                            void generateImageStyles()
                          }}
                        >
                          切换风格
                        </button>
                      </div>
                      <div className="grid gap-2">
                        {imageCandidates.map(candidate => (
                          <button key={candidate.id} type="button" draggable className="overflow-hidden border border-slate-200 bg-white text-left transition hover:border-slate-950" onDragStart={() => setDragTemplate(candidate)} onClick={() => addTemplateToCanvas(candidate)}>
                            {candidate.src ? <img src={candidate.src} alt="" className={`aspect-video w-full ${candidate.contentMode === 'contain' ? 'object-contain bg-slate-50' : 'object-cover'}`} /> : null}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
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
                    <span className="text-sm text-slate-500">{hyperlinkEnabled ? selectedResourceLabel : '未添加超链接'}</span>
                  </div>
                  <div
                    ref={canvasRef}
                    className="relative overflow-hidden border border-slate-300"
                    style={{ width: '100%', aspectRatio: `${canvasWidth} / ${canvasHeight}`, backgroundColor: creative.backgroundColor }}
                    onContextMenu={event => openCanvasContextMenu(event, null)}
                    onDragOver={event => event.preventDefault()}
                    onDrop={event => {
                      event.preventDefault()
                      if (!dragTemplate || !canvasRef.current) return
                      const rect = canvasRef.current.getBoundingClientRect()
                      const scale = canvasWidth / rect.width
                      addTemplateToCanvas(dragTemplate, {
                        x: Math.round((event.clientX - rect.left) * scale),
                        y: Math.round((event.clientY - rect.top) * scale),
                      })
                      setDragTemplate(null)
                    }}
                  >
                    {creative.elements.map(element => {
                      const scale = 100 / canvasWidth
                      const hoverResizeDirection = hoverResizeDirectionByElementId[element.id]
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
                            if (dragState || resizeState) return
                            if ((event.target as HTMLElement).closest('[data-remove-handle="true"]')) {
                              setHoverResizeDirectionByElementId(current => current[element.id] ? { ...current, [element.id]: null } : current)
                              return
                            }
                            const direction = resizeDirectionFromPointer(event, event.currentTarget.getBoundingClientRect())
                            setHoverResizeDirectionByElementId(current =>
                              current[element.id] === direction ? current : { ...current, [element.id]: direction },
                            )
                          }}
                          onMouseLeave={() => {
                            setHoverResizeDirectionByElementId(current => current[element.id] ? { ...current, [element.id]: null } : current)
                          }}
                          onContextMenu={event => openCanvasContextMenu(event, element.id)}
                          onMouseDown={event => {
                            if (!canvasRef.current) return
                            if ((event.target as HTMLElement).closest('[data-remove-handle="true"]')) {
                              return
                            }
                            event.preventDefault()
                            const elementRect = event.currentTarget.getBoundingClientRect()
                            const resizeDirection = hoverResizeDirection ?? resizeDirectionFromPointer(event, elementRect)
                            if (resizeDirection) {
                              setResizeState({
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
                            const rect = canvasRef.current.getBoundingClientRect()
                            setDragState({ elementId: element.id, offsetX: event.clientX - rect.left - element.x / canvasWidth * rect.width, offsetY: event.clientY - rect.top - element.y / canvasHeight * rect.height })
                          }}
                        >
                          <button
                            type="button"
                            data-remove-handle="true"
                            className="absolute right-1 top-1 z-20 grid h-6 w-6 place-items-center rounded-full bg-black/70 text-xs font-bold text-white opacity-0 transition-opacity group-hover:opacity-100 focus:opacity-100"
                            onClick={() => removeElement(element.id)}
                          >
                            ×
                          </button>
                          {element.type === 'image' && element.src ? (
                            <img src={element.src} alt="" className={`h-full w-full ${element.contentMode === 'contain' ? 'object-contain' : 'object-cover'}`} />
                          ) : (
                            <div className="grid h-full w-full place-items-center px-3 text-center">{element.text}</div>
                          )}
                          <div className="pointer-events-none absolute inset-0 border border-transparent transition group-hover:border-white/70" />
                        </div>
                      )
                    })}
                    {canvasContextMenu ? (
                      <div
                        className="fixed z-50 min-w-32 border border-slate-200 bg-white p-1 text-sm font-semibold text-slate-950 shadow-xl shadow-slate-900/15"
                        style={{ left: canvasContextMenu.x, top: canvasContextMenu.y }}
                        onClick={event => event.stopPropagation()}
                        onContextMenu={event => event.preventDefault()}
                      >
                        {canvasContextMenu.targetElementId ? (
                          <>
                            <button
                              type="button"
                              className="block w-full px-3 py-2 text-left hover:bg-slate-100"
                              onClick={() => copyCanvasElement(canvasContextMenu.targetElementId!)}
                            >
                              复制
                            </button>
                            <button
                              type="button"
                              className="block w-full px-3 py-2 text-left hover:bg-slate-100"
                              onClick={() => cutCanvasElement(canvasContextMenu.targetElementId!)}
                            >
                              剪切
                            </button>
                          </>
                        ) : null}
                        <button
                          type="button"
                          className="block w-full px-3 py-2 text-left hover:bg-slate-100 disabled:cursor-not-allowed disabled:text-slate-400 disabled:hover:bg-white"
                          disabled={!copiedElement}
                          onClick={pasteCanvasElement}
                        >
                          粘贴
                        </button>
                      </div>
                    ) : null}
                  </div>
                  <p className="m-0 text-sm leading-6 text-slate-500">{translate('advertising.factory.canvasHint')}</p>
                </div>
              </main>
            </div>

            <section className="grid gap-4 border border-slate-200 bg-slate-50 p-4">
              <div className="flex items-center justify-between gap-3">
                <div className="grid gap-1">
                  <strong>超链接</strong>
                  <span className="text-sm leading-6 text-slate-500">这是可选功能。需要时再把广告跳到对应预订页，并只筛选这一条航班。</span>
                </div>
                <label className="inline-flex items-center gap-2 text-sm font-semibold text-slate-700">
                  <input type="checkbox" checked={hyperlinkEnabled} onChange={event => setHyperlinkEnabled(event.target.checked)} />
                  添加超链接
                </label>
              </div>

              {hyperlinkEnabled ? (
                defaultTargetResourceType === 'Flight' ? (
                  <section className="grid gap-4">
                    <div className="grid gap-4 xl:grid-cols-[1fr_1fr_1fr_1fr_auto]">
                      <label className="grid gap-2">
                        <span>{translate('advertising.flight.departureCity')}</span>
                        <select value={searchDraft.departureCity} onChange={event => updateSearchDraftField({ departureCity: event.target.value })}>
                          <option value="">{translate('advertising.flight.allCities')}</option>
                          {flightCityOptions.map(city => <option key={city} value={city}>{city}</option>)}
                        </select>
                      </label>
                      <label className="grid gap-2">
                        <span>{translate('advertising.flight.arrivalCity')}</span>
                        <select value={searchDraft.arrivalCity} onChange={event => updateSearchDraftField({ arrivalCity: event.target.value })}>
                          <option value="">{translate('advertising.flight.allCities')}</option>
                          {flightCityOptions.map(city => <option key={city} value={city}>{city}</option>)}
                        </select>
                      </label>
                      <label className="grid gap-2">
                        <span>{translate('advertising.flight.departureDate')}</span>
                        <input type="date" value={searchDraft.departureDate} onChange={event => updateSearchDraftField({ departureDate: event.target.value })} />
                      </label>
                      <label className="grid gap-2">
                        <span>{translate('advertising.flight.timeRange')}</span>
                        <select value={searchDraft.timeRange} onChange={event => updateSearchDraftField({ timeRange: event.target.value })}>
                          <option value="all">{translate('advertising.flight.allDay')}</option>
                          {timeWindows.map(window => <option key={window} value={window}>{window}</option>)}
                        </select>
                      </label>
                      <button
                        type="button"
                        className="inline-flex min-h-11 items-center justify-center self-end border border-slate-950 bg-slate-950 px-4 py-2 text-sm font-semibold text-white"
                        onClick={runFlightSearch}
                      >
                        搜索航班
                      </button>
                    </div>

                    {!hasSearchedFlights ? (
                      <p className="m-0 text-sm leading-6 text-slate-500">先填筛选条件，再点“搜索航班”，这里才会出现可选航班。</p>
                    ) : flightSearchResults.length === 0 ? (
                      <p className="m-0 text-sm leading-6 text-slate-500">没有找到符合条件的航班，换个日期、城市或时段再试试。</p>
                    ) : (
                      <div className="grid gap-3">
                        {flightSearchResults.map(option => (
                          <button
                            key={option.value}
                            type="button"
                            className={`grid gap-1 border p-4 text-left transition ${targetResourceId === option.value ? 'border-pink-500 bg-pink-50' : 'border-slate-200 bg-white hover:border-slate-950'}`}
                            onClick={() => setTargetResourceId(option.value)}
                          >
                            <strong>{option.label}</strong>
                            {option.description ? <span className="text-sm text-slate-500">{option.description}</span> : null}
                          </button>
                        ))}
                      </div>
                    )}
                  </section>
                ) : (
                  <label className="grid gap-2">
                    <span>{translate('advertising.field.targetResource')}</span>
                    <select value={targetResourceId} onChange={event => setTargetResourceId(event.target.value)}>
                      {resourceOptions.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
                    </select>
                  </label>
                )
              ) : (
                <p className="m-0 text-sm leading-6 text-slate-500">不添加也没关系，这张广告会按普通展示内容保存，不绑定跳转目标。</p>
              )}
            </section>

            <div className="flex flex-wrap gap-3">
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white disabled:opacity-50" disabled={isLoading || isSubmitting || creative.elements.length === 0} onClick={() => {
                if (!draftName.trim()) {
                  setIsDraftNameDialogOpen(true)
                  return
                }
                void saveDraft(false)
              }}>保存草稿</button>
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-5 py-2 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600 disabled:opacity-50" disabled={isLoading || isSubmitting || creative.elements.length === 0} onClick={() => {
                if (!draftName.trim()) {
                  setIsDraftNameDialogOpen(true)
                  return
                }
                void saveDraft(true)
              }}>{isSubmitting ? translate('advertising.submitting') : '提交给网站管理者'}</button>
            </div>
          </div>
        ) : null}
      </section>

      {isDraftNameDialogOpen ? (
        <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/40 px-4">
          <section className="grid w-full max-w-md gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-xl">
            <div className="grid gap-1">
              <strong className="text-xl">给广告起个名字</strong>
              <span className="text-sm leading-6 text-slate-500">这个名字会显示在草稿列表里，也会作为广告标题保存。</span>
            </div>
            <label className="grid gap-2">
              <span className="text-sm font-semibold text-slate-700">广告名字</span>
              <input
                autoFocus
                value={draftNameInput}
                onChange={event => setDraftNameInput(event.target.value)}
                onKeyDown={event => {
                  if (event.key === 'Enter') {
                    confirmNewDraft()
                  }
                }}
                maxLength={120}
                placeholder="比如：科比航空六月直飞广告"
              />
            </label>
            <div className="flex flex-wrap justify-end gap-3">
              <button type="button" className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" onClick={() => setIsDraftNameDialogOpen(false)}>取消</button>
              <button type="button" className="inline-flex min-h-10 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600" onClick={confirmNewDraft}>开始创作</button>
            </div>
          </section>
        </div>
      ) : null}

      {workspaceTab === 'drafts' ? (
        <AdvertisementDraftSection
          advertisements={draftAdvertisements}
          translate={translate}
          onEdit={openAdvertisementDraft}
          onOpenResource={onOpenResource}
          onWithdraw={withdrawAdvertisement}
          onSubmitReview={submitDraftAdvertisement}
        />
      ) : null}
    </section>
  )
}

// 草稿列表区块参数，负责展示用户已有广告草稿。
type AdvertisementDraftSectionProps = {
  advertisements: AdvertisementResponse[]
  translate: (translationKey: string) => string
  onEdit: (advertisement: AdvertisementResponse) => void
  onOpenResource: (resourceId: string) => void
  onWithdraw: (advertisement: AdvertisementResponse) => Promise<void>
  onSubmitReview: (advertisementId: string) => Promise<AdvertisementResponse>
}

// 草稿列表区块，负责把所有草稿按卡片方式展示出来。
function AdvertisementDraftSection({ advertisements, translate, onEdit, onOpenResource, onWithdraw, onSubmitReview }: AdvertisementDraftSectionProps) {
  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div>
        <p className="text-sm font-bold text-slate-500">我的广告</p>
        <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">查看草稿</h3>
      </div>
      {advertisements.length === 0 ? <p className="text-sm leading-6 text-slate-500">{translate('advertising.empty')}</p> : (
        <div className="grid gap-4">
          {advertisements.map(advertisement => (
            <AdvertisementAdminCard
              key={advertisement.advertisementId}
              advertisement={advertisement}
              translate={translate}
              onEdit={onEdit}
              onOpenResource={onOpenResource}
              onWithdraw={onWithdraw}
              onSubmitReview={onSubmitReview}
            />
          ))}
        </div>
      )}
    </section>
  )
}

// 单条广告草稿卡片参数。
type AdvertisementAdminCardProps = {
  advertisement: AdvertisementResponse
  translate: (translationKey: string) => string
  onEdit: (advertisement: AdvertisementResponse) => void
  onOpenResource: (resourceId: string) => void
  onWithdraw: (advertisement: AdvertisementResponse) => Promise<void>
  onSubmitReview: (advertisementId: string) => Promise<AdvertisementResponse>
}

// 单条广告草稿卡片，负责展示封面、状态和操作按钮。
function AdvertisementAdminCard({ advertisement, translate, onEdit, onOpenResource, onWithdraw, onSubmitReview }: AdvertisementAdminCardProps) {
  const isSubmitted = advertisement.reviewStatus !== 'Draft' && advertisement.reviewStatus !== 'Rejected'
  const hasHyperlink = advertisement.landingTarget !== 'disabled'
  return (
    <article className="grid gap-4 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
      {advertisement.imageUrl ? (
        <div className="overflow-hidden border border-slate-200 bg-slate-50">
          <BackendAssetImage
            assetUrl={advertisement.imageUrl}
            alt={advertisement.title}
            className="aspect-[960/240] w-full object-contain"
            fallbackContent={advertisement.title}
          />
        </div>
      ) : null}
      <div className="grid gap-3">
        <div className="grid gap-1">
          <strong>{advertisement.title}</strong>
          <span className="text-sm text-slate-500">{advertisement.resourceSummaryTitle}</span>
          <span className="text-sm font-semibold text-slate-700">{advertisement.advertisementKind} / {isSubmitted ? '已提交' : '未提交'} / {advertisement.deliveryStatus}</span>
          {advertisement.rejectionNote ? <span className="text-sm font-medium text-slate-500">{`${translate('advertising.rejectionNote')}: ${advertisement.rejectionNote}`}</span> : null}
        </div>
        {/* 这里的按钮分别用于编辑、跳转目标页、提交审核和撤稿。 */}
        <div className="flex flex-wrap items-center gap-3">
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" onClick={() => onEdit(advertisement)}>编辑</button>
          {hasHyperlink ? <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" onClick={() => onOpenResource(advertisement.targetResourceId)}>打开目标页</button> : null}
          {!isSubmitted ? <button className="inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600" type="button" onClick={() => void onSubmitReview(advertisement.advertisementId)}>提交</button> : null}
          {isSubmitted ? <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" onClick={() => void onWithdraw(advertisement)}>撤稿</button> : null}
        </div>
      </div>
    </article>
  )
}


