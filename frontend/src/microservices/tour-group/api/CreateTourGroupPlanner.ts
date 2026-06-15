// 本文件定义 tour-group 子域的前端 planner 入口，本地保留函数封装以避免纯转发壳。
import { createTourGroup as createTourGroupImpl } from './TourGroupPlannerSupportPlanning'

export const createTourGroup = (...args: Parameters<typeof createTourGroupImpl>) => createTourGroupImpl(...args)


