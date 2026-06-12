import type { HomeHeroSlide } from '../objects'

// 计算下一张首页轮播图索引。
export function getNextHomeSlideIndex(activeIndex: number, totalSlides: number): number {
  return (activeIndex + 1) % totalSlides
}

// 截取首页轮播当前显示的文案长度。
export function buildHomeTypedTagline(activeSlide: HomeHeroSlide, typedLength: number): string {
  return activeSlide.tagline.slice(0, typedLength)
}
