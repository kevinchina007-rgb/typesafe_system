// 本文件定义 tour-group 子域的前端 planner 入口，本地保留函数封装以避免纯转发壳。
import { uploadConversationAttachment as uploadConversationAttachmentImpl } from './TourGroupPlannerSupportAttachment'

export const uploadConversationAttachment = (...args: Parameters<typeof uploadConversationAttachmentImpl>) => uploadConversationAttachmentImpl(...args)


