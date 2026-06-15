// BlogSuggestionRequest：博客域博客推荐请求对象。

export type BlogSuggestionRequest = {
  q: string
}

export const blogSuggestionRequestFromJson = (json: string): BlogSuggestionRequest =>
  JSON.parse(json) as BlogSuggestionRequest

export const blogSuggestionRequestToJson = (value: BlogSuggestionRequest): string =>
  JSON.stringify(value)