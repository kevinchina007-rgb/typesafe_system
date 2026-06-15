// 本文件定义 tour-group 子域的前端 planner 入口，本地保留函数封装以避免纯转发壳。
import { removeConversationReaction as removeConversationReactionImpl } from './TourGroupPlannerSupportConversation'

export const removeConversationReaction = (...args: Parameters<typeof removeConversationReactionImpl>) => removeConversationReactionImpl(...args)


