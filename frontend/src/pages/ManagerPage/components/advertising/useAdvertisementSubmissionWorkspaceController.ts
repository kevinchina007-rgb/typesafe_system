import { useEffect, useMemo, useRef, useState } from 'react'

import { useAdvertisingStore } from '@/app/stores/advertising-store'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import {
  cloneCreative,
  defaultCreative,
  inferResourceLabel,
  limitText,
  mergeAdvertisements,
  type AdvertisementSubmissionWorkspaceProps,
  type CanvasContextMenuState,
  type CreativeElement,
  type CreativeState,
  type FactoryMode,
  type ImageFactoryKind,
  type ResizeDirection,
  type ToneKey,
  type VisualStyleKey,
  type WorkspaceTab,
} from './AdvertisementSubmissionWorkspaceSupport'
import { useAdvertisementSubmissionWorkspaceCanvas } from './useAdvertisementSubmissionWorkspaceCanvas'
import { useAdvertisementSubmissionWorkspaceDraft } from './useAdvertisementSubmissionWorkspaceDraft'
import { useAdvertisementSubmissionWorkspaceMedia } from './useAdvertisementSubmissionWorkspaceMedia'
import { useAdvertisementSubmissionWorkspaceRestore } from './useAdvertisementSubmissionWorkspaceRestore'
import { useAdvertisementSubmissionWorkspaceSubmit } from './useAdvertisementSubmissionWorkspaceSubmit'
import { canvasHeight, canvasWidth, tonePalettes, visualStyleLabels } from './AdvertisementSubmissionWorkspaceSupport'

export function useAdvertisementSubmissionWorkspaceController({
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
  const [hyperlinkEnabled, setHyperlinkEnabled] = useState(false)
  const [searchDraft, setSearchDraft] = useState({ departureCity: '', arrivalCity: '', departureDate: '', timeRange: 'all' })
  const [flightSearchResults, setFlightSearchResults] = useState<typeof resourceOptions>([])
  const [hasSearchedFlights, setHasSearchedFlights] = useState(false)
  const splitContainerRef = useRef<HTMLDivElement | null>(null)
  const canvasRef = useRef<HTMLDivElement | null>(null)

  const selectedResourceLabel = inferResourceLabel(resourceOptions, targetResourceId)
  const draftAdvertisements = useMemo(() => mergeAdvertisements(ownerAdvertisements, localDraftAdvertisements), [localDraftAdvertisements, ownerAdvertisements])

  useEffect(() => {
    void loadOwnerAdvertisements()
  }, [loadOwnerAdvertisements])

  useEffect(() => {
    if (!targetResourceId && resourceOptions[0]) setTargetResourceId(resourceOptions[0].value)
  }, [resourceOptions, targetResourceId])

  useEffect(() => {
    if (defaultTargetResourceType !== 'Flight') return
    setFlightSearchResults([])
    setHasSearchedFlights(false)
  }, [defaultTargetResourceType])

  useEffect(() => {
    setCreative(current => (current.backgroundColor === tonePalettes[tone].bg ? current : { ...current, backgroundColor: tonePalettes[tone].bg }))
  }, [tone])

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

  function updateSearchDraftField(nextField: Partial<typeof searchDraft>) {
    setSearchDraft(current => ({ ...current, ...nextField }))
    setHasSearchedFlights(false)
  }

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
      setTargetResourceId(current => (nextResults.some(option => option.value === current) ? current : nextResults[0].value))
    }
  }

  const canvasActions = useAdvertisementSubmissionWorkspaceCanvas({
    creative,
    tone,
    setCreative,
    dragState,
    setDragState,
    resizeState,
    setResizeState,
    canvasContextMenu,
    setCanvasContextMenu,
    copiedElement,
    setCopiedElement,
    hoverResizeDirectionByElementId,
    setHoverResizeDirectionByElementId,
    splitContainerRef,
    canvasRef,
    factoryPanelWidth,
    setFactoryPanelWidth,
    isResizingFactoryPanel,
    setIsResizingFactoryPanel,
  })

  const mediaActions = useAdvertisementSubmissionWorkspaceMedia({
    tone,
    selectedResourceLabel,
    hyperlinkEnabled,
    imagePrompt,
    visualElementsPrompt,
    focusPrompt,
    avoidPrompt,
    textPrompt,
    textVisualStyles,
    setTextVisualStyles,
    imageVisualStyles,
    setImageVisualStyles,
    imageFactoryKind,
    shouldCutoutImageElement,
    backgroundAutoFit,
    setTextCandidates,
    setImageCandidates,
    setIsGeneratingText,
    setIsGeneratingImages,
    translate,
    onShowNotice,
    generateAdvertisementImageCandidates,
    describeCanvasContent: canvasActions.describeCanvasContent,
  })

  const submitActions = useAdvertisementSubmissionWorkspaceSubmit({
    creative,
    draftName,
    targetResourceId,
    selectedResourceLabel,
    hyperlinkEnabled,
    defaultPlacement,
    defaultTargetResourceType,
    resourceOptions,
    editingAdvertisementId,
    setEditingAdvertisementId,
    setLocalDraftAdvertisements,
    loadOwnerAdvertisements,
    uploadAdvertisementImage,
    createAdvertisement,
    updateAdvertisement,
    submitAdvertisementForReview,
    onShowNotice,
    translate,
    setWorkspaceTab,
    setIsSubmitting,
  })

  const restoreActions = useAdvertisementSubmissionWorkspaceRestore({
    setEditingAdvertisementId,
    setDraftName,
    setDraftNameInput,
    setTargetResourceId,
    setHyperlinkEnabled,
    setCreative,
    setTextPrompt,
    setImagePrompt,
    setFlightSearchResults,
    setHasSearchedFlights,
    setWorkspaceTab,
  })

  const draftActions = useAdvertisementSubmissionWorkspaceDraft({
    draftNameInput,
    setDraftNameInput,
    setDraftName,
    setIsDraftNameDialogOpen,
    setWorkspaceTab,
    resetComposer,
    onShowNotice,
  })

  const controller = {
    translate,
    draftName,
    defaultPlacement,
    defaultTargetResourceType,
    resourceOptions,
    targetResourceId,
    setTargetResourceId,
    hyperlinkEnabled,
    setHyperlinkEnabled,
    searchDraft,
    updateSearchDraftField,
    flightSearchResults,
    hasSearchedFlights,
    runFlightSearch,
    selectedResourceLabel,
    splitContainerRef,
    canvasRef,
    factoryPanelWidth,
    isResizingFactoryPanel,
    setIsResizingFactoryPanel,
    setFactoryPanelWidth,
    canvasWidth,
    canvasHeight,
    creative,
    factoryMode,
    setFactoryMode,
    textPrompt,
    setTextPrompt,
    focusPrompt,
    setFocusPrompt,
    visualElementsPrompt,
    setVisualElementsPrompt,
    avoidPrompt,
    setAvoidPrompt,
    textVisualStyles,
    setTextVisualStyles,
    imagePrompt,
    setImagePrompt,
    imageFactoryKind,
    setImageFactoryKind,
    shouldCutoutImageElement,
    setShouldCutoutImageElement,
    imageVisualStyles,
    setImageVisualStyles,
    backgroundAutoFit,
    setBackgroundAutoFit,
    textCandidates,
    imageCandidates,
    isGeneratingText,
    isGeneratingImages,
    dragTemplate,
    setDragTemplate,
    dragState,
    setDragState,
    resizeState,
    setResizeState,
    hoverResizeDirectionByElementId,
    setHoverResizeDirectionByElementId,
    copiedElement,
    setCopiedElement,
    canvasContextMenu,
    setCanvasContextMenu,
    ...canvasActions,
    ...mediaActions,
    ...submitActions,
    ...draftActions,
    ...restoreActions,
    isLoading,
    isSubmitting,
    tone,
    tonePalettes,
    visualStyleLabels,
    limitText,
    setIsDraftNameDialogOpen,
  }

  return {
    controller,
    workspaceTab,
    setWorkspaceTab,
    draftName,
    draftNameInput,
    setDraftNameInput,
    isDraftNameDialogOpen,
    setIsDraftNameDialogOpen,
    draftAdvertisements,
    openAdvertisementDraft: restoreActions.openAdvertisementDraft,
    withdrawAdvertisement: submitActions.withdrawAdvertisement,
    submitDraftAdvertisement: submitActions.submitDraftAdvertisement,
    confirmNewDraft: draftActions.confirmNewDraft,
    onOpenResource,
  }
}
