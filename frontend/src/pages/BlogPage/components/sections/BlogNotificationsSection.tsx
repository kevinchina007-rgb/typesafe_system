// BlogNotificationsSection：博客页面博客通知区块组件。

import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import type { BlogPageController } from '../../objects'
import { buildFallbackInitials, formatShortDate, notificationFilters } from '../../functions'

// 通知页面参数，只接收 controller。
type BlogNotificationsSectionProps = {
  controller: BlogPageController
}

// 通知页面，负责筛选展示不同类型的互动通知。
export function BlogNotificationsSection({ controller }: BlogNotificationsSectionProps) {
  const { notifications, notificationFilter, filteredNotifications, setNotificationFilter, openProfile } = controller

  return (
    <section className="grid gap-5">
      <div className="grid grid-cols-3 gap-4">
        {notificationFilters.map(filter => {
          const count = notifications.filter(notification => filter.types.includes(notification.notificationType)).length
          const active = notificationFilter === filter.key
          return (
            <button
              key={filter.key}
              className={`border p-5 text-center text-xl font-black transition ${
                active ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-pink-50 text-pink-500 hover:border-pink-400'
              }`}
              type="button"
              onClick={() => setNotificationFilter(filter.key)}
            >
              {filter.label}
              <span className={`ml-2 text-sm ${active ? 'text-white/80' : 'text-slate-400'}`}>{count}</span>
            </button>
          )
        })}
      </div>
      {filteredNotifications.map(notification => (
        <article key={notification.notificationId} className="flex items-center gap-4 border-b border-slate-100 py-4">
          <button
            className="grid h-12 w-12 place-items-center overflow-hidden border border-slate-200 bg-slate-100"
            type="button"
            disabled={!notification.actorUserId}
            onClick={() => (notification.actorUserId ? void openProfile(notification.actorUserId) : undefined)}
          >
            {notification.actorAvatarUrl ? (
              <BackendAssetImage
                className="h-full w-full object-cover"
                assetUrl={notification.actorAvatarUrl}
                alt={notification.actorDisplayName ?? '系统'}
                fallbackContent={buildFallbackInitials(notification.actorDisplayName ?? '系统')}
              />
            ) : (
              buildFallbackInitials(notification.actorDisplayName ?? '系统')
            )}
          </button>
          <div className="min-w-0">
            <button
              className="block truncate text-left text-lg font-black hover:text-pink-500"
              type="button"
              disabled={!notification.actorUserId}
              onClick={() => (notification.actorUserId ? void openProfile(notification.actorUserId) : undefined)}
            >
              {notification.actorDisplayName ?? '系统通知'}
            </button>
            <p className="truncate text-slate-500">{notification.content}</p>
          </div>
          <span className="ml-auto text-sm text-slate-400">{formatShortDate(notification.createdAt)}</span>
        </article>
      ))}
      {filteredNotifications.length === 0 ? <p className="text-slate-500">这一类暂时没有通知。</p> : null}
    </section>
  )
}
