import { useEffect, useMemo, useState } from 'react'

import { buildHomeTypedTagline, getNextHomeSlideIndex } from '../functions'
import { HOME_FEATURE_PLACEHOLDERS, HOME_HERO_SLIDES, type HomePageController } from '../objects'

export function useHomePageController(): HomePageController {
  const [activeIndex, setActiveIndex] = useState(0)
  const [typedLength, setTypedLength] = useState(0)
  const activeSlide = HOME_HERO_SLIDES[activeIndex]

  useEffect(() => {
    const intervalId = window.setInterval(() => {
      setActiveIndex(index => getNextHomeSlideIndex(index, HOME_HERO_SLIDES.length))
    }, 7200)

    return () => window.clearInterval(intervalId)
  }, [])

  useEffect(() => {
    setTypedLength(0)
    const intervalId = window.setInterval(() => {
      setTypedLength(length => {
        if (length >= activeSlide.tagline.length) {
          window.clearInterval(intervalId)
          return length
        }
        return length + 1
      })
    }, 85)

    return () => window.clearInterval(intervalId)
  }, [activeSlide.tagline])

  const typedTagline = useMemo(() => buildHomeTypedTagline(activeSlide, typedLength), [activeSlide, typedLength])

  return {
    activeIndex,
    typedLength,
    activeSlide,
    typedTagline,
    homeHeroSlides: HOME_HERO_SLIDES,
    homeFeaturePlaceholders: HOME_FEATURE_PLACEHOLDERS,
  }
}

