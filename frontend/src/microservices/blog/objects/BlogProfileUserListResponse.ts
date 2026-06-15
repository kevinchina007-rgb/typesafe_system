// BlogProfileUserListResponse：博客域博客主页用户列表返回对象。

import type { BlogProfileUserResponse } from './BlogProfileUserResponse'

export type BlogProfileUserListResponse = {
  users: BlogProfileUserResponse[]
}

export const blogProfileUserListResponseFromJson = (json: string): BlogProfileUserListResponse =>
  JSON.parse(json) as BlogProfileUserListResponse

export const blogProfileUserListResponseToJson = (value: BlogProfileUserListResponse): string =>
  JSON.stringify(value)