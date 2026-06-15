// 本文件定义 tour-group 子域的前端 planner 入口，本地保留函数封装以避免纯转发壳。
import { updateDirectConversationArchiveState as updateDirectConversationArchiveStateImpl } from './TourGroupPlannerSupportConversation'

export const updateDirectConversationArchiveState = (...args: Parameters<typeof updateDirectConversationArchiveStateImpl>) => updateDirectConversationArchiveStateImpl(...args)


