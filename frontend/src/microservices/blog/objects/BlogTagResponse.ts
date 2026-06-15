// BlogTagResponse：博客域博客标签返回对象。

export type BlogTagResponse = {
  tagType: string
  tagValue: string
}

export const blogTagResponseFromJson = (json: string): BlogTagResponse =>
  JSON.parse(json) as BlogTagResponse

export const blogTagResponseToJson = (value: BlogTagResponse): string =>
  JSON.stringify(value)