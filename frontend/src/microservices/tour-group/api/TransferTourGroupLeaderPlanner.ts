// 本文件定义 tour-group 子域的前端 planner 入口，本地保留函数封装以避免纯转发壳。
import { transferTourGroupLeader as transferTourGroupLeaderImpl } from './TourGroupPlannerSupportPlanning'

export const transferTourGroupLeader = (...args: Parameters<typeof transferTourGroupLeaderImpl>) => transferTourGroupLeaderImpl(...args)


