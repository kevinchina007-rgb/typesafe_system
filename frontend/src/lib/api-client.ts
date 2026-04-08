import { authApiClient } from './api-client/auth'
import { contentApiClient } from './api-client/content'
import { managerApiClient } from './api-client/manager'
import { orderApiClient } from './api-client/orders'
import { resourceApiClient } from './api-client/resources'
import { tourGroupApiClient } from './api-client/tour-groups'
import { userApiClient } from './api-client/users'

// 这里是前端 API 聚合入口。
// 页面通常只依赖这个对象，而不直接拼接 URL 或感知 transport 细节。
export const travelMvpApiClient = {
  ...authApiClient,
  ...contentApiClient,
  ...userApiClient,
  ...resourceApiClient,
  ...tourGroupApiClient,
  ...orderApiClient,
  ...managerApiClient,
}
