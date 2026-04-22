import { attractionApiClient } from './attractions'
import { hotelApiClient } from './hotels'
import { trainApiClient } from './trains'

export const resourceApiClient = {
  ...hotelApiClient,
  ...trainApiClient,
  ...attractionApiClient,
}
