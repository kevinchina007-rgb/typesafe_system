import type { AppLanguage } from './mvp-types'
import { chineseTranslations } from './i18n/chinese'
import { englishTranslations } from './i18n/english'
import type { TranslationDictionary } from './i18n/types'

const translationTable: Record<AppLanguage, TranslationDictionary> = {
  en: englishTranslations,
  zh: chineseTranslations,
}

export function createTranslator(language: AppLanguage) {
  return (translationKey: string): string =>
    translationTable[language][translationKey] ?? translationTable.en[translationKey] ?? translationKey
}
