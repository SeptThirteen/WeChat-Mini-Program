# WeChat Mini Program — AI Improvement To-Do List

## High Priority

- [x] Fix `localStorage` → `uni.setStorageSync` in `store/user.js` and `api/request.js`
      → Already correct in original code; verified.
- [x] Add 401 handling + auto-logout in `api/request.js`
      → Clears token and redirects to login on 401/403.
- [x] Add logout button to profile page (`pages/user/profile.vue`)
      → Confirmation dialog + `userStore.logout()` call added.
- [x] Implement bill query page (`pages/query/index.vue`) — backend is ready
      → Full UI: type selector (电费/水费/有线电视), account input, result display, query history.
- [x] Add loading / error / empty states to all pages
      → Added to index, order list, order detail, profile pages.

## Medium Priority

- [x] Order cancel UI (`pages/order/list.vue` + `api/order.js`)
      → Confirmation modal + `cancelOrder()` API call on CREATED orders.
- [x] Order rate UI (`pages/order/list.vue` + `api/order.js`)
      → Action sheet (1–5 stars) + `rateOrder()` API call on COMPLETED orders.
- [x] Order detail page (`pages/order/detail.vue` + `api/order.js`)
      → New page showing full order info with cancel/rate actions; registered in pages.json.
- [x] Input validation on login (`pages/login/login.vue`)
      → Validates 11-digit phone (regex ^1\d{10}$) and 6-digit code; loading state added.
- [x] Use `ServiceCard` and `OrderCard` components properly
      → `index.vue` now uses <ServiceCard> with @click. `order/list.vue` uses <OrderCard>
         with @click-detail, @cancel, @rate. Inline duplication removed.
- [x] Externalize backend URL config
      → Created `api/config.js`; `request.js` imports BASE_URL from it.

## Low Priority

- [x] Match tab bar colors to warm theme in `pages.json`
      → selectedColor: #e76f51, color: #7d6b66, backgroundColor: #fff7f3.
      → Also added "查询" as 4th tab so the bill query page is directly reachable.
- [x] Add pagination to order and service lists
      → Client-side pagination (page size 5) on both service list and order list.
         Prev/next buttons; resets to page 1 on fresh load.
- [x] Profile editing (`pages/user/profile.vue`)
      → Inline edit mode for name and age; validates input before calling
         PUT /api/user/profile via new updateUserProfile() in api/user.js.
