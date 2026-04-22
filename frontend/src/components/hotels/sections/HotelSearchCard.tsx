import { DateRangeSelector } from '../controls/DateRangeSelector'
import { DestinationSelector } from '../controls/DestinationSelector'
import { GuestRoomSelector } from '../controls/GuestRoomSelector'
import { HotelPreferenceSelector } from '../controls/HotelPreferenceSelector'
import { HotDestinations } from '../controls/HotDestinations'
import { applyHotelQuickDatePreset, countHotelStayNights, hotelLocationPlaceholder, type HotelPreference, type HotelQuickDatePreset } from '../hotelBookingModel'

const quickDatePresets: HotelQuickDatePreset[] = ['tonight', 'weekend', 'nextWeek', 'holiday']

type HotelSearchCardProps = {
  isBusy: boolean
  searchLocation: string
  searchCheckInDate: string
  searchCheckOutDate: string
  roomCount: number
  guestCount: number
  hotelPreference: HotelPreference
  nearbyPreference: string
  selectedQuickDatePreset: HotelQuickDatePreset | null
  hotDestinations: string[]
  recentSearches: string[]
  averagePriceInsight: string
  translate: (translationKey: string) => string
  onSearchLocationChange: (value: string) => void
  onSearchCheckInDateChange: (value: string) => void
  onSearchCheckOutDateChange: (value: string) => void
  onRoomCountChange: (value: number) => void
  onGuestCountChange: (value: number) => void
  onHotelPreferenceChange: (value: HotelPreference) => void
  onNearbyPreferenceChange: (value: string) => void
  onSelectQuickDatePreset: (preset: HotelQuickDatePreset, nextDates: { checkInDate: string; checkOutDate: string }) => void
  onSelectDestination: (value: string) => void
  onSearch: () => void
}

export function HotelSearchCard({
  isBusy,
  searchLocation,
  searchCheckInDate,
  searchCheckOutDate,
  roomCount,
  guestCount,
  hotelPreference,
  nearbyPreference,
  selectedQuickDatePreset,
  hotDestinations,
  recentSearches,
  averagePriceInsight,
  translate,
  onSearchLocationChange,
  onSearchCheckInDateChange,
  onSearchCheckOutDateChange,
  onRoomCountChange,
  onGuestCountChange,
  onHotelPreferenceChange,
  onNearbyPreferenceChange,
  onSelectQuickDatePreset,
  onSelectDestination,
  onSearch,
}: HotelSearchCardProps) {
  const stayNights = countHotelStayNights(searchCheckInDate, searchCheckOutDate)

  return (
    <section className="resource-search-card app-card">
      <div className="resource-search-card-head">
        <div>
          <p className="eyebrow-label">{translate('nav.hotels')}</p>
          <h2 className="resource-search-card-title">{translate('hotels.searchModuleTitle')}</h2>
        </div>
        <div className="resource-search-card-tip-grid">
          <div className="resource-search-card-tip">
            <span>{translate('hotels.averagePrice')}</span>
            <strong>{averagePriceInsight}</strong>
          </div>
          <div className="resource-search-card-tip">
            <span>{translate('hotels.stayNights')}</span>
            <strong>{translate('hotels.stayNightsValue').replace('{count}', String(stayNights))}</strong>
          </div>
        </div>
      </div>

      <HotDestinations destinations={hotDestinations} translate={translate} onSelectDestination={onSelectDestination} />

      <div className="resource-search-grid resource-search-grid-primary hotel-search-grid-primary">
        <DestinationSelector
          value={searchLocation}
          placeholder={hotelLocationPlaceholder}
          translate={translate}
          suggestions={[...recentSearches, ...hotDestinations]}
          onChange={onSearchLocationChange}
        />
        <DateRangeSelector
          checkInDate={searchCheckInDate}
          checkOutDate={searchCheckOutDate}
          translate={translate}
          onCheckInDateChange={onSearchCheckInDateChange}
          onCheckOutDateChange={onSearchCheckOutDateChange}
        />
      </div>

      <div className="resource-quick-date-row">
        <span className="resource-quick-date-label">{translate('hotels.quickDate')}</span>
        <div className="resource-quick-date-list">
          {quickDatePresets.map(preset => (
            <button
              key={preset}
              type="button"
              className={`resource-chip-button ${selectedQuickDatePreset === preset ? 'is-active' : ''}`}
              onClick={() => onSelectQuickDatePreset(preset, applyHotelQuickDatePreset(preset))}
            >
              {translate(`hotels.quickDate.${preset}`)}
            </button>
          ))}
        </div>
      </div>

      <div className="resource-search-grid resource-search-grid-secondary hotel-search-grid-secondary">
        <GuestRoomSelector
          roomCount={roomCount}
          guestCount={guestCount}
          translate={translate}
          onRoomCountChange={onRoomCountChange}
          onGuestCountChange={onGuestCountChange}
        />
        <HotelPreferenceSelector
          hotelPreference={hotelPreference}
          nearbyPreference={nearbyPreference}
          translate={translate}
          onHotelPreferenceChange={onHotelPreferenceChange}
          onNearbyPreferenceChange={onNearbyPreferenceChange}
        />
        <button type="button" className="resource-search-submit-button" onClick={onSearch} disabled={isBusy}>
          {translate('hotels.search')}
        </button>
      </div>

      <div className="resource-search-assist">
        <div className="resource-search-assist-block">
          <span className="resource-search-assist-label">{translate('hotels.recentSearches')}</span>
          <div className="resource-search-assist-tags">
            {recentSearches.map(item => (
              <span key={item} className="resource-tag-muted">{item}</span>
            ))}
          </div>
        </div>
        <div className="resource-search-assist-block">
          <span className="resource-search-assist-label">{translate('hotels.promoHint')}</span>
          <div className="resource-search-assist-tags">
            <span className="resource-tag-muted">{translate('hotels.promoValueOne')}</span>
            <span className="resource-tag-muted">{translate('hotels.promoValueTwo')}</span>
          </div>
        </div>
      </div>
    </section>
  )
}
