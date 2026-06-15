// 本文件定义 tour-group 子域的前端 planner 入口，本地保留函数封装以避免纯转发壳。
import { recallConversationMessage as recallConversationMessageImpl } from './TourGroupPlannerSupportConversation'

export const recallConversationMessage = (...args: Parameters<typeof recallConversationMessageImpl>) => recallConversationMessageImpl(...args)


