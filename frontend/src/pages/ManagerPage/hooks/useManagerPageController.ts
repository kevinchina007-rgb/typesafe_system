import type { ManagerPageController, ManagerPageProps } from '../objects'
import { createManagerPageControllerActions } from './useManagerPageControllerActions'
import { useManagerPageControllerCore } from './useManagerPageControllerCore'

export function useManagerPageController(props: ManagerPageProps): ManagerPageController {
  const core = useManagerPageControllerCore(props)
  const actions = createManagerPageControllerActions({
    ...props,
    ...core,
    onManagerSessionChange: props.onManagerSessionChange,
    onSignedInUserChange: props.onSignedInUserChange,
  })

  return {
    ...core,
    ...actions,
  }
}
