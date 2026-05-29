# Codex Handoff - 2026-05-29

## Project

Workspace: `C:\typesafe\project`

User language: Chinese. The user prefers direct implementation after confirming design, concise explanations, and strict alignment with the course-style architecture.

Core architecture rules the user has emphasized:

- Backend ordinary business entry should be `PlannerRouter -> XxxPlanner -> XxxTable / XxxPlainSql -> JDBC Connection`.
- Do not add new REST ApiRoutes for ordinary business.
- Planner objects live in each business module's `api` folder and should be singleton `object`.
- Use `cats.effect.IO`, not `F[_]`.
- Use plain SQL + JDBC `Connection`, not Doobie.
- Planner owns business validation and orchestration; tables/plain SQL only do simple DB read/write.
- Frontend module/object/API naming should align with backend Planner naming.
- Avoid DTO-style duplication.

## Current Focus

The current active area is the community/blog page:

- Frontend page: `frontend/src/pages/BlogPage/index.tsx`
- Frontend content API: `frontend/src/microservices/content/api/BlogPlanners.ts`
- Frontend content objects:
  - `frontend/src/microservices/content/objects/BlogPostSummaryResponse.ts`
  - `frontend/src/microservices/content/objects/BlogPostResponse.ts`
  - `frontend/src/microservices/content/objects/BlogProfileResponse.ts`
  - `frontend/src/microservices/content/objects/BlogNotificationResponse.ts`
- Backend objects: `backend/src/main/scala/microservices/content/objects/BlogPlannerModels.scala`
- Backend planners: `backend/src/main/scala/microservices/content/api/BlogPlanners.scala`
- Backend table/plain SQL: `backend/src/main/scala/microservices/content/tables/BlogPlannerPlainSql.scala`
- Planner registration: `backend/src/main/scala/routes/PlannerDefinitions.scala`

## Community Features Already Built

Community page now has:

- Left-side blog navigation: `主页`, `发布`, `通知`, `我的`.
- Home page:
  - Search field labeled `搜索帖子`, with magnifying glass icon.
  - Filter tags for days, season/time, companion, style.
  - Multi-select travel city tags.
  - Post cards showing cover image, title, cover text, author, likes, city/tags.
- Publish page:
  - Draft fields clearly labeled: title, cover text, content, image set.
  - First image is the title cover, remaining images are body image set.
  - City multi-select.
  - Save draft and publish buttons.
- Post detail modal:
  - Opens by clicking post cover/title.
  - Left side is image carousel with circular left/right buttons.
  - Right top has author avatar/name and follow button.
  - Right middle has title, full content, tags, city, comments.
  - Right bottom has comment input, like, favorite, comment count.
- Favorites:
  - Backend table `blog_favorites` via migration `V30__blog_favorites.sql`.
  - Backend planners `FavoriteBlogPostPlanner`, `UnfavoriteBlogPostPlanner`.
  - Frontend favorite button and `我的 -> 收藏栏`.
- Notifications:
  - Three real filter buttons now exist:
    - `回复与评论`: `comment`, `reply`, `postCommented`
    - `收到喜欢`: `like`, `favorite`, `postLiked`, `postFavorited`
    - `新增粉丝`: `follow`, `userFollowed`
  - Before this fix they were static `div`s and did nothing.
- Demo user avatars:
  - Migration `V29__short_blog_demo_population.sql` was fixed to use `/images/avatar-defaults/bara-avatar-x.png`.
  - Current local database was also updated for 199 demo users and `kapiba@la.com`.

## Latest Change In Progress / Completed This Turn

The user asked:

- In `我的` page, followers/following should be viewable.
- Clicking another user's avatar should open that user's `我的` page.

Implemented backend additions:

- New backend object models in `BlogPlannerModels.scala`:
  - `BlogProfileUserResponse`
  - `BlogProfileUserListPlannerResponse`
  - `ListBlogProfileUsersPlannerRequest`
- New backend table methods in `BlogPlannerPlainSql.scala`:
  - `listFollowers`
  - `listFollowing`
  - shared helper `listProfileUsers`
- New backend planners in `BlogPlanners.scala`:
  - `ListBlogFollowersPlanner`
  - `ListBlogFollowingPlanner`
- Registered both planners in `PlannerDefinitions.scala`.
- Added `profile` scope to `ListBlogPostsPlanner` so other users' pages can show only published posts.

Implemented frontend additions:

- New frontend types in `BlogProfileResponse.ts`:
  - `BlogProfileUserResponse`
  - `BlogProfileUserListResponse`
- New frontend API methods in `BlogPlanners.ts`:
  - `listBlogFollowers`
  - `listBlogFollowing`
- Exported those in `TravelMvpApiClient.ts`.
- `BlogPage/index.tsx` additions:
  - `viewedProfile`, `viewedProfilePosts`, `profileRelationTab`, `profileRelationUsers`.
  - `openProfile(profileUserId)` loads another user's profile and published posts.
  - `openProfileRelation('followers' | 'following')` loads a list.
  - Post-card author avatar/name and notification actor avatar/name are now clickable.
  - `我的` page now can show either own page or another user's page.
  - Own page keeps `发布栏` and `收藏栏`; other user page only shows `发布栏`.

Validation completed:

- `frontend`: `npm run build` passed.
- `backend`: `sbt compile` passed. It prints the usual sbt pipe warning but ends with success.

## Important Caveats

- Some database text may appear garbled in PowerShell API output because console encoding displays UTF-8 poorly. Browser rendering is the real check.
- Backend must be restarted after planner changes before new endpoints are available in the running app.
- The current Git working tree has many prior changes from the broader project. Do not revert unrelated files.
- If continuing in a new conversation, first inspect `git status --short`, then focus on the files listed above.

## Suggested Next Steps

1. Restart the backend/frontend stable launcher if the app is already running, so `ListBlogFollowersPlanner` and `ListBlogFollowingPlanner` become live.
2. Open `http://127.0.0.1:5173/?view=blog&boot=202605172330`.
3. Verify:
   - Click author avatar/name on a post card -> other user's `我的`.
   - Click `粉丝` -> follower list appears.
   - Click `关注` -> following list appears.
   - Click a user in those lists -> their `我的`.
   - Click `回到我的主页` -> own profile.
4. If the user wants further polish, improve the profile relation list visually and add a direct unfollow action.
