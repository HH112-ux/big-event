# 大事件项目全链路开发文档

> 基于《大事件接口文档 V1.0》，拆解为用户、文章分类、文章管理、文件上传四个模块，覆盖 17 个接口，并提供从数据库设计到接口实现的完整开发路径。

| 项目 | 说明 |
|------|------|
| 版本 | V1.0 |
| 日期 | 2026-09-11 |
| 技术栈 | Spring Boot + MyBatis-Plus + MySQL + JWT |

---

## 1. 设计概述

### 1.1 目的与范围

本文档将《大事件接口文档 V1.0》转化为可直接指导后端开发的软件设计文档。覆盖 4 个业务模块、17 个接口、3 张数据表，包含系统架构、数据库设计、模块边界划分、接口契约定义，以及从零到上线的全链路开发步骤。

不包含：前端页面设计、运维部署方案、压力测试方案、日志监控告警体系。这些内容需独立文档承载。

### 1.2 需求溯源

上游产物为接口文档，定义了所有接口的请求路径、参数格式、响应结构和业务约束。以下设计决策均回溯到接口文档中的具体条目。

**表 1.1 需求溯源矩阵**

| 设计目标 | 上游接口文档条目 | 证据标签 |
|----------|----------------|----------|
| 用户注册与登录（JWT 认证） | 1.1 注册 / 1.2 登录 | [Data-backed] |
| 用户信息管理（详情/更新/头像/密码） | 1.3–1.6 用户相关接口 | [Data-backed] |
| 文章分类 CRUD（用户隔离） | 2.1–2.5 文章分类相关接口 | [Data-backed] |
| 文章管理 CRUD + 条件分页 | 3.1–3.5 文章管理相关接口 | [Data-backed] |
| 文件上传至阿里云 OSS | 4.1 文件上传接口 | [Data-backed] |
| JWT 令牌下发与请求头校验 | 1.2.4 备注说明（Authorization 头 + 401） | [Data-backed] |

### 1.3 设计目标与约束

| 维度 | 目标 | 约束 |
|------|------|------|
| 功能完整性 | 17 个接口全部实现，请求/响应结构与文档一致 | 响应统一 {code, message, data} 结构 [Expert judgment] |
| 认证安全 | JWT 令牌签发、解析、拦截器校验 | 未登录返回 HTTP 401 [Data-backed] |
| 数据隔离 | 分类和文章绑定用户 ID，只能操作自己的数据 | create_user 字段关联 [Expert judgment] |
| 分页查询 | 文章列表支持 pageNum/pageSize + 条件筛选 | PageHelper 插件 [Expert judgment] |
| 参数校验 | 用户名 5~16 位、邮箱格式、文章标题 1~10 字符 | Spring Validation [Data-backed] |

### 1.4 术语表

| 术语 | 含义 |
|------|------|
| JWT | JSON Web Token，用于用户身份认证的无状态令牌 |
| ThreadLocal | 线程本地变量，用于在拦截器与 Controller 间传递当前登录用户 ID |
| PageHelper | MyBatis 物理分页插件，通过拦截 SQL 自动追加 LIMIT |
| OSS | 阿里云对象存储服务，用于文件/图片托管 |
| Result | 统一响应封装类，包含 code/message/data 三字段 |

---

## 2. 系统架构

### 2.1 架构概述

系统采用经典的三层分层架构（Controller → Service → Mapper），所有请求通过统一入口进入，经 JWT 拦截器认证后路由到对应 Controller。选择单体分层而非微服务，因为 17 个接口的业务复杂度和吞吐量不构成拆分理由，单体能将开发周期和运维成本压到最低 [Expert judgment]。

### 2.2 架构图

```
┌─────────────────────────────────────────────────┐
│                   客户端 (Web 前端)                │
└──────────────────────┬──────────────────────────┘
                       │
              ┌────────▼────────┐
              │  JWT 拦截器      │
              │ (校验 Authorization 头) │
              └────┬────┬────┬────┬─┘
          ┌────────┘    │    │    └────────┐
          ▼             ▼    ▼             ▼
    ┌──────────┐ ┌────────┐ ┌──────────┐ ┌──────────┐
    │UserCtrl  │ │CategCtrl│ │ArticleCtrl│ │FileCtrl  │
    └────┬─────┘ └────┬───┘ └────┬─────┘ └────┬─────┘
         ▼            ▼          ▼             ▼
    ┌──────────┐ ┌────────┐ ┌──────────┐ ┌──────────┐
    │UserSvc   │ │CategSvc│ │ArticleSvc│ │FileSvc   │
    └────┬─────┘ └────┬───┘ └────┬─────┘ └────┬─────┘
         ▼            ▼          ▼             ▼
    ┌──────────────────────────┐    ┌──────────────┐
    │        MySQL              │    │  阿里云 OSS   │
    └──────────────────────────┘    └──────────────┘
```

请求从 Web 前端发出后，首先经过 JWT 拦截器。拦截器校验请求头中的 `Authorization` 字段，解析 JWT 令牌并将用户 ID 存入 ThreadLocal。通过认证的请求路由到各 Controller，Controller 调用对应 Service 完成业务逻辑，Service 通过 Mapper 访问 MySQL 数据库。文件上传接口走独立路径，直接调用阿里云 OSS SDK 完成远程存储。

### 2.3 组件职责与边界

| 组件 | 职责 | 不负责 |
|------|------|--------|
| JWT 拦截器 | 校验令牌、解析用户 ID、存入 ThreadLocal、afterCompletion 清理 | 业务逻辑、数据库访问 |
| UserController | 接收用户注册/登录/信息管理请求 | 文章分类、文章管理逻辑 |
| CategoryController | 文章分类的增删改查 | 文章正文管理 |
| ArticleController | 文章的增删改查 + 条件分页 | 分类管理 |
| FileController | 文件上传至 OSS | 业务数据存储 |

### 2.4 技术选型

**表 2.1 技术选型与决策依据**

| 技术 | 用途 | 选择理由 | 证据标签 |
|------|------|----------|----------|
| Spring Boot 3.x | 应用框架 | 自动配置 + 内嵌 Tomcat，降低环境配置成本 | [Expert judgment] |
| MyBatis-Plus | ORM 持久层 | 通用 CRUD 方法开箱即用，减少 XML 编写量 | [Expert judgment] |
| MySQL 8.0 | 关系型数据库 | 事务一致性 + 成熟生态，满足用户隔离和数据完整性需求 | [Expert judgment] |
| java-jwt (Auth0) | JWT 令牌签发与解析 | 接口文档要求登录后下发 JWT 令牌并后续请求携带 | [Data-backed] |
| PageHelper | 物理分页插件 | 文章列表接口要求 pageNum/pageSize 分页返回 total + items | [Data-backed] |
| Spring Validation | 参数校验 | 用户名 5~16 位、邮箱格式、文章标题长度等约束需声明式校验 | [Data-backed] |
| 阿里云 OSS SDK | 文件上传 | 接口文档中文件上传返回的是 OSS 存储 URL | [Data-backed] |
| Lombok | 样板代码消除 | 减少 Entity/DTO 的 getter/setter 样板 | [Expert judgment] |

---

## 3. 数据库设计

### 3.1 ER 图

```
┌──────────────┐         ┌──────────────────┐        ┌──────────────────┐
│    USER      │ 1─────N │    CATEGORY      │ 1────N │     ARTICLE      │
├──────────────┤         ├──────────────────┤        ├──────────────────┤
│ id (PK)      │◄────────│ create_user (FK) │        │ category_id (FK) │
│ username     │         │ id (PK)          │◄───────│ create_user (FK) │
│ password     │         │ category_name   │        │ id (PK)          │
│ nickname     │         │ category_alias  │        │ title             │
│ email        │         │ create_time     │        │ content           │
│ user_pic     │         │ update_time     │        │ cover_img          │
│ create_time  │         └──────────────────┘        │ state             │
│ update_time  │                                      │ version           │
└──────────────┘                                      │ is_deleted        │
                                                      │ create_time       │
                                                      │ update_time       │
                                                      └──────────────────┘
```

用户表（USER）是核心实体，分类和文章均通过 `create_user` 外键关联到用户，实现数据隔离。文章额外通过 `category_id` 外键关联到分类表，形成「用户 → 分类 → 文章」的层级关系。

### 3.2 表结构定义

#### 3.2.1 用户表 (user)

| 字段 | 类型 | 可空 | 约束 | 说明 |
|------|------|------|------|------|
| id | BIGINT | 否 | 主键, 自增 | 用户主键 |
| username | VARCHAR(16) | 否 | 唯一索引 | 用户名, 5~16 位 |
| password | VARCHAR(64) | 否 | - | 密码 (MD5 加密存储) |
| nickname | VARCHAR(10) | 是 | - | 昵称, 1~10 位 |
| email | VARCHAR(64) | 是 | - | 邮箱 |
| user_pic | VARCHAR(512) | 是 | - | 头像 URL |
| create_time | DATETIME | 否 | 默认当前时间 | 创建时间 |
| update_time | DATETIME | 否 | 更新时自动刷新 | 更新时间 |

#### 3.2.2 文章分类表 (category)

| 字段 | 类型 | 可空 | 约束 | 说明 |
|------|------|------|------|------|
| id | BIGINT | 否 | 主键, 自增 | 分类主键 |
| category_name | VARCHAR(32) | 否 | 同用户下唯一 | 分类名称 |
| category_alias | VARCHAR(32) | 否 | 同用户下唯一 | 分类别名 |
| create_user | BIGINT | 否 | 外键 → user.id | 创建者用户 ID |
| create_time | DATETIME | 否 | 默认当前时间 | 创建时间 |
| update_time | DATETIME | 否 | 更新时自动刷新 | 更新时间 |

#### 3.2.3 文章表 (article)

| 字段 | 类型 | 可空 | 约束 | 说明 |
|------|------|------|------|------|
| id | BIGINT | 否 | 主键, 自增 | 文章主键 |
| title | VARCHAR(10) | 否 | - | 文章标题, 1~10 字符 |
| content | TEXT | 否 | - | 文章正文 |
| cover_img | VARCHAR(512) | 否 | - | 封面图 URL |
| state | VARCHAR(8) | 否 | 枚举: 已发布/草稿 | 发布状态 |
| category_id | BIGINT | 否 | 外键 → category.id | 所属分类 ID |
| create_user | BIGINT | 否 | 外键 → user.id | 作者用户 ID |
| create_time | DATETIME | 否 | 默认当前时间 | 创建时间 |
| update_time | DATETIME | 否 | 更新时自动刷新 | 更新时间 |
| version | INT | 否 | 默认 1, 乐观锁 | 乐观锁版本号, MyBatis-Plus @Version 注解 |
| is_deleted | TINYINT | 否 | 默认 0, 逻辑删除 | 0-正常 1-已删除, MyBatis-Plus @TableLogic 注解 |

### 3.3 索引策略

| 表 | 索引名 | 字段 | 理由 |
|----|--------|------|------|
| user | uk_username | username (UNIQUE) | 注册时校验用户名唯一，登录时按用户名查询 [Data-backed] |
| category | idx_user_name | (create_user, category_name) | 同一用户下分类名称不可重复 [Expert judgment] |
| category | idx_user_alias | (create_user, category_alias) | 同一用户下分类别名不可重复 [Expert judgment] |
| article | idx_category_state | (category_id, state, create_user) | 文章列表按分类+状态+用户条件分页查询 [Data-backed] |
| article | idx_create_user | create_user | 查询用户自己的文章列表 [Expert judgment] |

### 3.4 删除策略

文章采用**逻辑删除（软删除）**策略，通过 `is_deleted` 字段标记。MyBatis-Plus 的 `@TableLogic` 注解自动在查询时追加 `WHERE is_deleted = 0` 条件，删除操作自动转为 UPDATE。逻辑删除支持数据恢复和审计追溯，避免误删导致不可逆数据丢失 [Expert judgment]。

分类删除为**物理删除**，但需先检查该分类下是否有未删除的文章。存在文章时拒绝删除并返回错误提示，前端引导用户先处理文章再删分类 [Data-backed]。

### 3.5 并发控制

文章更新接口采用**乐观锁**防止并发编辑覆盖。article 表增加 `version` 字段，MyBatis-Plus 的 `@Version` 注解在 UPDATE 时自动追加 `WHERE version = #{oldVersion}` 并将 version + 1。如果版本号不匹配（记录已被其他事务修改），UPDATE 影响行数为 0，Service 层抛出业务异常提示"文章已被他人修改，请刷新后重试" [Expert judgment]。

### 3.6 数据一致性规则

- 分类名称和别名在同一用户范围内唯一，通过联合唯一索引保证，不依赖应用层校验
- 文章的 `category_id` 必须引用当前用户创建的分类，Service 层校验归属关系
- 所有表的 `create_time` 和 `update_time` 由 MyBatis-Plus 自动填充，不需要手动设置
- 删除分类时需检查该分类下是否已有文章，存在则拒绝删除
- 文章逻辑删除后，关联查询自动过滤已删除记录，不需要手动排除

---

## 4. 模块设计

系统按业务域拆分为四个模块，每个模块拥有独立的 Controller、Service、Mapper 和 Entity。模块间通过 Service 层方法调用协作，不存在循环依赖。

### 4.1 用户模块

**模块边界定义**

| 维度 | 说明 |
|------|------|
| 职责 | 用户注册、登录认证、个人信息管理（昵称/邮箱/头像/密码） |
| 输入 | 注册/登录表单参数、JWT 令牌（后续请求）、更新请求体 |
| 输出 | JWT 令牌（登录）、用户详情对象、操作结果 |
| 依赖 | JWT 工具类、ThreadLocal 工具类、MD5 加密工具 |
| 边界 | 不负责文章分类和文章管理；不负责文件上传（头像 URL 由前端上传后传入） |

用户模块是整个系统的认证基座。注册时对密码做 MD5 加密存储，登录时校验用户名密码后签发 JWT 令牌。令牌中包含用户 ID 和用户名，后续请求通过拦截器解析令牌获取用户身份。密码更新接口需校验旧密码匹配且新密码与确认密码一致。

### 4.2 文章分类模块

**模块边界定义**

| 维度 | 说明 |
|------|------|
| 职责 | 文章分类的增删改查，按用户隔离 |
| 输入 | 分类名称、分类别名、分类 ID、当前用户 ID（来自 ThreadLocal） |
| 输出 | 分类列表、分类详情对象、操作结果 |
| 依赖 | ThreadLocal 工具类（获取当前用户 ID） |
| 边界 | 不负责文章正文管理；分类下有文章时不可删除 |

分类模块的所有查询和写入都绑定 `create_user` 字段。新增分类时校验同一用户下分类名称和别名不重复。分类列表接口返回当前用户创建的全部分类，按创建时间排序。

### 4.3 文章管理模块

**模块边界定义**

| 维度 | 说明 |
|------|------|
| 职责 | 文章的新增、列表分页查询、详情、更新、删除 |
| 输入 | 文章标题/正文/封面/状态/分类 ID、分页参数、条件筛选参数 |
| 输出 | 分页数据（total + items）、文章详情对象、操作结果 |
| 依赖 | 分类 Service（校验分类归属）、ThreadLocal 工具类、PageHelper |
| 边界 | 不负责分类管理；不负责文件上传（封面 URL 由前端上传后传入） |

文章列表接口支持按 `categoryId` 和 `state` 条件筛选，分页参数 `pageNum` 和 `pageSize` 为必填。返回结构为 `{total, items}`，与接口文档定义一致。新增文章时校验分类 ID 必须属于当前用户。

### 4.4 文件上传模块

**模块边界定义**

| 维度 | 说明 |
|------|------|
| 职责 | 单文件上传至阿里云 OSS，返回可访问 URL |
| 输入 | multipart/form-data 文件 |
| 输出 | OSS 存储 URL |
| 依赖 | 阿里云 OSS SDK、OSS 配置（endpoint/accessKey/bucket） |
| 边界 | 不负责业务数据存储；不负责图片压缩 |

文件上传接口接收 `multipart/form-data` 格式的文件，上传至阿里云 OSS Bucket 后返回文件的公网访问 URL。该 URL 用于用户头像更新和文章封面设置。

**表 4.1 文件上传安全约束**

| 约束项 | 规则 | 实现方式 | 证据标签 |
|--------|------|----------|----------|
| 文件类型白名单 | 仅允许 image/png, image/jpeg, image/gif, image/webp | 校验 Content-Type + 文件头魔数（前 8 字节） | [Expert judgment] |
| 文件大小上限 | 单文件 ≤ 5MB | application.yml 配置 spring.servlet.multipart.max-file-size | [Expert judgment] |
| 文件名校验 | 禁止双扩展名（如 .png.exe） | UUID 重命名，丢弃原始文件名 | [Expert judgment] |
| 内容校验 | 不能仅信任 Content-Type | 读取文件前 8 字节校验魔数（PNG: `89 50 4E 47`，JPEG: `FF D8 FF`） | [Expert judgment] |

### 4.5 模块依赖关系

```
用户模块 ──────────────┐
分类模块 ──────────────┼──→ 基础设施 (JWT / ThreadLocal / Result / 异常处理)
文章模块 ─→ 分类模块 ──┤
文件上传模块 ──────────┘
```

所有模块依赖基础设施层（JWT 工具、ThreadLocal、统一响应、全局异常处理）。文章管理模块依赖分类模块——新增文章时需校验分类 ID 属于当前用户。文件上传模块独立于其他业务模块，仅依赖 OSS 配置。依赖方向单向无环。

---

## 5. 接口设计

### 5.1 接口清单

**表 5.1 全量接口清单（17 个）**

| # | 模块 | 方法 | 路径 | 说明 | 认证 |
|---|------|------|------|------|------|
| 1 | 用户 | POST | /user/register | 注册 | 否 |
| 2 | 用户 | POST | /user/login | 登录 | 否 |
| 3 | 用户 | GET | /user/userInfo | 获取用户详情 | 是 |
| 4 | 用户 | PUT | /user/update | 更新基本信息 | 是 |
| 5 | 用户 | PATCH | /user/updateAvatar | 更新头像 | 是 |
| 6 | 用户 | PATCH | /user/updatePwd | 更新密码 | 是 |
| 7 | 分类 | POST | /category | 新增分类 | 是 |
| 8 | 分类 | GET | /category | 分类列表 | 是 |
| 9 | 分类 | GET | /category/detail | 分类详情 | 是 |
| 10 | 分类 | PUT | /category | 更新分类 | 是 |
| 11 | 分类 | DELETE | /category | 删除分类 | 是 |
| 12 | 文章 | POST | /article | 新增文章 | 是 |
| 13 | 文章 | GET | /article | 列表(分页) | 是 |
| 14 | 文章 | GET | /article/detail | 文章详情 | 是 |
| 15 | 文章 | PUT | /article | 更新文章 | 是 |
| 16 | 文章 | DELETE | /article | 删除文章 | 是 |
| 17 | 文件 | POST | /upload | 文件上传 | 是 |

### 5.2 统一响应结构

所有接口返回统一的 JSON 结构，由 `Result<T>` 封装：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

登录接口的 `data` 为 String 类型（JWT 令牌）；用户详情接口的 `data` 为用户对象；文章列表接口的 `data` 为 `{total, items}` 分页对象；其他接口的 `data` 为 null。

#### 5.2.1 错误码定义

**表 5.2 结构化错误码**

| code | HTTP 状态 | 含义 | 典型场景 |
|------|----------|------|----------|
| 0 | 200 | 成功 | 请求处理完成 |
| 1 | 200 | 通用业务失败 | 参数校验不通过、通用业务异常 |
| 2 | 401 | 未授权 | JWT 令牌缺失、过期或无效 |
| 3 | 200 | 资源不存在 | 文章/分类 ID 不存在或不属于当前用户 |
| 4 | 200 | 数据冲突 | 用户名/分类名/分类别名已存在 |
| 5 | 200 | 并发冲突 | 乐观锁版本号不匹配（文章已被他人修改） |
| 6 | 200 | 文件校验失败 | 文件类型不允许、大小超限、魔数不匹配 |

### 5.3 认证机制

> 除注册和登录外，所有接口需在请求头携带 `Authorization: <JWT令牌>`。拦截器校验失败时返回 HTTP 401 状态码（code=2）。JWT 令牌的 claims 中包含用户 ID 和用户名，拦截器解析后存入 ThreadLocal 供 Controller 使用。

#### 5.3.1 JWT 过期与续期策略

| 维度 | 策略 |
|------|------|
| 令牌有效期 | 12 小时（可在 application.yml 中配置 `jwt.expire`） |
| 过期处理 | 拦截器解析令牌时捕获 `TokenExpiredException`，返回 code=2 + HTTP 401；前端收到 401 后跳转登录页 |
| 续期策略 | 不引入 refresh token 机制。令牌过期后前端重新登录获取新令牌。后续如需续期可在登录响应中额外下发 refresh_token，通过 POST /user/refresh 接口换取新令牌 [Expert judgment] |
| 签名算法 | HS256，密钥存储在 application.yml 的 `jwt.secret` 中，生产环境通过环境变量注入 [Expert judgment] |

#### 5.3.2 ThreadLocal 生命周期管理

LoginInterceptor 在 `preHandle` 阶段将用户 ID 存入 ThreadLocal，**必须在 `afterCompletion` 阶段调用 `ThreadLocal.remove()` 清除**。Spring Boot 默认使用 Tomcat 线程池，线程复用时不清理会导致用户身份串号和内存泄漏 [Expert judgment]。

```java
@Override
public void afterCompletion(HttpServletRequest req,
        HttpServletResponse resp, Object handler, Exception ex) {
    ThreadLocalUtil.remove();  // 必须执行，防止线程复用导致串号
}
```

#### 5.3.3 API 版本化策略

本项目当前不引入 URL 版本前缀（如 `/v1/user/register`），通过向后兼容实现接口迭代——新增字段可选、废弃字段保留但不报错、不修改已有字段语义。如果未来出现不兼容变更（如 V2 重构密码更新参数结构），届时引入 `/v2` 前缀并在旧版本设置弃用期 [Expert judgment]。

### 5.4 用户模块接口契约

#### 5.4.1 POST /user/register（注册）

| 项目 | 说明 |
|------|------|
| 请求格式 | application/x-www-form-urlencoded |
| 参数 | `username` (string, 必填, 5~16 位非空字符); `password` (string, 必填, 5~16 位非空字符) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 错误码 | 4 (用户名已被占用); 1 (参数格式不合法) |
| 认证 | 否 |

#### 5.4.2 POST /user/login（登录）

| 项目 | 说明 |
|------|------|
| 请求格式 | application/x-www-form-urlencoded |
| 参数 | `username` (string, 必填); `password` (string, 必填) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": "<JWT令牌>"}` |
| 错误码 | 1 (用户名或密码错误) |
| 认证 | 否 |

登录成功后 JWT 令牌的 claims 结构为 `{"claims": {"id": 5, "username": "wangba"}, "exp": 1693715978}`，令牌有效期由后端配置决定。

#### 5.4.3 GET /user/userInfo（获取用户详情）

| 项目 | 说明 |
|------|------|
| 请求参数 | 无（从 ThreadLocal 获取当前用户 ID） |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": {"id": 5, "username": "wangba", "nickname": "", "email": "", "userPic": "", "createTime": "2023-09-02 22:21:31", "updateTime": "2023-09-02 22:21:31"}}` |
| 认证 | 是 |

#### 5.4.4 PUT /user/update（更新基本信息）

| 项目 | 说明 |
|------|------|
| 请求格式 | application/json |
| 参数 | `id` (number, 必填); `username` (string, 选填, 5~16 位); `nickname` (string, 必填, 1~10 位); `email` (string, 必填, 邮箱格式) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 认证 | 是 |

#### 5.4.5 PATCH /user/updateAvatar（更新头像）

| 项目 | 说明 |
|------|------|
| 请求格式 | queryString |
| 参数 | `avatarUrl` (string, 必填, URL 地址) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 认证 | 是 |

#### 5.4.6 PATCH /user/updatePwd（更新密码）

| 项目 | 说明 |
|------|------|
| 请求格式 | application/json |
| 参数 | `old_pwd` (string, 必填); `new_pwd` (string, 必填); `re_pwd` (string, 必填, 需与 new_pwd 一致) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 错误码 | 1 (原密码错误); 1 (两次新密码不一致) |
| 认证 | 是 |

### 5.5 文章分类模块接口契约

#### 5.5.1 POST /category（新增分类）

| 项目 | 说明 |
|------|------|
| 请求格式 | application/json |
| 参数 | `categoryName` (string, 必填); `categoryAlias` (string, 必填) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 错误码 | 4 (分类名称/别名已存在) |
| 认证 | 是 |

#### 5.5.2 GET /category（分类列表）

| 项目 | 说明 |
|------|------|
| 请求参数 | 无 |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": [{"id": 3, "categoryName": "美食", "categoryAlias": "my", "createTime": "...", "updateTime": "..."}]}` |
| 认证 | 是 |

#### 5.5.3 GET /category/detail（分类详情）

| 项目 | 说明 |
|------|------|
| 请求格式 | queryString |
| 参数 | `id` (number, 必填) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": {"id": 6, "categoryName": "风土人情", "categoryAlias": "ftrq", ...}}` |
| 认证 | 是 |

#### 5.5.4 PUT /category（更新分类）

| 项目 | 说明 |
|------|------|
| 请求格式 | application/json |
| 参数 | `id` (number, 必填); `categoryName` (string, 必填); `categoryAlias` (string, 必填) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 认证 | 是 |

#### 5.5.5 DELETE /category（删除分类）

| 项目 | 说明 |
|------|------|
| 请求格式 | queryString |
| 参数 | `id` (number, 必填) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 认证 | 是 |

### 5.6 文章管理模块接口契约

#### 5.6.1 POST /article（新增文章）

| 项目 | 说明 |
|------|------|
| 请求格式 | application/json |
| 参数 | `title` (string, 必填, 1~10 字符); `content` (string, 必填); `coverImg` (string, 必填, URL); `state` (string, 必填, 已发布\|草稿); `categoryId` (number, 必填) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 认证 | 是 |

#### 5.6.2 GET /article（文章列表-条件分页）

| 项目 | 说明 |
|------|------|
| 请求格式 | queryString |
| 参数 | `pageNum` (number, 必填); `pageSize` (number, 必填); `categoryId` (number, 选填); `state` (string, 选填, 已发布\|草稿) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": {"total": 1, "items": [{"id": 5, "title": "...", "content": "...", "coverImg": "...", "state": "草稿", "categoryId": 2, "createTime": "...", "updateTime": "..."}]}}` |
| 认证 | 是 |

分页通过 PageHelper 实现。Service 层调用 `PageHelper.startPage(pageNum, pageSize)` 后执行条件查询，将 PageInfo 转换为 `{total, items}` 结构返回。条件筛选仅限当前用户自己的文章。

#### 5.6.3 GET /article/detail（文章详情）

| 项目 | 说明 |
|------|------|
| 请求格式 | queryString |
| 参数 | `id` (number, 必填) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": {"id": 4, "title": "...", "content": "...", ...}}` |
| 认证 | 是 |

#### 5.6.4 PUT /article（更新文章）

| 项目 | 说明 |
|------|------|
| 请求格式 | application/json |
| 参数 | `id` (number, 必填); `title` (string, 必填); `content` (string, 必填); `coverImg` (string, 必填); `state` (string, 必填); `categoryId` (number, 必填) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 错误码 | 5 (乐观锁版本不匹配) |
| 认证 | 是 |

#### 5.6.5 DELETE /article（删除文章）

| 项目 | 说明 |
|------|------|
| 请求格式 | queryString |
| 参数 | `id` (number, 必填) |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": null}` |
| 认证 | 是 |

文章删除为逻辑删除，MyBatis-Plus 自动将 `is_deleted` 设为 1。

### 5.7 文件上传接口契约

#### 5.7.1 POST /upload（文件上传）

| 项目 | 说明 |
|------|------|
| 请求格式 | multipart/form-data |
| 参数 | `file` (file, 必填, 表单文件字段) |
| 安全约束 | 仅允许 image/png, image/jpeg, image/gif, image/webp；单文件 ≤ 5MB；校验文件头魔数；UUID 重命名丢弃原始文件名 |
| 成功响应 | `{"code": 0, "message": "操作成功", "data": "https://big-event-gwd.oss-cn-beijing.aliyuncs.com/xxx.png"}` |
| 错误码 | 6 (文件类型不允许 / 大小超限 / 魔数不匹配) |
| 认证 | 是 |

文件上传至阿里云 OSS Bucket `big-event-gwd`（区域: oss-cn-beijing）。文件名使用 UUID 重命名避免覆盖，返回 OSS 公网访问 URL。Service 层在上传前依次校验文件类型白名单、文件大小、文件头魔数，任一校验不通过立即返回 code=6，不上传至 OSS。

### 5.8 关键业务时序

#### 5.8.1 登录流程

```
Client          JWT Interceptor    UserController    UserService    JwtUtil    MySQL
  │                   │                  │               │            │         │
  │── POST /user/login ──>│              │               │            │         │
  │                   │── 放行(无需认证) ──>│               │            │         │
  │                   │              │── login(username, password) ──>│         │
  │                   │              │               │── SELECT WHERE username ─>│
  │                   │              │               │<── User 实体 ───────────│
  │                   │              │               │── MD5 校验密码 ──│         │
  │                   │              │               │── generateToken ─>│       │
  │                   │              │               │<── JWT 令牌 (12h) ─│       │
  │                   │              │<── 令牌字符串 ──│            │         │
  │<── Result(code=0, data=令牌) ───│              │               │            │         │
```

登录请求无需经过 JWT 校验，拦截器直接放行。UserService 按用户名查询用户记录，对密码做 MD5 比对后调用 JwtUtil 签发令牌。令牌中携带用户 ID 和用户名，有效期 12 小时。

#### 5.8.2 新增文章流程

```
Client     JWT Interceptor   ArticleController   ArticleService   CategoryService   ArticleMapper   MySQL
  │              │                  │                  │                 │                 │           │
  │── POST /article ─>│              │                  │                 │                 │           │
  │              │── 解析Token, 存ThreadLocal ─>│        │                 │                 │           │
  │              │              │── addArticle(dto, userId) ─>│            │                 │           │
  │              │              │                  │── findById(categoryId, userId) ─>│     │           │
  │              │              │                  │                 │── SELECT WHERE id AND create_user ─>│
  │              │              │                  │                 │<── Category 或 null ────────────│
  │              │              │                  │<── 校验结果 ────│                 │           │
  │              │              │                  │                                              │
  │              │              │                  │── [校验通过] 填充 create_user/version/is_deleted ─│
  │              │              │                  │── insert(article) ──────────────────────────>│
  │              │              │                  │                 │                 │── INSERT ─>│
  │              │              │                  │                 │                 │<── 行数 1 ──│
  │              │              │                  │<── 成功 ────────────────────────│           │
  │              │              │<── 成功 ────────│                 │                 │           │
  │<── Result(code=0) ──────────│              │                  │                 │           │
  │              │── afterCompletion: ThreadLocal.remove() ─────────────────────────────────────────│
```

新增文章涉及跨模块协作：ArticleService 调用 CategoryService 校验分类归属，通过后填充 `create_user`、`version`、`is_deleted` 字段并写入数据库。拦截器在请求结束后执行 `ThreadLocal.remove()` 清理线程上下文。

### 5.9 DTO 与 Entity 设计

每个接口使用独立的 DTO 接收请求参数，与数据库 Entity 分离，避免暴露内部字段（如 `create_user`、`is_deleted`）并支持针对性的校验注解。

#### 5.9.1 DTO 清单

**表 5.3 DTO 定义与校验注解**

| DTO | 对应接口 | 字段 | 校验注解 | 来源 |
|-----|---------|------|----------|------|
| RegisterDTO | POST /user/register | username, password | @Size(min=5,max=16), @NotBlank | [Data-backed] |
| LoginDTO | POST /user/login | username, password | @NotBlank | [Data-backed] |
| UserUpdateDTO | PUT /user/update | id, username, nickname, email | @NotNull(id), @Size(min=1,max=10,nickname), @Email(email) | [Data-backed] |
| UpdatePwdDTO | PATCH /user/updatePwd | old_pwd, new_pwd, re_pwd | @NotBlank, 自定义 @FieldMatch(new_pwd, re_pwd) | [Data-backed] |
| CategoryDTO | POST/PUT /category | id(更新时), categoryName, categoryAlias | @NotBlank | [Data-backed] |
| ArticleDTO | POST/PUT /article | id(更新时), title, content, coverImg, state, categoryId | @Size(min=1,max=10,title), @NotBlank(content,coverImg,state), @NotNull(categoryId) | [Data-backed] |
| ArticleQueryDTO | GET /article | pageNum, pageSize, categoryId(选填), state(选填) | @Min(1) on pageNum/pageSize | [Data-backed] |

#### 5.9.2 Entity 层结构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Entity 层                                      │
├──────────────┬──────────────────┬───────────────────────────────────────┤
│ User         │ Category         │ Article                               │
├──────────────┼──────────────────┼───────────────────────────────────────┤
│ id           │ id               │ id                                    │
│ username     │ categoryName     │ title                                 │
│ password     │ categoryAlias    │ content                               │
│ nickname     │ createUser       │ coverImg                              │
│ email        │ createTime        │ state                                 │
│ userPic      │ updateTime        │ categoryId                            │
│ createTime   │                  │ createUser                            │
│ updateTime   │                  │ version     ← @Version (乐观锁)       │
│              │                  │ isDeleted   ← @TableLogic (逻辑删除)  │
│              │                  │ createTime                            │
│              │                  │ updateTime                            │
├──────────────┼──────────────────┼───────────────────────────────────────┤
│ UserMapper   │ CategoryMapper   │ ArticleMapper                        │
├──────────────┼──────────────────┼───────────────────────────────────────┤
│ findByUsername│ findByCreateUser│ findByCondition                       │
│ findById      │ findByIdAndCreate│ findByIdAndCreateUser                 │
│               │ User            │                                       │
├──────────────┼──────────────────┼───────────────────────────────────────┤
│ UserService  │ CategoryService  │ ArticleService                       │
├──────────────┼──────────────────┼───────────────────────────────────────┤
│ register()   │ add()            │ add()    → 调用 CategoryService      │
│ login()      │ list()           │ list()   → PageHelper 分页           │
│ getUserInfo()│ findById()       │ findById()                           │
│ update()     │ update()         │ update() → 乐观锁校验                │
│ updateAvatar()│ delete()        │ delete() → 逻辑删除                  │
│ updatePwd()  │                  │                                       │
└──────────────┴──────────────────┴───────────────────────────────────────┘
```

Entity 使用 Lombok `@Data` 注解生成 getter/setter，MyBatis-Plus 注解 `@TableName`、`@TableId`、`@TableField` 映射数据库字段。Article 实体额外标注 `@Version`（乐观锁）和 `@TableLogic`（逻辑删除）。Service 接口继承 MyBatis-Plus 的 `IService<T>`，实现类继承 `ServiceImpl<M, T>` 获得通用 CRUD 方法。

### 5.10 事务边界

Spring Boot 声明式事务通过 `@Transactional` 注解实现。以下列出需要事务保护的 Service 方法及其传播行为：

**表 5.4 事务边界定义**

| Service 方法 | 事务 | 传播行为 | 理由 |
|-------------|------|----------|------|
| UserService.register() | 是 | REQUIRED | 用户名唯一校验 + 插入需在同一事务内，防止并发注册同名用户 [Expert judgment] |
| UserService.updatePwd() | 是 | REQUIRED | 旧密码校验 + 新密码写入需原子完成，防止校验后写入失败导致状态不一致 [Expert judgment] |
| ArticleService.add() | 是 | REQUIRED | 分类归属校验 + 文章插入需原子完成，分类被并发删除时不插入孤儿记录 [Expert judgment] |
| ArticleService.update() | 是 | REQUIRED | 分类归属校验 + 乐观锁更新需原子完成 [Expert judgment] |
| CategoryService.delete() | 是 | REQUIRED | 检查分类下是否有文章 + 删除分类需原子完成，防止并发删除时遗漏检查 [Expert judgment] |
| 其他查询方法 | 否 | - | 只读操作无需事务 [Expert judgment] |

事务注解加在 Service 实现类的方法上，不是 Controller 上。MyBatis-Plus 的 `ServiceImpl` 已对 `save`、`update`、`removeById` 等通用方法加了 `@Transactional`，自定义方法需手动添加。

### 5.11 非功能设计

#### 5.11.1 性能

| 指标 | 目标 | 实现方式 | 证据标签 |
|------|------|----------|----------|
| 文章分页查询响应时间 | ≤ 500ms（10 万条数据） | 联合索引 (category_id, state, create_user) 覆盖查询条件；PageHelper 的 COUNT 查询走索引 | [Expert judgment] |
| 登录接口响应时间 | ≤ 200ms | 用户名唯一索引保证查询走索引 | [Expert judgment] |
| 文件上传响应时间 | ≤ 3s（5MB 文件） | 依赖 OSS 网络带宽，前端展示 loading 状态 | [Expert judgment] |
| 数据库连接池 | HikariCP，max-pool-size=10 | application.yml 配置 | [Expert judgment] |

#### 5.11.2 安全

| 维度 | 策略 | 证据标签 |
|------|------|----------|
| 密码存储 | MD5 加密（按接口文档约定） | [Data-backed] |
| JWT 密钥 | HS256 算法，密钥通过环境变量注入，不硬编码 | [Expert judgment] |
| SQL 注入防护 | MyBatis-Plus 参数化查询，所有用户输入通过 #{} 绑定 | [Expert judgment] |
| XSS 防护 | 文章正文存储原文，前端渲染时转义；Service 层不额外过滤 | [Expert judgment] |
| 文件上传安全 | 类型白名单 + 大小限制 + 魔数校验 + UUID 重命名 | [Expert judgment] |

#### 5.11.3 可观测性

| 维度 | 策略 | 证据标签 |
|------|------|----------|
| 日志框架 | SLF4J + Logback（Spring Boot 自带） | [Expert judgment] |
| 日志级别 | 开发环境 DEBUG，生产环境 INFO | [Expert judgment] |
| 审计日志 | 密码修改、文章删除操作记录 WARN 级别日志（用户 ID + 操作时间 + 目标 ID） | [Expert judgment] |
| 异常日志 | GlobalExceptionHandler 中对非业务异常打印 ERROR 级别堆栈 | [Expert judgment] |
| 请求日志 | 拦截器 preHandle 中记录请求路径和用户 ID（DEBUG 级别） | [Expert judgment] |

#### 5.11.4 可用性

| 维度 | 策略 | 证据标签 |
|------|------|----------|
| OSS 上传失败 | 捕获 OSSException 返回 code=1 + 友好提示，前端允许重试 | [Expert judgment] |
| 数据库连接超时 | HikariCP connection-timeout=30s，超时后返回 code=1 + "系统繁忙"提示 | [Expert judgment] |
| 分页越界 | pageNum 超过总页数时返回空 items（不报错），total 仍返回真实总数 | [Expert judgment] |

---

## 6. 全链路开发步骤

开发按依赖顺序从底层到上层推进，共分为五个阶段。每个阶段完成后可独立验证，降低集成风险。

### 6.1 开发流程

```
Phase 1: 项目初始化 + 数据库
    ↓
Phase 2: 基础设施层 (Result / JWT / 拦截器 / 异常处理)
    ↓
Phase 3: 用户模块 (6 个接口)
    ↓
Phase 4: 分类 + 文件上传 (6 个接口)
    ↓
Phase 5: 文章管理 (5 个接口)
```

### 6.2 Phase 1: 项目初始化与数据库设计

**产出物：**
- Spring Boot 项目骨架（Maven 依赖 + 基本配置）
- 三张数据表 DDL + 测试数据
- application.yml 配置（数据源 + MyBatis-Plus + OSS）

使用 Spring Initializr 创建项目，引入以下 Maven 依赖：

```xml
<dependencies>
  <!-- Spring Boot -->
  <dependency>spring-boot-starter-web</dependency>
  <!-- MyBatis-Plus -->
  <dependency>mybatis-plus-boot-starter</dependency>
  <!-- MySQL 驱动 -->
  <dependency>mysql-connector-j</dependency>
  <!-- 分页插件 -->
  <dependency>pagehelper-spring-boot-starter</dependency>
  <!-- JWT -->
  <dependency>java-jwt (com.auth0)</dependency>
  <!-- 参数校验 -->
  <dependency>spring-boot-starter-validation</dependency>
  <!-- 阿里云 OSS -->
  <dependency>aliyun-oss-sdk</dependency>
  <!-- Lombok -->
  <dependency>lombok</dependency>
</dependencies>
```

创建数据库 `big_event`，按 3.2 节表结构定义执行 DDL。初始化测试用户数据用于后续接口调试。

### 6.3 Phase 2: 基础设施层

**产出物：**
- Result<T> 统一响应封装类
- JwtUtil 工具类（签发/解析令牌）
- ThreadLocal 工具类（存取当前用户 ID）
- LoginInterceptor 拦截器（校验 Authorization 头 + afterCompletion 清理 ThreadLocal）
- WebConfig 配置类（注册拦截器 + 放行路径 + CORS 跨域配置）
- GlobalExceptionHandler 全局异常处理器

Result 类包含 `code`（0 成功 / 非0 失败）、`message`、`data` 三个字段，提供 `success()` 和 `error(msg)` 静态工厂方法。JwtUtil 使用 Auth0 java-jwt 库，签发时将用户 ID 和用户名存入 claims，设置 12 小时过期时间，使用 HS256 算法签名。

LoginInterceptor 拦截所有请求，放行 `/user/register` 和 `/user/login`。从请求头获取 `Authorization` 字段，调用 JwtUtil 解析，成功后将用户 ID 存入 ThreadLocal，失败则返回 HTTP 401（code=2）。**afterCompletion 阶段必须调用 `ThreadLocalUtil.remove()` 清除数据**，防止 Tomcat 线程池复用导致用户身份串号。

WebConfig 配置类注册拦截器并配置 CORS 跨域：允许前端开发域名（如 `http://localhost:5173`）访问，允许携带 Authorization 头，允许所有方法和路径。生产环境通过配置文件注入允许的域名列表 [Expert judgment]。

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
        .allowedHeaders("Authorization", "Content-Type")
        .allowCredentials(true);
}
```

GlobalExceptionHandler 捕获 `MethodArgumentNotValidException`（参数校验失败）、`TokenExpiredException`（令牌过期）和自定义业务异常，统一包装为 Result 返回。

### 6.4 Phase 3: 用户模块

**产出物：**
- User 实体类 + UserMapper + UserService + UserController
- 注册 DTO / 更新 DTO / 密码 DTO（含 Validation 注解）
- 6 个用户接口实现

按以下顺序实现 6 个接口，每个接口完成后立即用 Postman 或类似工具验证：

| 步骤 | 接口 | 关键实现点 |
|------|------|-----------|
| 3.1 | POST /user/register | 校验用户名唯一；密码 MD5 加密；写入 create_user 为自己的 ID |
| 3.2 | POST /user/login | 按用户名查询；校验密码 MD5 匹配；签发 JWT 令牌 |
| 3.3 | GET /user/userInfo | 从 ThreadLocal 取用户 ID；查询并返回用户信息 |
| 3.4 | PUT /user/update | 校验 nickname 长度和 email 格式；更新 createTime 不变，updateTime 自动刷新 |
| 3.5 | PATCH /user/updateAvatar | 从 queryString 获取 avatarUrl；更新 user_pic 字段 |
| 3.6 | PATCH /user/updatePwd | 校验 old_pwd 与数据库匹配；校验 new_pwd == re_pwd；MD5 加密后更新 |

Phase 3 完成后，注册一个测试用户并登录获取 JWT 令牌，验证后续接口的认证链路畅通。

### 6.5 Phase 4: 文章分类模块与文件上传模块

**产出物：**
- Category 实体类 + CategoryMapper + CategoryService + CategoryController（5 个接口）
- FileController + FileService（1 个接口）
- 阿里云 OSS 配置类

分类模块和文件上传模块可并行开发。分类模块的关键点在于所有 SQL 都带 `create_user` 条件，实现数据隔离。新增和更新时校验同一用户下分类名称和别名不重复。

文件上传模块配置 OSS 客户端 Bean，注入 endpoint、accessKeyId、accessKeySecret、bucketName。Controller 接收 MultipartFile，**先校验文件类型白名单、大小、文件头魔数，通过后**生成 UUID 文件名上传至 OSS，返回公网 URL。

| 步骤 | 接口 | 关键实现点 |
|------|------|-----------|
| 4.1 | POST /category | 从 ThreadLocal 获取 create_user；校验名称/别名唯一；写入 |
| 4.2 | GET /category | 按 create_user 查询全部分类，按 create_time 排序 |
| 4.3 | GET /category/detail | 按 id + create_user 查询，确保不能查别人的分类 |
| 4.4 | PUT /category | 校验 id + create_user 归属；校验更新后的名称/别名不冲突 |
| 4.5 | DELETE /category | 校验 id + create_user 归属；检查该分类下是否有文章 |
| 4.6 | POST /upload | 校验类型白名单+大小+魔数；UUID 重命名；上传 OSS；返回 URL |

### 6.6 Phase 5: 文章管理模块

**产出物：**
- Article 实体类 + ArticleMapper + ArticleService + ArticleController（5 个接口）
- 文章 DTO（含 Validation 注解）
- PageHelper 分页集成

文章管理是依赖最多的模块，需调用分类 Service 校验分类归属。分页查询接口是这个模块的重点。

| 步骤 | 接口 | 关键实现点 |
|------|------|-----------|
| 5.1 | POST /article | 校验标题长度(1~10)；校验 categoryId 属于当前用户；写入 create_user, version=1, is_deleted=0 |
| 5.2 | GET /article | PageHelper.startPage(pageNum, pageSize)；按 categoryId/state/create_user 条件查询；返回 {total, items} |
| 5.3 | GET /article/detail | 按 id + create_user 查询，确保数据隔离 |
| 5.4 | PUT /article | 校验 id + create_user 归属；校验 categoryId 归属；乐观锁更新（version 校验） |
| 5.5 | DELETE /article | 校验 id + create_user 归属；逻辑删除（is_deleted = 1） |

Phase 5 完成后，用测试用户登录 → 创建分类 → 上传封面图 → 发布文章 → 分页查询文章 → 更新文章 → 删除文章，走通全链路。

### 6.7 联调验证清单

全部模块开发完成后，按以下顺序走通业务全链路：

1. 注册新用户 → 登录获取 JWT 令牌
2. 携带令牌获取用户详情 → 更新昵称/邮箱 → 上传头像 → 更新密码
3. 新增文章分类 → 查看分类列表 → 更新分类 → 获取分类详情
4. 上传文件获取 OSS URL → 新增文章（关联分类 + 封面 URL）→ 设为草稿
5. 分页查询文章列表（按分类和状态筛选）→ 获取文章详情 → 更新文章状态为已发布
6. 删除文章 → 删除分类（需先确保无文章关联）

> 每个阶段完成后做一轮接口验证，不要等全部写完再联调。Phase 2 的基础设施层是全局复用的，投入时间把 Result、JWT、拦截器、异常处理写扎实，后续模块只关注业务逻辑即可。

---

> 本文档基于《大事件接口文档 V1.0》生成，用于指导后端开发实现。技术选型和架构决策标注了证据来源：[Data-backed] 来自接口文档约束，[Expert judgment] 来自工程经验判断。
