# 公司車輛管理系統

> 114-2 逢甲大學 軟體框架設計 期末專題  
> Corporate Vehicle Management System

## 專案成員

| 學號 | 姓名 | 負責範疇 |
|------|------|----------|
| D1210799 | 王建葦 | 系統架構設計、OOD 原則應用、後端 API 實作 |
| D1249623 | 陳稚翔 | 前端 Vue 3 實作、資料庫設計、整合測試 |

---

## 系統簡介

本系統為企業內部車輛借用管理平台，提供完整的車輛申請、審核、出車、還車工作流程，以及車輛保養紀錄管理功能。系統以物件導向設計原則（SOLID、迪米特法則）與多種 GoF 設計模式為核心架構依據。

### 核心功能

| 功能 | 說明 |
|------|------|
| 使用者認證 | JWT 無狀態認證，BCrypt 密碼加密 |
| 車輛管理 | 車輛 CRUD、狀態追蹤（可用 / 使用中 / 維修中） |
| 借車申請 | 時段衝突檢查、申請 → 審核 → 出車 → 還車完整流程 |
| 審核工作流 | 管理員核准 / 拒絕、備注填寫、全部記錄查詢 |
| 保養管理 | 保養紀錄新增 / 查詢、到期日提醒 |

---

## 技術選型

| 層級 | 技術 | 版本 |
|------|------|------|
| 後端框架 | Spring Boot | 3.3.4 |
| 安全認證 | Spring Security + JJWT | 0.12.6 |
| 資料庫 | PostgreSQL + Spring Data JPA | — |
| DB 遷移 | Flyway | — |
| 前端框架 | Vue 3 + TypeScript | — |
| 狀態管理 | Pinia | 2.x |
| HTTP 客戶端 | Axios | 1.x |
| 前端建構 | Vite | 5.x |

---

## 系統架構

### 分層架構

```
┌─────────────────────────────────────────────────┐
│  Presentation Layer  (Vue 3 SPA + REST API)     │
│  LoginView / EmployeeBorrowView / Admin Views   │
│  AuthController / VehicleController / ...       │
├─────────────────────────────────────────────────┤
│  Service Layer                                   │
│  BorrowingService / VehicleService /            │
│  MaintenanceService / UserService               │
├─────────────────────────────────────────────────┤
│  Domain Layer                                    │
│  BorrowingRequest (State Pattern)               │
│  Vehicle / User / MaintenanceRecord             │
│  Role / Permission (組合模式)                   │
├─────────────────────────────────────────────────┤
│  Repository Interface Layer (DIP)               │
│  IVehicleRepository / IBorrowingRepository / … │
├─────────────────────────────────────────────────┤
│  Infrastructure Layer                            │
│  JPA Adapters / JWT Security / Flyway Migration │
└─────────────────────────────────────────────────┘
```

### 專案結構

```
├── backend/
│   └── src/
│       ├── main/java/com/vehicle/management/
│       │   ├── api/
│       │   │   ├── controller/      # REST Controllers + GlobalExceptionHandler
│       │   │   └── dto/             # Request / Response records
│       │   ├── domain/
│       │   │   ├── model/           # BorrowingRequest, Vehicle, User, MaintenanceRecord
│       │   │   ├── state/           # State Pattern: BorrowingState, PendingState, …
│       │   │   ├── role/            # Role, AdminRole, EmployeeRole, RoleFactory, Permission
│       │   │   ├── observer/        # BorrowingEventPublisher, EmailNotificationObserver
│       │   │   └── strategy/        # ConflictCheckStrategy, StrictOverlapStrategy
│       │   ├── service/             # BorrowingService, VehicleService, …
│       │   ├── repository/          # IVehicleRepository, IBorrowingRepository, …
│       │   │   └── inmemory/        # InMemory 實作（單元測試用）
│       │   └── infrastructure/
│       │       ├── persistence/     # JPA Entities + Repository Adapters
│       │       └── security/        # JwtUtil, JwtAuthFilter, SecurityConfig
│       └── test/java/com/vehicle/management/
│           ├── unit/                # BorrowingServiceTest, VehicleServiceTest, …
│           └── integration/         # BorrowingControllerTest (WebMvcTest)
└── frontend/
    └── src/
        ├── api/             # auth.ts / vehicles.ts / borrowings.ts / maintenance.ts
        ├── stores/          # auth.ts (Pinia)
        ├── router/          # index.ts
        └── views/           # LoginView / EmployeeBorrowView / Admin views
```

---

## 設計模式應用

### State Pattern（Ch19）— 借車申請生命週期

`BorrowingRequest` 持有一個 `BorrowingState` 介面，狀態轉換邏輯封裝於各 ConcreteState 類別，避免在 Service 層堆積 `if-else` 判斷。

```
[PendingState]
    ├─ approve() ──→ [ApprovedState]
    │                    └─ startUse() ──→ [InUseState]
    │                                          └─ complete() ──→ [ReturnedState]
    └─ reject()  ──→ [RejectedState]
```

非法的狀態轉換（例如從 `InUseState` 呼叫 `approve()`）會拋出 `InvalidStateTransitionException`，由 `GlobalExceptionHandler` 映射為 HTTP 422。

### Observer Pattern（Ch20）— 借車事件通知

`BorrowingService` 繼承 `BorrowingEventPublisher`（Subject），當申請狀態變更時廣播通知所有已註冊的 `BorrowingEventObserver`（如 `EmailNotificationObserver`）。Service 層不感知具體通知實作，符合 DIP。

### Strategy Pattern（Ch18）— 時段衝突檢查

`BorrowingService` 注入 `ConflictCheckStrategy` 介面，預設使用 `StrictOverlapStrategy`（嚴格重疊檢查）。未來可替換為寬鬆策略（如僅同一天才衝突）而不修改 Service。

### Factory Method（Ch11）— 角色建立

`RoleFactory.create(roleName)` 集中管理 `Role` 物件的建立，呼叫端只依賴 `Role` 介面，不感知 `AdminRole` / `EmployeeRole` 的存在（OCP）。

### Adapter Pattern（Ch14）— Repository 橋接

`VehicleRepositoryAdapter`、`BorrowingRepositoryAdapter` 等類別將 Spring Data JPA 介面轉換為 Domain 所需的 `IVehicleRepository` 等介面，Service 層完全不感知 JPA 存在（DIP）。

---

## OOD 原則應用

| 原則 | 應用位置 |
|------|----------|
| **SRP** | Service 層分為 BorrowingService / VehicleService / MaintenanceService / UserService，各自只有單一修改理由 |
| **OCP** | `Role` 介面 + Factory Method，新增角色不需修改既有程式碼；State Pattern 同理 |
| **DIP** | 所有 Service 依賴 Repository 介面而非 JPA 實作；測試注入 InMemory 實作 |
| **LoD** | Service 呼叫 `request.approve()` 委派給 State，不直接操作狀態字串 |
| **組合優於繼承** | `User` 持有 `Set<Role>`，`Role` 持有 `Set<Permission>`，角色以組合方式設計 |

---

## 快速啟動

### 環境需求

- Java 21+
- Maven 3.9+
- PostgreSQL 14+
- Node.js 18+

### 後端

```bash
# 1. 建立資料庫
createdb vehicle_mgmt

# 2. 設定環境變數（或修改 application.yml）
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=at-least-32-characters-long-secret

# 3. 啟動後端（Flyway 自動執行 DB 遷移）
cd backend
mvn spring-boot:run
# API 服務啟動於 http://localhost:8080
```

### 前端

```bash
cd frontend
npm install
npm run dev
# 前端啟動於 http://localhost:5173
```

### 預設測試帳號（需自行透過 API 建立）

```bash
# 建立管理員帳號
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"管理員","email":"admin@company.com","password":"Admin1234","role":"ADMIN"}'

# 建立員工帳號
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"員工甲","email":"emp@company.com","password":"Emp1234","role":"EMPLOYEE"}'
```

---

## API 文件

所有需要認證的端點請在 Header 加入：  
`Authorization: Bearer <JWT Token>`

### 認證

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/auth/register` | 註冊帳號（body: `name, email, password, role`） |
| POST | `/api/auth/login` | 登入，回傳 JWT token |

### 車輛管理

| 方法 | 路徑 | 權限 | 說明 |
|------|------|------|------|
| GET | `/api/vehicles` | 認證用戶 | 所有車輛列表 |
| GET | `/api/vehicles/available?start=&end=` | 認證用戶 | 查詢時段內可用車輛 |
| GET | `/api/vehicles/{id}` | 認證用戶 | 取得單一車輛 |
| POST | `/api/vehicles` | Admin | 新增車輛 |
| PUT | `/api/vehicles/{id}` | Admin | 更新車輛 |
| DELETE | `/api/vehicles/{id}` | Admin | 刪除車輛 |

### 借車申請

| 方法 | 路徑 | 權限 | 說明 |
|------|------|------|------|
| POST | `/api/borrowings` | 認證用戶 | 送出借車申請 |
| GET | `/api/borrowings/my` | 認證用戶 | 查看個人申請記錄 |
| GET | `/api/borrowings/pending` | 認證用戶 | 待審核申請列表 |
| GET | `/api/borrowings` | Admin | 所有申請記錄 |
| POST | `/api/borrowings/{id}/approve` | Admin | 核准申請 |
| POST | `/api/borrowings/{id}/reject` | Admin | 拒絕申請 |
| POST | `/api/borrowings/{id}/start` | Admin | 標記出車 |
| POST | `/api/borrowings/{id}/complete` | Admin | 標記還車 |

### 保養管理

| 方法 | 路徑 | 權限 | 說明 |
|------|------|------|------|
| POST | `/api/maintenance` | Admin | 新增保養記錄 |
| GET | `/api/maintenance/vehicle/{vehicleId}` | 認證用戶 | 查詢車輛保養記錄 |
| DELETE | `/api/maintenance/{id}` | Admin | 刪除保養記錄 |

---

## 測試

### 單元測試

使用 InMemory Repository 實作，不依賴資料庫，執行速度為毫秒級：

```bash
cd backend
mvn test -pl . -Dtest="*ServiceTest"
```

| 測試類別 | 測試案例 |
|----------|----------|
| `BorrowingServiceTest` | 送審、核准、拒絕、員工無法審核、衝突檢查、完整工作流程 |
| `VehicleServiceTest` | 建立、查詢、刪除、員工無法管理車輛 |
| `MaintenanceServiceTest` | 新增、查詢（依車輛）、到期提醒、刪除、員工無法操作 |
| `UserServiceTest` | 註冊、密碼雜湊、重複 email、角色權限驗證 |

### 整合測試（HTTP 層）

使用 `@WebMvcTest` + `@MockBean` 測試 REST 控制器的完整 HTTP 處理鏈：

```bash
cd backend
mvn test -Dtest="BorrowingControllerTest"
```

測試內容涵蓋：HTTP 狀態碼、JSON 序列化、JWT 認證守衛、权限拒絕（403）、未認證請求（401）。

---

## 資料庫 ERD

```
users ─────────────────────────────────────────────────────────
  id, name, email, password_hash, created_at
  │
  ├──→ user_roles ──→ roles ──→ role_permissions ──→ permissions
  │
  └──→ borrowing_requests ──→ vehicles
         id, user_id, vehicle_id                   id, plate, model, year, status
         period_start, period_end                  created_at
         state, review_note
         created_at, updated_at
                                  vehicles ──→ maintenance_records
                                                id, vehicle_id, date
                                                items, cost
                                                next_due_date, next_due_km
```

---

## 開發里程碑

| Phase | 內容 | Issues |
|-------|------|--------|
| Phase 1 | 初始化後端（Spring Boot）、前端（Vue 3）、DB 遷移結構 | #1–3 ✅ |
| Phase 2 | Domain Layer — Vehicle、User、BorrowingRequest（State Pattern）、Role（Factory） | #4–7 ✅ |
| Phase 3 | Repository Layer — 介面定義、InMemory 實作、JPA Adapter | #8–10 ✅ |
| Phase 4 | Service Layer — BorrowingService（Observer + Strategy）、VehicleService 等 | #11–13 ✅ |
| Phase 5 | Presentation Layer — REST Controllers、DTOs、JWT Security | #14–17 ✅ |
| Phase 6 | Frontend — 5 個 Vue 3 頁面（登入、借車、審核、車輛管理、保養管理） | #18–22 ✅ |
| Phase 7 | 測試 — 4 個 Service 單元測試、1 個 Controller 整合測試 | #23–24 ✅ |
