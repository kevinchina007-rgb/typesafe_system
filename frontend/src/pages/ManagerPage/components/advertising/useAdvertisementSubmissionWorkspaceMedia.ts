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
  textStylePreference,
  imageVisualStyles,
  setImageVisualStyles,
  imageStylePreference,
  imageFactoryKind,
  shouldCutoutImageElement,
  backgroundAutoFit,
  textGenerationNonce,
  setTextGenerationNonce,
  setTextCandidates,
  setImageCandidates,
  setIsGeneratingText,
  setIsGeneratingImages,
  translate,
  onShowNotice,
  generateAdvertisementTextCandidates,
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
  textStylePreference: string
  imageVisualStyles: VisualStyleKey[]
  setImageVisualStyles: (value: VisualStyleKey[] | ((current: VisualStyleKey[]) => VisualStyleKey[])) => void
  imageStylePreference: string
  imageFactoryKind: ImageFactoryKind
  shouldCutoutImageElement: boolean
  backgroundAutoFit: boolean
  textGenerationNonce: number
  setTextGenerationNonce: (value: number | ((current: number) => number)) => void
  setTextCandidates: (value: ReturnType<typeof buildTextCandidates>) => void
  setImageCandidates: (value: ReturnType<typeof buildImageCandidates>) => void
  setIsGeneratingText: (value: boolean) => void
  setIsGeneratingImages: (value: boolean) => void
  translate: AdvertisementSubmissionWorkspaceProps['translate']
  onShowNotice?: AdvertisementSubmissionWorkspaceProps['onShowNotice']
  generateAdvertisementTextCandidates: (args: any) => Promise<any>
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

  function buildTextColorPreferenceHint(value: string) {
    const normalized = value.trim().toLowerCase()
    if (!normalized) {
      return ''
    }

    if (normalized.includes('红') || normalized.includes('red') || normalized.includes('crimson') || normalized.includes('scarlet')) {
      return '自定义风格中的颜色词是红色时，请把红色作为文字的主色和最醒目的视觉主导色。'
    }

    if (normalized.includes('蓝') || normalized.includes('blue') || normalized.includes('azure') || normalized.includes('sky')) {
      return '自定义风格中的颜色词是蓝色时，请把蓝色作为文字的主色和最醒目的视觉主导色。'
    }

    if (normalized.includes('绿') || normalized.includes('green') || normalized.includes('emerald') || normalized.includes('jade')) {
      return '自定义风格中的颜色词是绿色时，请把绿色作为文字的主色和最醒目的视觉主导色。'
    }

    if (normalized.includes('金') || normalized.includes('gold') || normalized.includes('golden')) {
      return '自定义风格中的颜色词是金色时，请把金色作为文字的主色和最醒目的视觉主导色。'
    }

    if (normalized.includes('紫') || normalized.includes('purple') || normalized.includes('violet')) {
      return '自定义风格中的颜色词是紫色时，请把紫色作为文字的主色和最醒目的视觉主导色。'
    }

    return ''
  }

  async function generateTextStyles() {
    const linkedResourceLabel = hyperlinkEnabled ? selectedResourceLabel : ''
    const sourceText = textPrompt.trim() || linkedResourceLabel
    const colorPreferenceHint = buildTextColorPreferenceHint(textStylePreference)
    const styleRequirement = [
      textVisualStyles.length > 0 ? `选择风格：${textVisualStyles.map(style => visualStyleLabels[style]).join('、')}` : null,
      textStylePreference.trim() ? `自定义风格偏好：${textStylePreference.trim()}` : null,
      colorPreferenceHint || null,
    ]
      .filter(Boolean)
      .join('；') || '默认文字风格'

    const textSource = [
      `\u8bf7\u4ee5\u6587\u5b57\u672c\u8eab\u4f5c\u4e3a\u753b\u9762\u4e3b\u4f53\uff0c\u5fc5\u987b\u4e25\u683c\u5305\u542b ${sourceText}\u3002`,
      `\u8bf7\u628a ${sourceText} \u76f4\u63a5\u6392\u7248\u6210\u4e3b\u89c6\u89c9\u5185\u5bb9\uff0c\u4e0d\u8981\u505a\u6210\u80cc\u666f\u56fe\u3002`,
      '\u4e0d\u8981\u7701\u7565\u3001\u66ff\u6362\u3001\u6539\u5199\u6216\u7ffb\u8bd1\u539f\u6587\u3002',
      '\u4e0d\u8981\u628a\u6587\u5b57\u85cf\u5230\u56fe\u7247\u89d2\u843d\uff0c\u6587\u5b57\u5fc5\u987b\u6e05\u6670\u53ef\u8bfb\u3002',
      styleRequirement,
      `\u91cd\u91c7\u6837\u8f6e\u6b21\uff1a${textGenerationNonce}`,
      '\u8bf7\u7ed9\u51fa\u4e0e\u4e0a\u4e00\u8f6e\u660e\u663e\u4e0d\u540c\u7684\u6392\u7248\u6784\u56fe\uff0c\u4f46\u4fdd\u6301\u539f\u6587\u5b8c\u5168\u4e00\u81f4\u3002',
      '\u8f93\u51fa\u9002\u5408\u5e7f\u544a\u7f16\u8f91\u5668\u7684\u6587\u5b57\u6837\u5f0f\u65b9\u6848\u3002',
    ].join(' ')

    setIsGeneratingText(true)
    try {
      const response = await generateAdvertisementTextCandidates({
        prompt: textSource,
        sourceText,
        styleRequirement,
        regenerationNonce: textGenerationNonce,
        focus: null,
        tone: tonePalettes[tone].label,
        resourceLabel: linkedResourceLabel || sourceText,
        advertisementKind: 'ResourcePromotion',
        candidateCount: 1,
        avoidText: [avoidPrompt.trim(), '\u4e0d\u8981\u51fa\u73b0\u9519\u522b\u5b57\u3001\u989d\u5916\u8bf4\u660e\u6216\u4e0e\u4e3b\u9898\u65e0\u5173\u7684\u5185\u5bb9\u3002'].filter(Boolean).join('\uff0c'),
      })

      if (response.candidates.length > 0) {
        setTextCandidates(buildRemoteTextCandidates(response.candidates, sourceText, tone, textStylePreference))
      } else {
        setTextCandidates(buildTextCandidates(sourceText, tone, linkedResourceLabel, textGenerationNonce, textStylePreference))
      }
    } catch (error) {
      setTextCandidates(buildTextCandidates(sourceText, tone, linkedResourceLabel, textGenerationNonce, textStylePreference))
      onShowNotice?.('error', translate('advertising.factory.text'), error instanceof Error ? error.message : '\u6587\u5b57\u751f\u6210\u5931\u8d25\uff0c\u5df2\u56de\u9000\u5230\u672c\u5730\u9884\u89c8\u56fe')
    } finally {
      setTextGenerationNonce(current => current + 1)
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
      imageStylePreference.trim() ? `custom style: ${imageStylePreference.trim()}` : null,
      imageFactoryKind === 'element' ? 'image usage: element' : 'image usage: background',
      imageFactoryKind === 'element' ? `cutout subject: ${shouldCutoutImageElement ? 'yes' : 'no'}` : null,
      imageFactoryKind === 'element' && shouldCutoutImageElement ? 'transparent background' : null,
      imageFactoryKind === 'background' && backgroundAutoFit && canvasDescription ? `fit around ${canvasDescription}` : null,
    ]
      .filter(Boolean)
      .join(', ')
    const supportingCopy = [focusPrompt.trim(), visualElementsPrompt.trim()].filter(Boolean).join('\uff0c')
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
        avoidText: avoidPrompt.trim() || '\u4e0d\u8981\u51fa\u73b0\u56fe\u7247\u4e2d\u7684\u6587\u5b57\u3001\u6c34\u5370\u3001logo',
      })
      if (response.candidates.length > 0) {
        setImageCandidates(buildRemoteImageCandidates(response.candidates, tone, imageFactoryKind))
        return
      }
      setImageCandidates(buildImageCandidates(fallbackLabel, tone, imageFactoryKind, imageFactoryKind === 'element' && shouldCutoutImageElement))
    } catch (error) {
      setImageCandidates(buildImageCandidates(fallbackLabel, tone, imageFactoryKind, imageFactoryKind === 'element' && shouldCutoutImageElement))
      onShowNotice?.('error', translate('advertising.factory.image'), error instanceof Error ? error.message : '\u56fe\u7247\u751f\u6210\u5931\u8d25\uff0c\u5df2\u56de\u9000\u5230\u672c\u5730\u9884\u89c8\u56fe')
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
