import type { ManagerPageProps } from '../objects'
import type { ManagerPageControllerCoreState } from './useManagerPageControllerCore'
import {
  createManagerPageControllerAuthActions,
  type ManagerPageAuthActions,
} from './useManagerPageControllerAuthActions'
import {
  createManagerPageControllerAttractionTrainActions,
  type ManagerPageAttractionTrainActions,
} from './useManagerPageControllerAttractionTrainActions'
import {
  createManagerPageControllerSupplierActions,
  type ManagerPageSupplierActions,
} from './useManagerPageControllerSupplierActions'

export type ManagerPageControllerActions = ManagerPageAuthActions & ManagerPageSupplierActions & ManagerPageAttractionTrainActions

type ManagerPageActionDependencies = ManagerPageProps & ManagerPageControllerCoreState

export function createManagerPageControllerActions(deps: ManagerPageActionDependencies): ManagerPageControllerActions {
  return {
    ...createManagerPageControllerAuthActions(deps),
    ...createManagerPageControllerSupplierActions(deps),
    ...createManagerPageControllerAttractionTrainActions(deps),
  }
}
