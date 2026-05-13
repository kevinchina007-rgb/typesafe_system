export type HotAttraction = string

type HotAttractionsProps = {
  items: HotAttraction[]
  translate: (translationKey: string) => string
  onSelect: (value: string) => void
}

export function HotAttractions({ items, translate, onSelect }: HotAttractionsProps) {
  return (
    <div className="resource-hot-routes">
      <span className="resource-hot-routes-label">{translate('attractions.hotCities')}</span>
      <div className="resource-hot-routes-list">
        {items.map(item => (
          <button key={item} type="button" className="resource-tag-button" onClick={() => onSelect(item)}>
            {item}
          </button>
        ))}
      </div>
    </div>
  )
}
