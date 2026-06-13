import { useEffect, type MouseEvent as ReactMouseEvent, type RefObject } from 'react'

import type { CreativeElement, CreativeState, ResizeDirection, ToneKey } from './AdvertisementSubmissionWorkspaceSupport'
import { canvasWidth, cursorForResizeDirection, tonePalettes, type CanvasContextMenuState } from './AdvertisementSubmissionWorkspaceSupport'

export function useAdvertisementSubmissionWorkspaceCanvas({
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
}: {
  creative: CreativeState
  tone: ToneKey
  setCreative: (value: CreativeState | ((current: CreativeState) => CreativeState)) => void
  dragState: { elementId: string; offsetX: number; offsetY: number } | null
  setDragState: (value: { elementId: string; offsetX: number; offsetY: number } | null) => void
  resizeState: {
    elementId: string
    startClientX: number
    startClientY: number
    startX: number
    startY: number
    startWidth: number
    startHeight: number
    direction: ResizeDirection
  } | null
  setResizeState: (value: {
    elementId: string
    startClientX: number
    startClientY: number
    startX: number
    startY: number
    startWidth: number
    startHeight: number
    direction: ResizeDirection
  } | null) => void
  canvasContextMenu: CanvasContextMenuState
  setCanvasContextMenu: (value: CanvasContextMenuState) => void
  copiedElement: CreativeElement | null
  setCopiedElement: (value: CreativeElement | null) => void
  hoverResizeDirectionByElementId: Record<string, ResizeDirection | null>
  setHoverResizeDirectionByElementId: (value: Record<string, ResizeDirection | null> | ((current: Record<string, ResizeDirection | null>) => Record<string, ResizeDirection | null>)) => void
  splitContainerRef: RefObject<HTMLDivElement | null>
  canvasRef: RefObject<HTMLDivElement | null>
  factoryPanelWidth: number
  setFactoryPanelWidth: (value: number) => void
  isResizingFactoryPanel: boolean
  setIsResizingFactoryPanel: (value: boolean) => void
}) {
  void hoverResizeDirectionByElementId
  void setHoverResizeDirectionByElementId
  void factoryPanelWidth

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
  }, [isResizingFactoryPanel, setFactoryPanelWidth, setIsResizingFactoryPanel, splitContainerRef])

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

        if (direction.includes('e')) nextWidth = resizeState.startWidth + widthDelta
        if (direction.includes('s')) nextHeight = resizeState.startHeight + heightDelta
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

        resizeElement(resizeState.elementId, nextX, nextY, nextWidth, nextHeight)
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
  }, [canvasRef, dragState, resizeState, setDragState, setResizeState])

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
  }, [canvasContextMenu, setCanvasContextMenu])

  function addTemplateToCanvas(template: CreativeElement, placement?: { x: number; y: number }) {
    const nextElement = {
      ...template,
      id: `${template.type}-${Date.now()}`,
      x: placement?.x ?? template.x,
      y: placement?.y ?? template.y,
    }
    setCreative(current => ({ ...current, backgroundColor: tonePalettes[tone].bg, elements: [...current.elements, nextElement] }))
  }

  function canvasPointFromMouseEvent(event: ReactMouseEvent<HTMLElement>) {
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

  function openCanvasContextMenu(event: ReactMouseEvent<HTMLElement>, targetElementId: string | null) {
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

  function copyCanvasElement(elementId: string) {
    const element = creative.elements.find(currentElement => currentElement.id === elementId)
    if (!element) return
    setCopiedElement({ ...element })
    setCanvasContextMenu(null)
  }

  function cutCanvasElement(elementId: string) {
    const element = creative.elements.find(currentElement => currentElement.id === elementId)
    if (!element) return
    setCopiedElement({ ...element })
    removeElement(elementId)
    setCanvasContextMenu(null)
  }

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

  function moveElement(elementId: string, x: number, y: number) {
    setCreative(current => ({
      ...current,
      elements: current.elements.map(element => (element.id === elementId ? { ...element, x, y } : element)),
    }))
  }

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

  function removeElement(elementId: string) {
    setCreative(current => ({
      ...current,
      elements: current.elements.filter(element => element.id !== elementId),
    }))
  }

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

  return {
    addTemplateToCanvas,
    openCanvasContextMenu,
    copyCanvasElement,
    cutCanvasElement,
    pasteCanvasElement,
    moveElement,
    resizeElement,
    removeElement,
    describeCanvasContent,
  }
}
