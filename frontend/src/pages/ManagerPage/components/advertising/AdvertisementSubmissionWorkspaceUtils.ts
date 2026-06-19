import { toBackendAssetUrl } from '@/lib/presenters/view-models'
import type { MouseEvent } from 'react'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementTextCandidateResponse } from '@/microservices/advertising/objects/AdvertisementTextCandidateResponse'

export type CanvasElementType = 'text' | 'image' | 'shape' | 'field'
export type PlacementValue = 'FlightBookingPage' | 'HotelBookingPage' | 'TrainBookingPage' | 'AttractionBookingPage'
export type TargetResourceType = 'Flight' | 'Hotel' | 'Train' | 'Attraction'
export type WorkspaceTab = 'create' | 'drafts'
export type ToneKey = 'clean' | 'premium' | 'energetic' | 'warm'
export type FactoryMode = 'text' | 'image'
export type VisualStyleKey =
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
export type ImageFactoryKind = 'background' | 'element'
export type ResizeDirection = 'n' | 'e' | 's' | 'w' | 'ne' | 'nw' | 'se' | 'sw'
export type CanvasContextMenuState = {
  x: number
  y: number
  targetElementId: string | null
  canvasX: number | null
  canvasY: number | null
} | null

export type AdvertisementSubmissionWorkspaceProps = {
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

export type CreativeElement = {
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

export type CreativeState = {
  width: number
  height: number
  backgroundColor: string
  elements: CreativeElement[]
}

export const canvasWidth = 960
export const canvasHeight = 240
export const timeWindows = ['00:00-03:59', '04:00-07:59', '08:00-11:59', '12:00-15:59', '16:00-19:59', '20:00-23:59']
export const flightCityOptions = ['北京', '上海', '武汉', '南京', '杭州', '深圳', '重庆', '广州', '成都', '长沙', '厦门', '西安', '天津', '青岛']
export const tonePalettes: Record<ToneKey, { label: string; bg: string; fg: string; accent: string; soft: string }> = {
  clean: { label: '清爽', bg: '#075985', fg: '#ffffff', accent: '#38bdf8', soft: '#dbeafe' },
  premium: { label: '高级', bg: '#111827', fg: '#f8fafc', accent: '#d4af37', soft: '#e5e7eb' },
  energetic: { label: '活力', bg: '#be185d', fg: '#ffffff', accent: '#fb923c', soft: '#fce7f3' },
  warm: { label: '温暖', bg: '#166534', fg: '#ffffff', accent: '#facc15', soft: '#dcfce7' },
}

export const visualStyleLabels: Record<VisualStyleKey, string> = {
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


export const defaultCreative: CreativeState = {
  width: canvasWidth,
  height: canvasHeight,
  backgroundColor: tonePalettes.clean.bg,
  elements: [],
}

export function cloneCreative(creative: CreativeState): CreativeState {
  return { ...creative, elements: creative.elements.map(element => ({ ...element })) }
}

export function parseCreativeJson(creativeJson: string | null | undefined): CreativeState | null {
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

export function defaultWindow() {
  const startAt = new Date()
  const endAt = new Date()
  endAt.setDate(endAt.getDate() + 30)
  return { startAt: startAt.toISOString(), endAt: endAt.toISOString() }
}

export function inferResourceLabel(
  resourceOptions: Array<{
    value: string
    label: string
  }>,
  resourceId: string,
) {
  return resourceOptions.find(option => option.value === resourceId)?.label ?? resourceId
}

export function escapeSvgText(value: string) {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

export function escapeSvgAttribute(value: string) {
  return escapeSvgText(value).replace(/"/g, '&quot;')
}

function normalizeStylePreference(preference: string) {
  return preference.trim().toLowerCase()
}

function resolveTextStyleColor(preference: string) {
  const normalized = normalizeStylePreference(preference)
  if (!normalized) {
    return null
  }

  const colorHints: Array<{
    keywords: string[]
    accent: string
    stroke: string
  }> = [
    { keywords: ['红', 'red', 'scarlet', 'crimson', 'ruby', 'rose'], accent: '#ef4444', stroke: '#991b1b' },
    { keywords: ['蓝', 'blue', 'azure', 'sky', 'navy', 'cobalt'], accent: '#3b82f6', stroke: '#1d4ed8' },
    { keywords: ['绿', 'green', 'emerald', 'jade', 'mint'], accent: '#10b981', stroke: '#047857' },
    { keywords: ['黄', 'gold', 'yellow', 'amber', 'sunny'], accent: '#f59e0b', stroke: '#b45309' },
    { keywords: ['橙', 'orange', 'tangerine'], accent: '#f97316', stroke: '#c2410c' },
    { keywords: ['紫', 'purple', 'violet', 'lavender', 'plum'], accent: '#8b5cf6', stroke: '#6d28d9' },
    { keywords: ['粉', 'pink', 'magenta', 'fuchsia', 'rose'], accent: '#ec4899', stroke: '#be185d' },
    { keywords: ['黑', 'black', 'charcoal', 'ink'], accent: '#111827', stroke: '#000000' },
    { keywords: ['白', 'white', 'ivory', 'snow'], accent: '#f8fafc', stroke: '#e2e8f0' },
    { keywords: ['金', 'golden', 'metallic', 'bronze'], accent: '#d4af37', stroke: '#8a6a12' },
  ]

  return colorHints.find(({ keywords }) => keywords.some(keyword => normalized.includes(keyword))) ?? null
}

export function blobToDataUrl(blob: Blob) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(typeof reader.result === 'string' ? reader.result : '')
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(blob)
  })
}

export async function makeEmbeddableImageSource(imageSource: string) {
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

export function makeImageDataUrl(_prompt: string, tone: ToneKey, index: number, transparentBackground = false) {
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

export function makeTextArtDataUrl(text: string, tone: ToneKey, variantSeed = 0, stylePreference = '') {
  const palette = tonePalettes[tone]
  const safeText = escapeSvgText(text.trim() || '广告创意图')
  const preferenceColor = resolveTextStyleColor(stylePreference)
  const paletteSwap = variantSeed % 3
  const accent = preferenceColor?.accent ?? (paletteSwap === 0 ? palette.accent : paletteSwap === 1 ? '#ffffff' : '#38bdf8')
  const stroke = preferenceColor?.stroke ?? (paletteSwap === 2 ? palette.bg : palette.accent)
  const shadowColor = preferenceColor?.accent ?? palette.bg
  const xOffset = 336 + (variantSeed % 4) * 10
  const yOffset = 116 + (variantSeed % 5) * 6
  const scale = 66 + (variantSeed % 3) * 4
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="720" height="220" viewBox="0 0 720 220">
    <defs>
      <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
        <feDropShadow dx="0" dy="8" stdDeviation="10" flood-color="${shadowColor}" flood-opacity="0.28"/>
      </filter>
    </defs>
    <g filter="url(#shadow)">
      <text x="${xOffset}" y="${yOffset}" text-anchor="middle" font-size="${scale}" font-weight="900" font-family="Arial, PingFang SC, Microsoft YaHei, sans-serif" fill="${palette.fg}" stroke="${stroke}" stroke-width="6" paint-order="stroke fill">${safeText}</text>
      <text x="${xOffset}" y="${yOffset}" text-anchor="middle" font-size="${scale}" font-weight="900" font-family="Arial, PingFang SC, Microsoft YaHei, sans-serif" fill="${accent}">${safeText}</text>
    </g>
  </svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

export function buildTextCandidates(prompt: string, tone: ToneKey, resourceLabel: string, variantSeed = 0, stylePreference = ''): CreativeElement[] {
  const baseText = prompt.trim() || `${resourceLabel} 文案创意`
  return [{
    id: `text-candidate-${Date.now()}-${variantSeed}`,
    type: 'text',
    text: baseText,
    src: makeTextArtDataUrl(baseText, tone, variantSeed, stylePreference),
    x: 96,
    y: 44,
    width: 560,
    height: 180,
    fontSize: 34,
    fontWeight: 900,
    color: tonePalettes[tone].fg,
    backgroundColor: 'transparent',
    opacity: 1,
    borderRadius: 0,
    effect: 'fadeIn',
  }]
}

export function buildImageCandidates(prompt: string, tone: ToneKey, imageFactoryKind: ImageFactoryKind, transparentBackground = false): CreativeElement[] {
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

export function makeLocalImageFallbackLabel(input: {
  imagePrompt: string
  visualElementsPrompt: string
  focusPrompt: string
  selectedResourceLabel: string
  imageFactoryKind: ImageFactoryKind
}) {
  const primary = input.imagePrompt.trim() || input.visualElementsPrompt.trim() || input.focusPrompt.trim() || input.selectedResourceLabel.trim()
  if (!primary) return input.imageFactoryKind === 'element' ? '广告元素图' : '广告背景图'
  return primary.slice(0, 24)
}

export function buildRemoteImageCandidates(
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

export function buildRemoteTextCandidates(
  candidates: AdvertisementTextCandidateResponse[],
  sourceText: string,
  tone: ToneKey,
  stylePreference = '',
): CreativeElement[] {
  return candidates.slice(0, 1).map((candidate, index) => ({
    id: `text-candidate-${candidate.seed}-${index}`,
    type: 'text',
    text: candidate.text?.trim() || sourceText,
    src: makeTextArtDataUrl(candidate.text?.trim() || sourceText, tone, candidate.seed, stylePreference),
    x: 96,
    y: 44,
    width: 560,
    height: 180,
    fontSize: 34,
    fontWeight: 900,
    color: '#0f172a',
    backgroundColor: 'transparent',
    opacity: 1,
    borderRadius: 0,
    effect: 'fadeIn',
  }))
}

export async function renderCreativeSvg(creative: CreativeState) {
  const elements = await Promise.all(creative.elements.map(async element => {
    const opacity = Math.max(0, Math.min(1, element.opacity))
    if (element.src) {
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

export function svgToFile(svg: string) {
  return new File([new Blob([svg], { type: 'image/svg+xml' })], `advertisement-${Date.now()}.svg`, { type: 'image/svg+xml' })
}

export function getPrimaryCopy(creative: CreativeState) {
  const textElements = creative.elements.filter(element => element.type !== 'image').map(element => element.text.trim()).filter(Boolean)
  const imageTextFallback = creative.elements
    .find(element => element.type === 'image' && element.contentMode === 'contain' && element.text.trim())
    ?.text.trim()
  const primaryText = textElements[0] ?? imageTextFallback
  const secondaryText = textElements[1] ?? primaryText
  return {
    title: primaryText ?? '创意标题',
    subtitle: secondaryText ?? '精选推荐',
    ctaLabel: textElements.find(text => text.length <= 8) ?? '查看详情',
  }
}

export function limitText(value: string, maxLength: number) {
  const normalized = value.trim()
  return normalized.length > maxLength ? normalized.slice(0, maxLength) : normalized
}

export function upsertLocalAdvertisement(advertisements: AdvertisementResponse[], nextAdvertisement: AdvertisementResponse) {
  const filtered = advertisements.filter(advertisement => advertisement.advertisementId !== nextAdvertisement.advertisementId)
  return [nextAdvertisement, ...filtered].sort((left, right) => Date.parse(right.updatedAt) - Date.parse(left.updatedAt))
}

export function mergeAdvertisements(primary: AdvertisementResponse[], secondary: AdvertisementResponse[]) {
  const byId = new Map<string, AdvertisementResponse>()
  secondary.forEach(advertisement => byId.set(advertisement.advertisementId, advertisement))
  primary.forEach(advertisement => byId.set(advertisement.advertisementId, advertisement))
  return [...byId.values()].sort((left, right) => Date.parse(right.updatedAt) - Date.parse(left.updatedAt))
}

export function cursorForResizeDirection(direction: ResizeDirection) {
  if (direction === 'n' || direction === 's') return 'ns-resize'
  if (direction === 'e' || direction === 'w') return 'ew-resize'
  if (direction === 'ne' || direction === 'sw') return 'nesw-resize'
  return 'nwse-resize'
}

export function resizeDirectionFromPointer(event: MouseEvent<HTMLElement>, rect: DOMRect): ResizeDirection | null {
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


