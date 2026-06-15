// 本文件定义 tour-group 子域的前端 planner 入口，本地保留函数封装以避免纯转发壳。
import { submitTourGroupSelection as submitTourGroupSelectionImpl } from './TourGroupPlannerSupportPlanning'

export const submitTourGroupSelection = (...args: Parameters<typeof submitTourGroupSelectionImpl>) => submitTourGroupSelectionImpl(...args)


