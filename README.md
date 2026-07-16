# Java-Admin 后台管理系统

## 项目简介

一个前后端分离的后台管理系统，包含**登录认证**和**用户管理**功能。

- **后端**：Spring Boot 3 + MyBatis-Plus + JWT + MySQL
- **前端**：Vue 3 + Vite + Element Plus + Pinia + Vue Router

## 目录结构

```
Java-Admin/
├── backend/                # 后端 Spring Boot 项目
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/admin/
│       │   ├── AdminApplication.java     # 启动类
│       │   ├── controller/               # 控制器层
│       │   ├── service/                  # 服务层
│       │   ├── mapper/                   # 数据访问层
│       │   ├── entity/                   # 实体类
│       │   ├── dto/                      # 数据传输对象
│       │   ├── config/                   # 配置类
│       │   ├── common/                   # 公共工具
│       │   ├── interceptor/              # 拦截器
│       │   ├── exception/                # 异常处理
│       │   └── annotation/               # 自定义注解
│       └── resources/
│           ├── application.yml           # 配置文件
│           └── db/migration/             # Flyway 迁移脚本
│               ├── V1__Create_user_table.sql
│               └── V2__Insert_default_users.sql
├── frontend/               # 前端 Vue 3 项目
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── api/                          # API 接口
│       ├── router/                       # 路由
│       ├── store/                        # 状态管理
│       ├── utils/                        # 工具函数
│       ├── views/                        # 页面视图
│       │   ├── login/                    # 登录页
│       │   ├── layout/                   # 布局
│       │   ├── dashboard/                # 首页
│       │   └── user/                     # 用户管理
│       └── styles/                       # 样式
```

## 环境要求

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Node.js | 18+ |

## 快速启动

### 1. 确保 MySQL 已启动

项目使用 Flyway 做数据库迁移，启动时会**自动创建数据库和表**，无需手动执行 SQL。

只需确保 MySQL 在 `127.0.0.1:3306` 运行，且 `root` 用户密码为 `root`（可在 `backend/src/main/resources/application.yml` 中修改）。

### 2. 启动后端

```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```

后端启动成功后，Flyway 会自动：
1. 创建 `java_admin` 数据库（如果不存在）
2. 执行迁移脚本（建表 + 插入初始数据）

访问：`http://localhost:8080/api`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动成功后，浏览器会自动打开：`http://localhost:5173`

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| test | 123456 | 普通用户 |

## API 接口

### 认证模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| GET | /api/auth/info | 获取当前用户信息 |
| POST | /api/auth/logout | 登出 |

### 用户管理模块

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users/page | 分页查询用户列表 |
| GET | /api/users/{id} | 根据ID查询用户 |
| POST | /api/users | 创建用户 |
| PUT | /api/users | 更新用户 |
| DELETE | /api/users/{id} | 删除用户 |
| PUT | /api/users/{id}/reset-password | 重置密码 |
| PUT | /api/users/{id}/status | 切换用户状态 |

## 功能特性

- ✅ JWT 认证（登录、Token 校验、401 自动跳转）
- ✅ 用户 CRUD（增删改查）
- ✅ 分页列表 + 多维筛选
- ✅ 角色管理（管理员/普通用户）
- ✅ 状态开关（启用/禁用）
- ✅ 密码重置
- ✅ 逻辑删除
- ✅ 表单校验
- ✅ 响应式布局

## Flyway 迁移说明

迁移脚本位于 `backend/src/main/resources/db/migration/`：

| 文件 | 说明 |
|------|------|
| `V1__Create_user_table.sql` | 创建 sys_user 用户表 |
| `V2__Insert_default_users.sql` | 插入默认管理员和测试用户 |

如需新增迁移，按版本号递增创建 `V3__xxx.sql`，Flyway 会在下次启动时自动执行。

> Flyway 通过 `flyway_schema_history` 表跟踪已执行的迁移，请勿修改已执行的脚本。

## 注意事项

1. **生产环境部署前**请修改 `application.yml` 中的 `jwt.secret`，使用更安全的密钥。
2. 密码加密当前使用 MD5，生产环境建议替换为 BCrypt。
3. 跨域已配置（允许所有来源），生产环境请限制为具体域名。
