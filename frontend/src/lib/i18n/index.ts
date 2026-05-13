import type { AppLanguage } from '@/lib/mvp-types/index'
import { chineseTranslations } from '@/lib/i18n/chinese'
import { englishTranslations } from '@/lib/i18n/english'
import type { TranslationDictionary } from '@/lib/i18n/types'

const translationTable: Record<AppLanguage, TranslationDictionary> = {
  en: englishTranslations,
  zh: chineseTranslations,
}

export function createTranslator(language: AppLanguage) {
  return (translationKey: string): string =>
    translationTable[language][translationKey] ?? translationTable.en[translationKey] ?? translationKey
}
