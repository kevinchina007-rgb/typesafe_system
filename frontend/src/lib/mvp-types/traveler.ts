// 本文件只汇总 traveler 域对外需要暴露的前端类型，不再承担实现逻辑。
export type { TravelerPlannerResponse, TravelerResponse } from '@/microservices/traveler/objects/TravelerPlannerResponse'
export type { TravelerListPlannerResponse } from '@/microservices/traveler/objects/TravelerListPlannerResponse'
export type { TravelerDeletedPlannerResponse } from '@/microservices/traveler/objects/TravelerDeletedPlannerResponse'
export type { CreateTravelerPlannerRequest, CreateTravelerRequest } from '@/microservices/traveler/objects/CreateTravelerPlannerRequest'
export type { UpdateTravelerPlannerRequest, UpdateTravelerRequest } from '@/microservices/traveler/objects/UpdateTravelerPlannerRequest'
export type { ListTravelersPlannerRequest } from '@/microservices/traveler/objects/ListTravelersPlannerRequest'
export type { DeleteTravelerPlannerRequest } from '@/microservices/traveler/objects/DeleteTravelerPlannerRequest'
export type {
  TravelerBasicInfo,
  TravelerContactInfo,
  TravelerDocumentInfo,
  TravelerPreferenceInfo,
  TravelerServiceSummary,
  TravelerSpecialRequirementInfo,
} from '@/microservices/traveler/objects/TravelerProfileDetails'
