# Search Module

这个模块是后端内部的共享搜索基础层，不是一个会对应前端目录的 planner 微服务。

## 这个模块负责什么

- [SearchModels.scala](/E:/typesafe/backend/src/main/scala/microservices/search/api/SearchModels.scala)：定义搜索资源类型、搜索建议和探索搜索结果模型。
- [SearchRanking.scala](/E:/typesafe/backend/src/main/scala/microservices/search/api/SearchRanking.scala)：定义关键词归一化、打分和排序工具。
- [TravelSearchAliases.scala](/E:/typesafe/backend/src/main/scala/microservices/search/api/TravelSearchAliases.scala)：定义机场、酒店地点、火车站和景点等别名匹配与查询扩展工具。

## 为什么没有前端镜像

- 这个模块只服务后端内部 planner。
- 前端不会直接调用这里的内容。
- 前端只会看到各个具体业务域的搜索入口，例如 flight / hotel / train / content 的 planner。

## 使用方式

如果别的业务域需要搜索能力，应该直接复用这里的模型和工具，而不是再新建一个前端 search 微服务目录。
