# 当前网站整体情况说明（交接给下一个 Codex）

更新时间：2026-04-21
项目路径：仓库根目录（当前工作区）
前端地址：`http://localhost:5174`
后端健康检查：`http://127.0.0.1:19095/api/health`

## 1. 项目整体定位
这是一个 Scala 3 全栈旅行平台，前后端都已存在较多模块，当前目标风格是：
- type-safe
- declarative
- modular
- 前后端 DTO / API 严格对齐
- 前端共享状态尽量统一到 Zustand

当前已有主要业务域：
- 账户 / 登录 / 会话
- 出行人
- 航班
- 酒店
- 火车票
- 景点
- 旅游团
- 订单
- 博客
- 评价
- 客服反馈
- 广告投放与审核
- 智能行程（结构已开始，但不是完整产品）

## 2. 技术现状
### 后端
- 语言：Scala 3
- 路由：http4s 风格
- 数据库：PostgreSQL（当前运行链依赖本地 5432）
- 多个 domain 已被分为：
  - `api/`
  - `objects/`
  - `tables/` / repository
  - `service/`（部分域仍在继续整理）

### 前端
- 技术栈：React + TypeScript + Vite
- 共享状态：一部分已迁移到 Zustand
- 页面结构已经开始往以下目录分：
  - `models/`
  - `stores/`
  - `hooks/`
  - `sections/`
  - `components/`
  - `dialogs/`

## 3. 当前已经完成得比较明确的功能
### 3.1 用户侧主流程
已实现或基本可用：
- 用户注册 / 登录 / 用户个人中心
- 出行人创建与维护
- 航班 / 酒店 / 火车票 / 景点的搜索与预订页
- 订单中心（查看订单、支付、退款流程）
- 客服反馈页面

### 3.2 管理者侧基础结构
已实现或基本可见：
- 管理者中心独立于用户个人中心
- 管理者分类卡片：
  - 航空经理
  - 酒店经理
  - 火车经理
  - 景点经理
  - 网站管理者
- 登录后，管理者拥有与用户不同的主导航
- 酒店经理 / 景点经理已有广告提交入口
- 网站管理者已有广告审核入口（前端和后端链路都写过）

### 3.3 广告系统（已经写入代码，但需要重新完整验证）
已存在的后端模块：
- `backend/src/main/scala/advertising-domain/...`
- `backend/src/main/scala/routes/AdvertisementApiRoutes.scala`
- 数据库迁移：`V21__advertising_phase1.sql`

已存在的前端模块：
- `frontend/src/lib/api-dtos/advertising.ts`
- `frontend/src/lib/api-client/advertising.ts`
- `frontend/src/app/stores/advertising-store.ts`
- `frontend/src/components/advertising/sections/AdvertisementSubmissionWorkspace.tsx`
- `frontend/src/components/advertising/sections/AdvertisementReviewWorkspace.tsx`
- `frontend/src/components/advertising/sections/AdvertisementCardRail.tsx`

已做过的功能：
- 经理提交广告
- 网站管理者审核广告
- 用户端酒店页 / 景点页广告卡片展示
- 广告图片上传（不再只是填图片 URL）
- 选择图片后会显示本地预览
- 广告提交成功会弹提示

但是：
- 我后来直接查数据库时，`advertisements = 0`，`advertisement_reviews = 0`
- 这说明在最近一次清库前，没有任何广告成功进入数据库
- 所以这条链虽然代码写了，但在交接时应视为：
  - **代码存在**
  - **数据库表存在**
  - **前端入口存在**
  - **是否真正端到端可用，需要重新验证**

### 3.4 客服反馈 / 聊天链路
当前已经写过并接入过真实后端接口：
- 前端：
  - `frontend/src/pages/CustomerFeedbackPage/index.tsx`
  - `frontend/src/components/feedback/FeedbackConversationWorkspace.tsx`
  - `frontend/src/app/stores/feedback-chat-store.ts`
- 后端：
  - 反馈线程 / 消息相关 route 与数据库表已存在

已做过的目标：
- 用户点击订单里的“评价/Write review”后，跳转到“客服反馈”页面
- 不再使用页面中间的小弹窗聊天框
- `客服反馈` 页面承载全屏评价 + 聊天工作区
- 管理者和网站管理者可看对应反馈线程

但这部分曾多次返工，交接时建议重新重点核验：
1. 订单页点击评价是否一定跳到 `customerFeedback`
2. 已评价过的订单项是否不会重复报错
3. `客服反馈` 顶部导航是否对普通用户可见
4. 页面是否仍有中英文混杂

## 4. 当前已经明确存在的问题 / 风险
### 4.1 博客审核并未真正接到后端
这是一个非常重要的事实：
- 前端有 `网站管理者 -> 博客审核` 导航入口
- 但后端并没有真正完整的博客审核微服务 / route / 审核动作链
- 也就是说：
  - **博客审核当前更像占位页面**
  - **不是完整可演示的真实后台功能**

如果下一个 Codex 要继续做网站管理者能力，这一块必须优先补齐。

### 4.2 中英文混杂问题仍然没有彻底收干净
曾经多次修过：
- `frontend/src/lib/i18n/chinese.ts`
- `frontend/src/lib/i18n/english.ts`
- 各页面本地 model 里的硬编码数据

目前状态：
- 大量核心页面已经补过中文
- 但用户多次反馈仍有中英文混杂
- 特别需要重新逐页检查：
  - 航班页
  - 酒店页
  - 火车票页
  - 景点页
  - 智能行程
  - 管理者页面
  - 评价 / 客服反馈

不要假设 `chinese.ts` 一次修完就好了，很多页面还有本地英文常量。

### 4.3 网站管理者注册问题的真实根因
我已经查清：
- 之前网站管理者无法注册，不是接口没写，而是**本地 PostgreSQL 没启动**
- 当时后端虽然 `/api/health` 是绿的，但数据库连接写入超时
- 我后来启动了本地 PostgreSQL 开发实例后，接口实测：
  - `POST /api/manager/site-admin/register` 返回 `201`
  - 所以注册链本身是通的

结论：
- 网站管理者注册问题本质是**运行环境问题**
- 不是这条业务代码完全没写
- 但前端对错误提示仍然比较粗糙，建议补强

### 4.4 广告链需要重新做端到端验证
原因：
- 代码有
- 数据表有
- 路由有
- 前端页面有
- 但清库前数据库里没有任何广告数据

所以交接时要把它视为：
- **半完成**
- 不能只看代码文件就默认可用
- 需要重新测试：
  1. 酒店经理提交广告
  2. 是否入库到 `advertisements`
  3. 网站管理者是否能在待审核列表看到
  4. 审核通过后是否能在用户酒店页/景点页看到卡片

### 4.5 客服反馈与评价链也需要重新回归测试
尤其是：
- 新建评价
- 已存在评价
- 跳转客服反馈
- 创建反馈线程
- 聊天消息是否写入后端
- 管理者页是否能看到对应线程

## 5. 已做过的结构整理（对下一个 Codex 有帮助）
### 5.1 前端 Zustand 已经开始落地
已收进 store 的主要状态包括：
- app shell 状态
- 语言 / 主题 / 当前页面
- 用户会话 / 管理者会话
- 资源页搜索条件（flight / hotel / train / attraction）
- 反馈聊天 store
- 广告 store

建议继续保持这个方向，不要又回到一堆 scattered `useState`。

### 5.2 多个 domain 已被持续收整
已经推进过或动过的域包括：
- `flight-domain`
- `hotel-domain`
- `train-domain`
- `attraction-domain`
- `identity-domain`
- `operations-domain`
- `inventory-domain`
- `auth-domain`
- `content-domain`
- `traveler-domain`
- `planner-domain`
- `order-domain`
- `shared-kernel`
- `tour-group-domain`

注意：
- 并不代表全部完美完成
- 只是说明已经被多轮调整过
- 后续改动前一定要先读现状，不要按老印象改

## 6. 本地数据库当前状态（非常重要）
我已经按用户要求把开发库业务数据清空了。
当前数据库确认如下：
- `users = 0`
- `orders = 0`
- `blog_posts = 0`
- `reviews = 0`
- `hotels = 0`
- `attractions = 0`
- `flights = 0`
- `trains = 0`
- `feedback_threads = 0`
- `advertisements = 0`
- `advertisement_reviews = 0`

说明：
- 当前非常适合重新造一批干净的演示数据
- 但任何页面如果依赖初始数据，现在都可能看起来是空的

## 7. 当前最适合下一个 Codex 继续做的事情
建议按优先级：

### 第一优先级：准备可演示环境
1. 重新造一套演示数据：
- 普通用户
- 酒店经理
- 景点经理
- 网站管理者
- 至少一条酒店、一条景点、一条广告、一条反馈线程
- 至少一条订单

2. 验证广告完整链路：
- 提交
- 审核
- 展示

3. 验证评价 -> 客服反馈完整链路：
- 评价跳转
- 线程创建
- 聊天展示

### 第二优先级：补完网站管理者能力
1. 真实博客审核后端
2. 广告审核列表可视化完善
3. 网站管理者沟通页再核验

### 第三优先级：中英文清理
逐页做，不要只改词典：
- flight
- hotel
- train
- attraction
- smart planner
- customer feedback
- manager center

## 8. 交接提醒
请下一个 Codex 不要做这些误判：
1. 不要因为看到前端页面存在，就默认后端一定实现了（博客审核就是反例）
2. 不要因为看到后端接口存在，就默认前端提交已经真正入库（广告链就是反例）
3. 不要因为 `/api/health` 是绿的，就默认数据库一定可写（网站管理者注册问题就是反例）
4. 不要因为词典文件改过一次，就默认全站中文已经修好（很多页面有本地英文常量）

## 9. 一句话总结
当前网站已经具备一个相当完整的旅行平台骨架，用户侧、管理者侧、广告、反馈、订单、资源预订等核心模块都已经有基础结构，部分链路甚至已有完整前后端代码；但中期状态下最大的问题不是“完全没做”，而是：
- 有些链路写了代码但没有完成真实验证
- 有些功能只有前端骨架没有真正后端闭环
- 中英文和交互一致性还没有收干净
- 现在数据库已经被清空，适合重新造一批干净的演示数据和走查关键流程
