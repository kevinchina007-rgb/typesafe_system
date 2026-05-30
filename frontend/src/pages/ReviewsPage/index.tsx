import type { ReviewsPageProps } from './objects'
import { REVIEWS_PAGE_REGIONS } from './objects'
import { ReviewsPageShell } from './components/ReviewsPageShell'
import { useReviewsPageController } from './hooks'

export function ReviewsPage({
  currentLanguage,
  signedInUser,
  translate,
  onShowNotice,
}: ReviewsPageProps) {
  const controller = useReviewsPageController({
    currentLanguage,
    signedInUser,
    translate,
    onShowNotice,
  })

  return (
    <>
      <div className="sr-only">
        {REVIEWS_PAGE_REGIONS.map(region => (
          <span key={region}>{region}</span>
        ))}
      </div>
      <ReviewsPageShell controller={controller} />
    </>
  )
}
