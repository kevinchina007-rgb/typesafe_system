import { AttractionAdminPanel } from '@/pages/ManagerPage/components/managers/AttractionAdminPanel'
import { ManagerPanel } from '@/pages/ManagerPage/components/managers/ManagerPanel'
import { TrainAdminPanel } from '@/pages/ManagerPage/components/managers/TrainAdminPanel'
import type { AppLanguage, AttractionAdminSessionResponse, ManagerRefundTaskResponse, ManagerSessionResponse, ManagerTaskResponse, TrainAdminSessionResponse } from '@/lib/mvp-types/index'
import type { ManagerFlightPlannerResponse } from '@/lib/mvp-types/manager'

// 管理后台不同业务面板的分发层，按是否展示来决定渲染哪个面板。
type BasePanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  translate: (translationKey: string) => string
}

type SupplierManagerPanelSectionProps = BasePanelProps & {
  isVisible: boolean
  managerSession: ManagerSessionResponse | null
  managedFlights: ManagerFlightPlannerResponse[]
  managedHotels: Parameters<typeof ManagerPanel>[0]['managedHotels']
  managerTasks: ManagerTaskResponse[]
  managerRefundTasks: ManagerRefundTaskResponse[]
  initialAirlineSection?: Parameters<typeof ManagerPanel>[0]['initialAirlineSection']
  onRegisterAirlineManager: Parameters<typeof ManagerPanel>[0]['onRegisterAirlineManager']
  onRegisterHotelManager: Parameters<typeof ManagerPanel>[0]['onRegisterHotelManager']
  onCreateManagerRoomType: Parameters<typeof ManagerPanel>[0]['onCreateManagerRoomType']
  onLoginManager: Parameters<typeof ManagerPanel>[0]['onLoginManager']
  onValidationError: Parameters<typeof ManagerPanel>[0]['onValidationError']
  onReloadTasks: Parameters<typeof ManagerPanel>[0]['onReloadTasks']
  onReloadRefundTasks: Parameters<typeof ManagerPanel>[0]['onReloadRefundTasks']
  onCreateManagerFlight: Parameters<typeof ManagerPanel>[0]['onCreateManagerFlight']
  onToggleManagerFlightStatus: Parameters<typeof ManagerPanel>[0]['onToggleManagerFlightStatus']
  onSearchManagerFlights: Parameters<typeof ManagerPanel>[0]['onSearchManagerFlights']
  onLoadManagerFlightOrders: Parameters<typeof ManagerPanel>[0]['onLoadManagerFlightOrders']
  onUpdateAirlineManagerProfile: Parameters<typeof ManagerPanel>[0]['onUpdateAirlineManagerProfile']
  onUpdateHotelManagerProfile: Parameters<typeof ManagerPanel>[0]['onUpdateHotelManagerProfile']
  onConfirmTask: Parameters<typeof ManagerPanel>[0]['onConfirmTask']
  onRejectTask: Parameters<typeof ManagerPanel>[0]['onRejectTask']
  onBatchConfirmTasks: Parameters<typeof ManagerPanel>[0]['onBatchConfirmTasks']
  onBatchRejectTasks: Parameters<typeof ManagerPanel>[0]['onBatchRejectTasks']
  onApproveRefundTask: Parameters<typeof ManagerPanel>[0]['onApproveRefundTask']
  onRejectRefundTask: Parameters<typeof ManagerPanel>[0]['onRejectRefundTask']
  onLogoutManager: Parameters<typeof ManagerPanel>[0]['onLogoutManager']
}

export function SupplierManagerPanelSection({
  isVisible,
  ...props
}: SupplierManagerPanelSectionProps) {
  if (!isVisible) {
    return null
  }

  return <ManagerPanel {...props} />
}

type TrainManagerPanelSectionProps = BasePanelProps & {
  isVisible: boolean
  trainAdminSession: TrainAdminSessionResponse | null
  onRegisterRailwayManager: Parameters<typeof TrainAdminPanel>[0]['onRegisterRailwayManager']
  onLoginRailwayManager: Parameters<typeof TrainAdminPanel>[0]['onLoginRailwayManager']
  onValidationError: Parameters<typeof TrainAdminPanel>[0]['onValidationError']
  onReloadManagedTrains: Parameters<typeof TrainAdminPanel>[0]['onReloadManagedTrains']
  onCreateTrainJourney: Parameters<typeof TrainAdminPanel>[0]['onCreateTrainJourney']
  onLogoutRailwayManager: Parameters<typeof TrainAdminPanel>[0]['onLogoutRailwayManager']
}

export function TrainManagerPanelSection({
  isVisible,
  ...props
}: TrainManagerPanelSectionProps) {
  if (!isVisible) {
    return null
  }

  return <TrainAdminPanel {...props} />
}

type AttractionManagerPanelSectionProps = BasePanelProps & {
  isVisible: boolean
  attractionAdminSession: AttractionAdminSessionResponse | null
  onRegisterAttractionManager: Parameters<typeof AttractionAdminPanel>[0]['onRegisterAttractionManager']
  onLoginAttractionManager: Parameters<typeof AttractionAdminPanel>[0]['onLoginAttractionManager']
  onValidationError: Parameters<typeof AttractionAdminPanel>[0]['onValidationError']
  onReloadManagedAttractions: Parameters<typeof AttractionAdminPanel>[0]['onReloadManagedAttractions']
  onCreateAttraction: Parameters<typeof AttractionAdminPanel>[0]['onCreateAttraction']
  onCreateTicketType: Parameters<typeof AttractionAdminPanel>[0]['onCreateTicketType']
  onCreateSession: Parameters<typeof AttractionAdminPanel>[0]['onCreateSession']
  onCreateRule: Parameters<typeof AttractionAdminPanel>[0]['onCreateRule']
  onLogoutAttractionManager: Parameters<typeof AttractionAdminPanel>[0]['onLogoutAttractionManager']
}

export function AttractionManagerPanelSection({
  isVisible,
  ...props
}: AttractionManagerPanelSectionProps) {
  if (!isVisible) {
    return null
  }

  return <AttractionAdminPanel {...props} />
}
