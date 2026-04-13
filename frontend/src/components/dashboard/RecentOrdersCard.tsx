import { DashboardCard } from './DashboardCard'
import type { RecentOrdersCardProps } from './types'

export function RecentOrdersCard({ orders, translate, onSelectView }: RecentOrdersCardProps) {
  return (
    <DashboardCard
      eyebrow={translate('nav.section.workspace')}
      title={translate('dashboard.orders.title')}
      description={translate('dashboard.orders.description')}
      action={
        <button type="button" className="secondary-button" onClick={() => onSelectView('orders')}>
          {translate('dashboard.viewOrders')}
        </button>
      }
    >
      {orders.length > 0 ? (
        <ul className="entity-list">
          {orders.map(order => (
            <li key={order.orderId}>
              <div>
                <strong>{order.orderId}</strong>
                <p>{`${order.status} · ${order.totalPriceLabel}`}</p>
              </div>
              <span>{`${order.itemCount} · ${order.createdAtLabel}`}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="empty-state">{translate('dashboard.orders.empty')}</p>
      )}
    </DashboardCard>
  )
}

