import { attractionApiClient } from './api-client/attractions'
import { advertisingApiClient } from './api-client/advertising'
import { authApiClient } from './api-client/auth'
import { contentApiClient } from './api-client/content'
import { flightApiClient } from './api-client/flights'
import { hotelApiClient } from './api-client/hotels'
import { managerApiClient } from './api-client/manager'
import { orderApiClient } from './api-client/orders'
import { tourGroupApiClient } from './api-client/tour-groups'
import { trainApiClient } from './api-client/trains'
import { userApiClient } from './api-client/users'

// 这里是前端 API 聚合入口。
// 页面通常只依赖这一个对象，而不直接拼接 URL 或感知 transport 细节。
export const travelMvpApiClient = {
  ...authApiClient,
  ...advertisingApiClient,
  ...contentApiClient,
  ...userApiClient,
  ...flightApiClient,
  ...hotelApiClient,
  ...trainApiClient,
  ...attractionApiClient,
  ...tourGroupApiClient,
  ...orderApiClient,
  ...managerApiClient,
}
