import type { AccountPageProps } from './objects'
import { AccountGuestSection, AccountSignedInSection } from './components'
import { useAccountPageController } from './hooks'

// 账号页入口，根据是否已登录切换不同区域。
export function AccountPage(props: AccountPageProps) {
  const controller = useAccountPageController(props)
  const { signedInUser, translate } = props

  return signedInUser ? (
    <AccountSignedInSection account={signedInUser} controller={controller} translate={translate} />
  ) : (
    <AccountGuestSection controller={controller} translate={translate} />
  )
}
