import type { TourGroupConversationSummaryResponse, TourGroupMembershipResponse } from '@/lib/mvp-types/index'

export const quickReactions = ['👍', '❤️', '😄', '🎉']

export function localizeConversationTitle(translate: (translationKey: string) => string, conversationTitle: string, conversationType?: string) {
  if (conversationType === 'GroupPublic' || conversationTitle === 'Group chat') {
    return translate('tourGroups.groupChat')
  }
  if (conversationTitle === 'Direct chat') {
    return translate('tourGroups.directMessages')
  }
  return conversationTitle
}

export function getDirectConversationIdentityLabel(
  translate: (translationKey: string) => string,
  organizerUserId: string,
  conversation: TourGroupConversationSummaryResponse,
) {
  const roleLabel = conversation.counterpartUserId === organizerUserId ? translate('tourGroups.organizer') : translate('tourGroups.member')
  const displayName = conversation.counterpartDisplayName?.trim() || conversation.counterpartUserId || translate('tourGroups.directMessages')
  return `${roleLabel} - ${displayName}`
}

export function getMemberDisplayNameMap(
  memberships: TourGroupMembershipResponse[],
  signedInUserId: string | undefined,
  signedInUserNickname: string | undefined,
) {
  const entries = memberships.map(membership => [membership.userId, membership.userDisplayName ?? membership.userId] as const)
  if (signedInUserId && signedInUserNickname) {
    entries.push([signedInUserId, signedInUserNickname] as const)
  }
  return new Map(entries)
}

export function getUserDisplayName(
  userId: string,
  organizerUserId: string,
  memberDisplayNameMap: Map<string, string>,
  translate: (translationKey: string) => string,
) {
  if (userId === organizerUserId) {
    return memberDisplayNameMap.get(userId) ?? translate('tourGroups.organizer')
  }
  return memberDisplayNameMap.get(userId) ?? userId
}
