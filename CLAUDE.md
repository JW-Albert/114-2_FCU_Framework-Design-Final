# CLAUDE.md

## Project Overview

Project: 114-2 逢甲大學軟體框架設計期末專題 — 公司車輛管理系統
Purpose: 企業內部車輛借用管理平台，提供借車申請、審核、出車、還車完整工作流程，並涵蓋站內通知收件夾、稽核日誌、帳號安全（密碼政策 + 登入鎖定）、車輛軟刪除等企業化功能；展示 OOD 原則（SOLID、LoD）與 10 個 GoF 設計模式（State、Observer、Strategy、Template Method、Factory Method、Adapter、Builder、Chain of Responsibility、Decorator、Command）的實際應用。
Owner: DamnDamnDamnM3（王建葦 D1210799、陳稚翔 D1249623）
Demo: https://demo.jw-albert.dev

## Tech Stack

**Backend**
- Language: Java 21
- Framework: Spring Boot 3.3.4
- Key dependencies: Spring Security + JJWT 0.12.6、Spring Data JPA、Flyway、Apache POI 5.3.0（Excel 匯出）、springdoc-openapi 2.6.0（Swagger UI）、Spring Boot Actuator（健康監控）、H2（dev profile）、PostgreSQL（prod profile）
- Build tool: Maven 3.9+（含 Maven Wrapper，無需全域安裝）

**Frontend**
- Language: TypeScript 5.5
- Framework: Vue 3.5 + Vite 5.4
- Key dependencies: Pinia 2.2、Vue Router 4.4、Axios 1.7、Chart.js 4.5

## Directory Structure

```
backend/
  src/main/java/com/vehicle/management/
    api/controller/     - REST Controllers（Borrowing、Vehicle、Notification、Audit、Violation 等）
    api/dto/            - Request / Response records
    domain/model/       - 領域模型（BorrowingRequest、Vehicle、User、Notification、AuditLog）
    domain/state/       - State Pattern（PendingState、ApprovedState、InUseState 等）
    domain/role/        - Role / Permission、RoleFactory（Factory Method）
    domain/observer/    - BorrowingEventPublisher、Email/InboxNotificationObserver（Observer）
    domain/strategy/    - ConflictCheckStrategy、StrictOverlapStrategy、BufferedOverlapDecorator（Decorator）
    domain/chain/       - BorrowingValidator 責任鏈（Chain of Responsibility）
    domain/command/     - BorrowingCommand、ApproveCommand 等 + BorrowingCommandBus（Command）
    service/            - BorrowingService、NotificationService、AuditService、LoginAttemptService、PasswordPolicy 等
    repository/         - 介面定義（IBorrowingRepository、INotificationRepository 等）+ InMemory 實作（測試用）
    infrastructure/
      persistence/      - JPA Entities + Repository Adapters（Adapter Pattern）
      security/         - JwtUtil、JwtAuthFilter、SecurityConfig
      config/           - OpenApiConfig（Swagger UI 設定）
  src/main/resources/
    application.yml               - 預設設定（PostgreSQL prod）
    application-dev.yml           - dev profile（H2 記憶體 DB，自動建測試帳號）
    db/migration/                 - Flyway SQL 遷移腳本（V1__initial_schema.sql … V8__soft_delete_vehicles.sql）

frontend/
  src/
    api/        - auth / vehicles / borrowings / maintenance / reports / notifications / audit / system
    stores/     - auth.ts（Pinia，存 JWT token 與使用者資訊）
    router/     - index.ts（路由守衛：adminOnly / approverOnly / requiresAuth）
    views/      - LoginView / EmployeeBorrowView / InboxView / AdminReviewView / AdminVehiclesView
                  AdminMaintenanceView / AdminUserManagementView / AdminViolationsView
                  AdminAuditView / CalendarView / AdminDashboardView

.github/workflows/
  deploy.yml    - GitHub Actions CI/CD（tag-based，建置 JAR + SSH 部署至 VPS）
```

## Build & Run Commands

**後端（開發模式，H2 DB，免安裝 PostgreSQL）**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# Windows: .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
# API 啟動於 http://localhost:8080
# 自動建立：admin@demo.com / Admin1234、emp@demo.com / Emp12345
```

**前端**
```bash
cd frontend
npm install
npm run dev
# 啟動於 http://localhost:5173，/api/* 自動 proxy 到 localhost:8080
```

**建置後端 JAR**
```bash
cd backend
./mvnw package -DskipTests
```

**執行測試**
```bash
cd backend
./mvnw test                          # 全部測試
./mvnw test -Dtest="*ServiceTest"    # 只跑 Service 單元測試
./mvnw test -Dtest="BorrowingControllerTest"  # Controller 整合測試
```

**前端型別檢查**
```bash
cd frontend
npx vue-tsc --build --noEmit
```

## Environment & Setup Notes

- **dev profile**（本機開發首選）：`application-dev.yml` 使用 H2 記憶體 DB，啟動時 `DataInitializer` 自動建立測試帳號與 3 輛測試車輛。不需要 PostgreSQL。
- **prod profile**（正式環境）：需設定環境變數 `DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`（至少 32 字元）。Flyway 自動執行 DB 遷移。
- **PM2 on VPS**：VPS 使用者無 sudo 權限，PM2 安裝於 `$HOME/.npm-global`。SSH session 不讀 `~/.profile`，因此 `java` 必須用絕對路徑啟動（`which java` 動態取得）。
- **Vite allowedHosts**：`vite.config.ts` 中已設定 `allowedHosts: ['demo.jw-albert.dev']`，讓 Cloudflare Tunnel 可以正常存取 Vite dev server。
- **`frontend/tsconfig.node.json`**：需包含 `"types": ["node"]` 與 `"skipLibCheck": true`，否則 CI `vue-tsc --build` 會失敗。
- **API 文件與健康檢查**：後端啟動後可瀏覽 `http://localhost:8080/swagger-ui.html`（互動式 API 文件，可用 JWT 授權測試）與 `http://localhost:8080/actuator/health`（健康狀態）。前端 Vite proxy 已涵蓋 `/api` 與 `/actuator`。

## Workflow Rules

Branch naming: `feature/<kebab-case>` / `fix/<kebab-case>`
Commit format: `feat: 說明（closes #issue）` / `fix: 說明` — 遵循 Conventional Commits
Before PR merge: 若有多個 feature branch 串接，需依序 rebase onto main 後再 merge（避免衝突累積）
Deploy trigger: 推送 `v*` tag 才會觸發 CI/CD（`push: tags: ['v*']`），push to main 不會觸發

**部署流程**
```bash
git checkout main && git pull origin main
git tag v1.x.x
git push origin v1.x.x
# GitHub Actions 自動建置 JAR → SCP 上傳 → SSH 重啟 PM2
```

## CI/CD Architecture

```
push tag v*  →  GitHub Actions
  1. Build backend JAR (Maven, Java 21, skip tests)
  2. SCP JAR → VPS ~/deploy/app.jar
  3. SSH:
     - git pull latest frontend source
     - npm install
     - pm2 delete + pm2 start backend (--spring.profiles.active=dev, H2 DB)
     - pm2 delete + pm2 start frontend (npm run dev, port 5173)
     - pm2 save

Cloudflare Tunnel: demo.jw-albert.dev → VPS:5173
Vite proxy: /api/* → localhost:8080
```

GitHub Secrets required: `HOST_ADDRESS`, `HOST_PORT`, `ACCOUNT_NAME`, `ACCOUNT_PASSWORD`

## Key Conflict-Prone Files

下列檔案在多個 feature branch 中都有修改，rebase 時容易衝突：

| 檔案 | 常見衝突原因 |
|------|-------------|
| `BorrowingController.java` | 新增 endpoint / import（`DateTimeFormat.ISO`）|
| `BorrowingService.java` | 新增 `findConflicts()` vs `listInRange()` 等方法 |
| `frontend/src/api/borrowings.ts` | 新增 API 方法 / interface 欄位 |
| `frontend/src/router/index.ts` | 各 feature 各自新增路由 |

Rebase 衝突解法：兩邊的新增**都要保留**，不要選邊站。

## MCP / External Tools

- **Context7**: 查詢 Spring Boot、Vue 3、Vite、Pinia 等框架的最新文件時使用
- **markitdown**: 將 PDF 或 Word 文件轉為 Markdown 閱讀時使用

## Reference Docs

- [README.md](README.md) — 系統簡介、快速啟動、API 文件、設計模式說明、CI/CD 部署指南
- [backend/src/main/resources/application-dev.yml](backend/src/main/resources/application-dev.yml) — dev 環境設定（H2、測試帳號）
- [.github/workflows/deploy.yml](.github/workflows/deploy.yml) — CI/CD 完整腳本
- [frontend/vite.config.ts](frontend/vite.config.ts) — Vite proxy 與 allowedHosts 設定
