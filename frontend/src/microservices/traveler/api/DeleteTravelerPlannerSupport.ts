import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const deleteTraveler = (userId: string, travelerId: string): Promise<{ deleted: boolean; hidden: boolean }> =>
  executeJsonApiRequest('/DeleteTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    travelerId,
  })
