// 这个文件是 tour-group 后端聊天域的总 support 汇总。
// 它汇总了会话、消息、读状态、搜索、附件、会话设置等共享查询与更新逻辑，供更薄的 planner/table 文件调用。
// 前端不应镜像这个文件；它只用于后端内部编排和 SQL 共享。
package com.typesafe.travel.tourgroup.domain

object TourGroupChatPlainSqlSupport:
  export TourGroupChatMessagePlainSqlSupport.*
  export TourGroupConversationPlainSqlSupport.*
  export TourGroupMemberPlainSqlSupport.*
  export TourGroupAttachmentPlainSqlSupport.*
