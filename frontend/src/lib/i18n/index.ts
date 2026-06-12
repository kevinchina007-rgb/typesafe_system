// 本文件作为当前目录的入口导出文件。

import { chineseTranslations } from '@/lib/i18n/chinese'

export function createTranslator() {
  return (translationKey: string): string =>
    chineseTranslations[translationKey] ?? translationKey
}