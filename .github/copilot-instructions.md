# Copilot Instructions for WeChat-Mini-Program

## Project layout and boundaries
- This workspace has two deployable apps:
  - `wechat-elderly-mini` (uni-app + Vue 3 + Pinia) for WeChat mini program/H5 UI.
  - `wechat-elderly-service` (Spring Boot 3 + MyBatis-Plus + MySQL) for REST APIs.
- Frontend-to-backend integration is direct HTTP via `api/request.js`; no BFF layer.
- Keep feature changes mirrored across both sides when touching API contracts.

## Backend architecture (Spring Boot)
- Use controller → service → mapper layering under `com.example.elderly`.
- Return payloads through `ApiResponse<T>` (`code/message/data`), not raw entities in new endpoints.
- Throw `BusinessException(code, message)` for business failures; `GlobalExceptionHandler` maps validation and unknown exceptions.
- JWT auth is enforced by `AuthInterceptor` on `/api/**` except paths in `WebConfig` excludes.
- JWT claims currently include `userId` and `phone`; token creation/parsing is centralized in `JwtUtil`.
- Persistence uses MyBatis-Plus `BaseMapper` with entity/table mapping (`users`, `services`, `orders`, `bill_queries`).
- `Order.status` values used in UI/backend: `CREATED`, `COMPLETED`, `RATED`, `CANCELLED`.

## Frontend architecture (uni-app)
- API calls must go through `api/request.js` so Authorization header + 401 handling stay consistent.
- Request success expectation is backend envelope with `code === 0`; non-zero is treated as reject.
- Auth state is in Pinia store `store/user.js` and persisted in `uni` storage (`token`, `userId`).
- Pages are task-oriented (service list/detail, order list/detail, query, profile) and use lightweight local state.
- UI tokens for elder-friendly style come from `styles/theme.scss` and `uni.scss`; reuse these variables.

## Critical integration notes (project-specific)
- Backend public routes are currently `/api/auth/login`, `/api/service/list`, `/api/service/{id}`, `/api/health`.
- `/api/bill/query` requires auth + `userId` (`@NotNull`), so anonymous calls will fail.
- Profile update backend route is `/api/user/update` (not `/api/user/profile`); align frontend API modules with controller mappings.
- Bill query service currently returns mocked amount/month and stores snapshot as string; do not assume full billing engine exists.
- `api/config.js` base URL is environment-specific and often must be changed for local LAN debugging.

## Developer workflow
- Frontend (from `wechat-elderly-mini`):
  - Install deps: `npm install`
  - WeChat dev: `npm run dev:mp-weixin`
  - H5 dev: `npm run dev:h5`
  - Build: `npm run build:mp-weixin` / `npm run build:h5`
- Backend (from `wechat-elderly-service`):
  - Run app: `mvn spring-boot:run`
  - Package: `mvn clean package`
  - Java version target: 17
- Database bootstrap: execute `src/main/resources/db/init.sql` before first run.
- API docs/UI: `/swagger-ui.html`; OpenAPI JSON at `/api-docs`.

## Change guidance for AI agents
- When adding endpoints, update all three layers (controller/service/mapper) and keep `ApiResponse` envelope compatibility.
- When changing endpoint paths/params, update matching module in frontend `api/*.js` and affected pages in the same task.
- Prefer preserving existing Chinese user-facing messages and status labels.
- Do not introduce new global state management patterns; extend Pinia `useUserStore` only if auth/user scope is required.
- Keep edits minimal and consistent with current simple architecture (no premature abstractions).
