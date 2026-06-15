import { Settings } from 'lucide-react'

import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { BlogPostCard } from '../BlogPostCard'
import type { BlogPageController } from '../../objects'
import { buildFallbackInitials } from '../../functions'

// 主页面板参数，决定是否以内联或浮层方式展示。
type BlogProfilePanelProps = {
  controller: BlogPageController
  signedInUser: { userId: string; nickname?: string | null; avatarUrl?: string | null } | null
  variant?: 'inline' | 'overlay'
}

// 个人主页面板，负责展示资料、关系列表和帖子列表。
export function BlogProfilePanel({ controller, signedInUser, variant = 'inline' }: BlogProfilePanelProps) {
  const {
    currentProfile,
    profileRelationTab,
    profileRelationUsers,
    profileSettingsOpen,
    profileRelationsHidden,
    displayedMinePosts,
    mineTab,
    isOwnProfile,
    followedAuthors,
    likePost,
    setMineTab,
    setProfileSettingsOpen,
    setProfileRelationTab,
    setProfileRelationUsers,
    openProfileRelation,
    openProfile,
    openPost,
    updateProfilePrivacy,
    followAuthor,
  } = controller

  // 个人主页名称兜底。
  const profileName = currentProfile?.nickname ?? signedInUser?.nickname ?? '未登录用户'
  // 个人主页头像兜底。
  const profileAvatar = currentProfile?.avatarUrl ?? signedInUser?.avatarUrl ?? null

  // 面板主体内容，内联与浮层共用。
  const body = (
    <section className="grid gap-6">
      <div className={`flex items-center gap-5 border border-slate-200 bg-white p-5 ${variant === 'overlay' ? 'shadow-sm' : ''}`}>
        <span className="grid h-20 w-20 place-items-center overflow-hidden border border-slate-200 bg-slate-100 text-3xl font-black">
          {profileAvatar ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={profileAvatar} alt={profileName} fallbackContent={buildFallbackInitials(profileName)} /> : buildFallbackInitials(profileName)}
        </span>
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-3">
            <h2 className="text-3xl font-black">{profileName}</h2>
            {!isOwnProfile && currentProfile ? (
              <button
                className="border border-pink-500 bg-pink-500 px-4 py-2 font-black text-white disabled:border-slate-200 disabled:bg-slate-100 disabled:text-slate-500"
                type="button"
                disabled={currentProfile.isFollowing || followedAuthors.has(currentProfile.userId) || !signedInUser}
                onClick={() => void followAuthor({ authorUserId: currentProfile.userId } as never)}
              >
                {currentProfile.isFollowing || followedAuthors.has(currentProfile.userId) ? '已关注' : '关注'}
              </button>
            ) : null}
            {isOwnProfile && currentProfile ? (
              <span className="relative ml-auto">
                <button className="grid h-10 w-10 place-items-center border border-slate-200 bg-white text-slate-700 hover:border-pink-400 hover:text-pink-500" type="button" onClick={() => setProfileSettingsOpen(open => !open)} title="社区设置">
                  <Settings className="h-5 w-5" aria-hidden="true" />
                </button>
                {profileSettingsOpen ? (
                  <span className="absolute right-0 top-12 z-20 grid w-72 gap-3 border border-slate-200 bg-white p-4 text-sm shadow-xl">
                    <strong className="text-base">社区设置</strong>
                    <label className="flex items-center justify-between gap-4">
                      <span className="font-bold text-slate-600">隐藏我的关注和粉丝列表</span>
                      <input type="checkbox" checked={currentProfile.hideRelations} onChange={event => void updateProfilePrivacy(event.target.checked)} />
                    </label>
                    <span className="text-slate-500">开启后，其他用户无法查看你的粉丝和关注列表。</span>
                  </span>
                ) : null}
              </span>
            ) : null}
          </div>
          <div className="mt-2 flex flex-wrap gap-4 text-slate-500">
            <button className="font-bold hover:text-pink-500 disabled:text-slate-400" type="button" disabled={!currentProfile || profileRelationsHidden} onClick={() => void openProfileRelation('followers')}>
              粉丝 {currentProfile?.followerCount ?? 0}
            </button>
            <button className="font-bold hover:text-pink-500 disabled:text-slate-400" type="button" disabled={!currentProfile || profileRelationsHidden} onClick={() => void openProfileRelation('following')}>
              关注 {currentProfile?.followingCount ?? 0}
            </button>
            <span className="font-bold">获赞 {currentProfile?.receivedLikeCount ?? 0}</span>
          </div>
        </div>
      </div>

      {profileRelationTab ? (
        <div className="grid gap-3 border border-slate-200 bg-white p-4">
          <div className="flex items-center justify-between">
            <strong className="text-xl font-black">{profileRelationTab === 'followers' ? '粉丝列表' : '关注列表'}</strong>
            <button className="border border-slate-200 px-3 py-1 font-bold" type="button" onClick={() => { setProfileRelationTab(null); setProfileRelationUsers([]) }}>
              收起
            </button>
          </div>
          {profileRelationsHidden ? <p className="text-slate-500">该用户已隐藏关注和粉丝列表。</p> : null}
          <div className="grid gap-3 md:grid-cols-2">
            {profileRelationUsers.map(user => (
              <button key={user.userId} className="flex items-center gap-3 border border-slate-200 p-3 text-left hover:border-pink-400" type="button" onClick={() => void openProfile(user.userId)}>
                <span className="grid h-12 w-12 place-items-center overflow-hidden border border-slate-200 bg-slate-100 font-black">
                  {user.avatarUrl ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={user.avatarUrl} alt={user.nickname} fallbackContent={buildFallbackInitials(user.nickname)} /> : buildFallbackInitials(user.nickname)}
                </span>
                <span className="min-w-0">
                  <strong className="block truncate">{user.nickname}</strong>
                  <span className="text-sm text-slate-500">粉丝 {user.followerCount} / 关注 {user.followingCount} / 获赞 {user.receivedLikeCount}</span>
                </span>
              </button>
            ))}
          </div>
          {!profileRelationsHidden && profileRelationUsers.length === 0 ? <p className="text-slate-500">这里暂时还没有人。</p> : null}
        </div>
      ) : null}

      <div className="flex gap-3">
        <button className={`border px-5 py-3 font-black ${mineTab === 'published' ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`} type="button" onClick={() => setMineTab('published')}>
          发布
        </button>
        {isOwnProfile ? (
          <button className={`border px-5 py-3 font-black ${mineTab === 'favorites' ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`} type="button" onClick={() => setMineTab('favorites')}>
            收藏
          </button>
        ) : null}
      </div>

      <div className="grid grid-cols-1 gap-5 md:grid-cols-3">
        {displayedMinePosts.map(post => (
          <BlogPostCard
            key={post.postId}
            post={post}
            compact
            onOpenPost={id => void openPost(id)}
            onOpenProfile={id => void openProfile(id)}
            onLike={nextPost => void likePost(nextPost)}
          />
        ))}
        {displayedMinePosts.length === 0 ? <p className="text-slate-500">{mineTab === 'published' ? '还没有发布内容。' : '还没有收藏帖子。'}</p> : null}
      </div>
    </section>
  )

  if (variant === 'overlay') {
    return body
  }

  return body
}
