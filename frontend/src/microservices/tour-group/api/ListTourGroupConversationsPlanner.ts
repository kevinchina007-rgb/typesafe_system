// 本文件定义 tour-group 子域的前端 planner 入口，本地保留函数封装以避免纯转发壳。
import { listTourGroupConversations as listTourGroupConversationsImpl } from './TourGroupPlannerSupportConversation'

export const listTourGroupConversations = (...args: Parameters<typeof listTourGroupConversationsImpl>) => listTourGroupConversationsImpl(...args)


