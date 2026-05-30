import type { HomeHeroSlide } from '../objects'

export function getNextHomeSlideIndex(activeIndex: number, totalSlides: number): number {
  return (activeIndex + 1) % totalSlides
}

export function buildHomeTypedTagline(activeSlide: HomeHeroSlide, typedLength: number): string {
  return activeSlide.tagline.slice(0, typedLength)
}

