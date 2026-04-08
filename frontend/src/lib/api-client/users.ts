import type { TravelerListResponse, TravelerResponse, UserResponse } from '../api-dtos'
import { createSingleFileFormData, executeApiRequest, executeJsonApiRequest, executeMultipartApiRequest } from '../api-transport'

export const userApiClient = {
  createUser: (payload: { email: string; nickname: string; phone: string }): Promise<UserResponse> =>
    executeJsonApiRequest('/users', 'POST', payload),

  loginUser: (payload: { email: string }): Promise<UserResponse> =>
    executeJsonApiRequest('/session/login', 'POST', payload),

  getUser: (userId: string): Promise<UserResponse> =>
    executeApiRequest(`/users/${userId}`),

  uploadUserAvatar: (userId: string, avatarFile: File): Promise<UserResponse> =>
    executeMultipartApiRequest(`/users/${userId}/avatar`, 'POST', createSingleFileFormData('avatar', avatarFile)),

  createTraveler: (
    userId: string,
    payload: {
      fullName: string
      documentType: string
      documentNumber: string
      phone: string
      birthDate: string
      seatPreference: string
      mealPreference: string
      accessibilityRequestNotes: string | null
      emergencyContactName: string | null
      emergencyContactPhoneNumber: string | null
      isDefaultTraveler: boolean
    },
  ): Promise<TravelerResponse> =>
    executeJsonApiRequest(`/users/${userId}/travelers`, 'POST', payload),

  updateTraveler: (
    userId: string,
    travelerId: string,
    payload: {
      fullName: string
      documentType: string
      documentNumber: string
      phone: string
      birthDate: string
      seatPreference: string
      mealPreference: string
      accessibilityRequestNotes: string | null
      emergencyContactName: string | null
      emergencyContactPhoneNumber: string | null
      isDefaultTraveler: boolean
    },
  ): Promise<TravelerResponse> =>
    executeJsonApiRequest(`/users/${userId}/travelers/${travelerId}`, 'PUT', payload),

  listTravelers: (userId: string): Promise<TravelerListResponse> =>
    executeApiRequest(`/users/${userId}/travelers`),

  deleteTraveler: (userId: string, travelerId: string): Promise<void> =>
    executeApiRequest(`/users/${userId}/travelers/${travelerId}`, { method: 'DELETE' }),
}
