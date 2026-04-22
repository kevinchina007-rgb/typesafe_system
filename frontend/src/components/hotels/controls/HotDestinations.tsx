type HotDestinationsProps = {
  destinations: string[]
  translate: (translationKey: string) => string
  onSelectDestination: (value: string) => void
}

export function HotDestinations({ destinations, translate, onSelectDestination }: HotDestinationsProps) {
  return (
    <div className="resource-hot-routes">
      <span className="resource-hot-routes-label">{translate('hotels.hotDestinations')}</span>
      <div className="resource-hot-routes-list">
        {destinations.map(destination => (
          <button key={destination} type="button" className="resource-tag-button" onClick={() => onSelectDestination(destination)}>
            {destination}
          </button>
        ))}
      </div>
    </div>
  )
}
