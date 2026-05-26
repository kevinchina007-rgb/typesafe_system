import { chineseTranslations } from '@/lib/i18n/chinese'

export function createTranslator() {
  return (translationKey: string): string =>
    chineseTranslations[translationKey] ?? translationKey
}