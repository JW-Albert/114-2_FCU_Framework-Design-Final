# 企業化 Prototype 改進規劃（Enterprise Roadmap）

> 對應 Epic：[#73](https://github.com/DamnDamnDamnM3/114-2_FCU_Framework-Design-Final/issues/73)

## 目標與約束

將現有「公司車輛管理系統」由教學專題提升為**企業化 prototype application**，在不改變既有開發型態的前提下補齊企業級能力。

**約束（依需求設定）：**
- ✅ 維持**開發狀態**：持續使用 H2 記憶體資料庫（dev profile），**不導入 PostgreSQL**
- ✅ 維持**本系統帳號登入**（JWT + BCrypt），**不導入 SSO / OAuth**
- ✅ 訊息通知採**站內收件夾**，**不接 Gmail、不做瀏覽器/網頁推播**

## 現況基線

系統已具備：借車申請完整生命週期、角色權限（ADMIN / MANAGER / EMPLOYEE）、違規記錄（自動 + 手動）、報表匯出、統計儀表板，並落實多種 GoF 設計模式（State / Observer / Strategy / Factory Method / Adapter / Template Method / Builder / Chain of Responsibility / Decorator / Command）。

## 改進主軸

| # | 主軸 | 議題 | 狀態 |
|---|------|------|------|
| 1 | **通訊協作** | 收件夾通知中心（站內訊息 + 未讀狀態燈）[#74](https://github.com/DamnDamnDamnM3/114-2_FCU_Framework-Design-Final/issues/74) | ✅ 本次交付 |
| 2 | 可觀測性 | 稽核日誌持久化與查詢 [#75](https://github.com/DamnDamnDamnM3/114-2_FCU_Framework-Design-Final/issues/75) | 規劃 |
| 3 | 可觀測性 | API 文件（OpenAPI / Swagger UI）[#76](https://github.com/DamnDamnDamnM3/114-2_FCU_Framework-Design-Final/issues/76) | 規劃 |
| 4 | 可觀測性 | 系統健康監控（Spring Boot Actuator）[#77](https://github.com/DamnDamnDamnM3/114-2_FCU_Framework-Design-Final/issues/77) | 規劃 |
| 5 | 安全治理 | 帳號安全強化（密碼政策 + 登入失敗鎖定）[#78](https://github.com/DamnDamnDamnM3/114-2_FCU_Framework-Design-Final/issues/78) | 規劃 |
| 6 | 資料治理 | 軟刪除與資料保留政策 [#79](https://github.com/DamnDamnDamnM3/114-2_FCU_Framework-Design-Final/issues/79) | 規劃 |

## 本次交付：收件夾通知中心（#74）

### 設計理念
**沿用既有 Observer Pattern 擴展，不修改 `BorrowingService`**（OCP 實證）。原本只有 `EmailNotificationObserver`（印 console）；新增 `InboxNotificationObserver` 將事件寫入收件夾資料表，兩個觀察者並存。

### 通知流
| 事件 | 觸發 | 收件人 |
|------|------|--------|
| 送出申請 | `submitRequest` → `onSubmitted`（新增事件） | 可審核者（ADMIN + 同部門 MANAGER） |
| 核准 / 拒絕 | `approveRequest` / `rejectRequest` | 申請人 |
| 出車 / 還車 | `startUse` / `completeUse` | 申請人 |

### 元件
**後端**
- `Notification`（domain）/ `NotificationEntity`（H2 dev 自動建表，prod 由 `V6__notifications.sql`）
- `INotificationRepository` + `NotificationRepositoryAdapter`（Adapter Pattern）
- `NotificationService`：建立、查詢、未讀計數、標記已讀
- `InboxNotificationObserver`：實作擴充後的 `BorrowingEventObserver`（含新事件 `onSubmitted`）
- `NotificationController`：`GET /api/notifications`、`GET /unread-count`、`POST /{id}/read`、`POST /read-all`

**前端**
- `InboxView.vue`（`/inbox`）：收件夾列表、未讀高亮、點擊已讀、全部已讀
- 導覽列**狀態燈**：收件夾圖示 + 未讀紅點 badge（所有角色），登入後每 30 秒輪詢未讀數
- `stores/notifications.ts`（Pinia）：共享未讀數，標記已讀後即時同步狀態燈

### 驗收
- 員工送出申請 → 管理者/主管收件夾出現未讀、狀態燈亮
- 管理者核准 → 員工收件夾出現未讀
- 標記已讀 → 狀態燈未讀數同步歸零
- 全程維持 H2 dev 模式，無需 PostgreSQL
