import type { AppLanguage, TourGroupDetailsResponse, TourGroupMembershipResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { formatTravelerChipLabel, getTourGroupConceptLabel } from '@/lib/presenters/tour-group-presenter'
import { localizeTourGroupStatus } from '@/lib/presenters/view-models'

type TourGroupMemberSectionProps = {
  currentLanguage: AppLanguage
  details: TourGroupDetailsResponse
  signedInUser: UserResponse | null
  activeMembership: TourGroupMembershipResponse | null
  travelers: TravelerResponse[]
  isBusy: boolean
  translate: (translationKey: string) => string
  onJoinGroup: () => Promise<void>
  onAddMembershipTraveler: (travelerId: string) => Promise<void>
}

export function TourGroupMemberSection({
  currentLanguage,
  details,
  signedInUser,
  activeMembership,
  travelers,
  isBusy,
  translate,
  onJoinGroup,
  onAddMembershipTraveler,
}: TourGroupMemberSectionProps) {
  const membershipTravelerIds = new Set(
    activeMembership
      ? details.membershipTravelers
          .filter(row => row.membershipId === activeMembership.membershipId && row.status === 'Active')
          .map(row => row.travelerId)
      : [],
  )

  const availableTravelers = travelers.filter(traveler => !membershipTravelerIds.has(traveler.travelerId))
  const joinedTravelers = travelers.filter(traveler => membershipTravelerIds.has(traveler.travelerId))

  return (
    <section className="list-surface">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{getTourGroupConceptLabel('member', currentLanguage)}</p>
          <h3>{translate('tourGroups.memberSectionTitle')}</h3>
        </div>
      </div>

      {!signedInUser ? (
        <p className="empty-state">{translate('tourGroups.guest')}</p>
      ) : !activeMembership ? (
        <div className="stack-form">
          <p className="hero-copy">{translate('tourGroups.joinHint')}</p>
          <div className="action-cluster">
            <button type="button" disabled={isBusy} onClick={() => void onJoinGroup()}>
              {translate('tourGroups.joinGroup')}
            </button>
          </div>
        </div>
      ) : (
        <div className="stack-form">
          <div className="detail-grid">
            <div>
              <span className="detail-label">{translate('tourGroups.membershipStatus')}</span>
              <strong>{localizeTourGroupStatus(activeMembership.status, currentLanguage)}</strong>
            </div>
            <div>
              <span className="detail-label">{translate('tourGroups.joinedAt')}</span>
              <strong>{activeMembership.joinedAt}</strong>
            </div>
          </div>

          <div className="panel-card">
            <h4>{translate('tourGroups.myTravelers')}</h4>
            {joinedTravelers.length === 0 ? (
              <p className="empty-state">{translate('tourGroups.noJoinedTravelers')}</p>
            ) : (
              <ul className="entity-list">
                {joinedTravelers.map(traveler => (
                  <li key={traveler.travelerId}>
                    <div>
                      <strong>{traveler.fullName}</strong>
                      <p>{formatTravelerChipLabel(traveler)}</p>
                    </div>
                    <span className="tag-chip">{translate('tourGroups.readyForChoices')}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <form
            className="panel-card stack-form"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              await onAddMembershipTraveler(String(formData.get('travelerId') ?? ''))
              event.currentTarget.reset()
            }}
          >
            <h4>{translate('tourGroups.addMembershipTraveler')}</h4>
            <label>
              {translate('tourGroups.selectTraveler')}
              <select name="travelerId" required defaultValue="">
                <option value="" disabled>
                  {translate('tourGroups.selectTravelerPlaceholder')}
                </option>
                {availableTravelers.map(traveler => (
                  <option key={traveler.travelerId} value={traveler.travelerId}>
                    {formatTravelerChipLabel(traveler)}
                  </option>
                ))}
              </select>
            </label>
            <button type="submit" disabled={isBusy || availableTravelers.length === 0}>
              {translate('tourGroups.addMembershipTraveler')}
            </button>
          </form>
        </div>
      )}
    </section>
  )
}
