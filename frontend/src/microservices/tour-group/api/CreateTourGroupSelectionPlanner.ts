// 本文件定义 tour-group 子域的前端 planner 入口，本地保留函数封装以避免纯转发壳。
import { createTourGroupSelection as createTourGroupSelectionImpl } from './TourGroupPlannerSupportPlanning'

export const createTourGroupSelection = (...args: Parameters<typeof createTourGroupSelectionImpl>) => createTourGroupSelectionImpl(...args)


