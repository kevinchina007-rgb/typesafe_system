import type { AdvertisementSubmissionWorkspaceProps, ImageFactoryKind, VisualStyleKey } from './AdvertisementSubmissionWorkspaceSupport'
import { buildImageCandidates, buildRemoteImageCandidates, buildRemoteTextCandidates, buildTextCandidates, tonePalettes, visualStyleLabels } from './AdvertisementSubmissionWorkspaceSupport'

export function useAdvertisementSubmissionWorkspaceMedia({
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
  describeCanvasContent,
}: {
  tone: keyof typeof tonePalettes
  selectedResourceLabel: string
  hyperlinkEnabled: boolean
  imagePrompt: string
  visualElementsPrompt: string
  focusPrompt: string
  avoidPrompt: string
  textPrompt: string
  textVisualStyles: VisualStyleKey[]
  setTextVisualStyles: (value: VisualStyleKey[] | ((current: VisualStyleKey[]) => VisualStyleKey[])) => void
  imageVisualStyles: VisualStyleKey[]
  setImageVisualStyles: (value: VisualStyleKey[] | ((current: VisualStyleKey[]) => VisualStyleKey[])) => void
  imageFactoryKind: ImageFactoryKind
  shouldCutoutImageElement: boolean
  backgroundAutoFit: boolean
  setTextCandidates: (value: ReturnType<typeof buildTextCandidates>) => void
  setImageCandidates: (value: ReturnType<typeof buildImageCandidates>) => void
  setIsGeneratingText: (value: boolean) => void
  setIsGeneratingImages: (value: boolean) => void
  translate: AdvertisementSubmissionWorkspaceProps['translate']
  onShowNotice?: AdvertisementSubmissionWorkspaceProps['onShowNotice']
  generateAdvertisementImageCandidates: (args: any) => Promise<any>
  describeCanvasContent: () => string
}) {
  void setTextVisualStyles
  void setImageVisualStyles

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

  async function generateTextStyles() {
    const linkedResourceLabel = hyperlinkEnabled ? selectedResourceLabel : ''
    const sourceText = textPrompt.trim() || linkedResourceLabel
    const styleRequirement = [textVisualStyles.length > 0 ? `风格：${textVisualStyles.map(style => visualStyleLabels[style]).join('、')}` : null]
      .filter(Boolean)
      .join('、') || '保持和当前广告主题一致'

    const textSource = [
      `请根据当前草稿内容${sourceText}生成广告文案。`,
      `要求：${styleRequirement}`,
      '只输出文案，不要输出解释。',
      '内容要适合广告位展示。',
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
        avoidText: [avoidPrompt.trim(), '不要写错字，不要漏字，不要出现多余说明'].filter(Boolean).join('、'),
      })
      if (response.candidates.length > 0) {
        setTextCandidates(buildRemoteTextCandidates(response.candidates, sourceText))
        return
      }
      setTextCandidates(buildTextCandidates(sourceText, tone, linkedResourceLabel))
    } catch (error) {
      setTextCandidates(buildTextCandidates(sourceText, tone, linkedResourceLabel))
      onShowNotice?.('error', translate('advertising.factory.text'), error instanceof Error ? error.message : '文案生成失败，已回退到本地候选')
    } finally {
      setIsGeneratingText(false)
    }
  }

  async function generateImageStyles(styleOverride?: VisualStyleKey[]) {
    const activeStyles = styleOverride ?? imageVisualStyles
    const linkedResourceLabel = hyperlinkEnabled ? selectedResourceLabel : ''
    const canvasDescription = backgroundAutoFit ? describeCanvasContent() : ''
    const fallbackLabel = imagePrompt || visualElementsPrompt || focusPrompt || selectedResourceLabel
    const primaryPrompt = [
      imagePrompt.trim() || visualElementsPrompt.trim() || focusPrompt.trim() || linkedResourceLabel,
      `styles: ${activeStyles.map(style => visualStyleLabels[style]).join(', ')}`,
      imageFactoryKind === 'element' ? 'image usage: element' : 'image usage: background',
      imageFactoryKind === 'element' ? `cutout subject: ${shouldCutoutImageElement ? 'yes' : 'no'}` : null,
      imageFactoryKind === 'element' && shouldCutoutImageElement ? 'transparent background' : null,
      imageFactoryKind === 'background' && backgroundAutoFit && canvasDescription ? `fit around ${canvasDescription}` : null,
    ]
      .filter(Boolean)
      .join(', ')
    const supportingCopy = [focusPrompt.trim(), visualElementsPrompt.trim()].filter(Boolean).join('、')
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
        width: 960,
        height: 240,
        candidateCount: 1,
        avoidText: avoidPrompt.trim() || '不要出现图片中的文字、水印、logo',
      })
      if (response.candidates.length > 0) {
        setImageCandidates(buildRemoteImageCandidates(response.candidates, tone, imageFactoryKind))
        return
      }
      setImageCandidates(buildImageCandidates(fallbackLabel, tone, imageFactoryKind, imageFactoryKind === 'element' && shouldCutoutImageElement))
    } catch (error) {
      setImageCandidates(buildImageCandidates(fallbackLabel, tone, imageFactoryKind, imageFactoryKind === 'element' && shouldCutoutImageElement))
      onShowNotice?.('error', translate('advertising.factory.image'), error instanceof Error ? error.message : '图片生成失败，已回退到本地预览图')
    } finally {
      setIsGeneratingImages(false)
    }
  }

  return {
    toggleStyleSelection,
    generateTextStyles,
    generateImageStyles,
  }
}
