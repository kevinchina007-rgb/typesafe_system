// 这个文件是 tour-group 后端聊天 SQL 的对外聚合入口。
// 它只是把 conversation、list messages、send message、mark read、search 这几块内部 SQL 功能重新导出，方便 route/planner 调用。
// 前端不需要镜像这个文件；前端只需要同名 API 文件和 request/response 对象。
package com.typesafe.travel.tourgroup.domain

object TourGroupChatPlainSql:
  export ConversationPlainSql.*
  export ListMessagesPlainSql.*
  export SendMessagePlainSql.*
  export MarkReadPlainSql.*
  export SearchMessagesPlainSql.*
