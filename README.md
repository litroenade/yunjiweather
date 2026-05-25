# 云迹天气 YunJi Weather

云迹天气是一个原生 Android 天气生活服务 App，使用 Java、XML Views、Jetpack 组件、Room、Retrofit 和 WorkManager 实现。它不是单纯展示接口返回值的 Demo，而是围绕“用户账户、城市管理、天气查询、生活指数、天气预警、缓存降级、通知提醒、偏好设置”做成一个可运行、可演示、可答辩的完整毕业设计项目。

这份 README 按毕业设计答辩来写：先说明项目为什么做，再说明系统做了什么、怎么设计、关键技术怎么落地、遇到问题怎么处理、如何演示、老师追问时怎么回答。

## 1. 答辩时可以先这样讲

### 30 秒版本

云迹天气是我用原生 Android 开发的一款天气生活服务 App。系统支持本地账户注册登录、用户数据隔离、默认城市和关注城市管理、首页天气、未来天气、空气质量、日历天气、生活指数、官方天气预警、后台通知提醒和本地缓存回退。项目默认使用 Open-Meteo 作为免 Key 天气源，QWeather 作为可选增强源。整体架构采用 Single Activity + Fragment + MVVM + Repository + Room DAO，UI 层、业务状态、网络请求和本地持久化职责分离。

### 2 分钟版本

本项目解决的是移动端天气应用中常见的几个问题：用户希望能快速查看默认城市天气，也希望管理多个城市；天气数据依赖网络，所以需要缓存和失败降级；预警和每日提醒属于后台能力，需要通知权限和后台任务；课程项目没有真实后端，所以我用本地 Room 实现账户、城市、缓存和预警数据，并通过 `ownerUserId` 做用户隔离。

技术上，应用入口是 `SplashActivity`，登录后进入 `MainActivity`。主页面用 Jetpack Navigation 管理五个 Fragment：天气、日历、城市、预警、我的。页面状态通过 ViewModel 暴露 LiveData，业务数据由 Repository 编排。天气数据优先请求远程接口，请求成功写入 Room 缓存；失败时读取缓存并展示明确的缓存状态。后台预警和每日提醒使用 WorkManager，每个用户都有独立的任务名，避免多账户之间互相影响。

### 5 分钟版本

如果答辩老师要求展开，可以按这个顺序讲：

1. 项目背景：天气查询是典型移动端场景，包含网络、缓存、权限、通知、设置、列表和多页面导航，适合作为毕业设计展示完整工程能力。
2. 需求设计：系统包含登录注册、天气首页、城市管理、日历天气、天气预警、我的设置六类核心能力。
3. 架构设计：采用 MVVM + Repository + DAO，避免把网络请求、数据库操作和 UI 渲染写在一个 Activity 里。
4. 数据源设计：Open-Meteo 保证项目开箱可运行；QWeather 提供官方预警和增强数据；没有配置 QWeather 时不伪造官方预警。
5. 数据隔离：账户、城市、缓存、预警和设置都带用户维度，账户 A 的数据不会影响账户 B。
6. 缓存降级：天气接口失败时读取 Room 缓存；缓存过期会明确提示“仅供参考”。
7. 后台能力：WorkManager 做 6 小时预警检查和 24 小时每日提醒，通知前检查 Android 13 通知权限。
8. 测试验证：项目包含 36 个 JVM 单元测试类和 1 个 Android Instrumented Test，覆盖密码、数据隔离、缓存回退、接口配置、天气映射和工具函数。

## 2. 项目目标与边界

### 项目目标

| 目标 | 说明 |
| --- | --- |
| 可运行 | 不配置 QWeather 也能通过 Open-Meteo 展示基础天气。 |
| 可演示 | 能从启动、注册、登录、查天气、加城市、看预警、改设置完整演示。 |
| 可解释 | 架构分层明确，每一层为什么存在都能在答辩中说明。 |
| 可降级 | 网络失败、接口未配置、权限未授予时有明确处理，不静默崩溃。 |
| 可测试 | 核心业务逻辑有 JVM 单元测试，避免只靠手动点页面验证。 |

### 当前边界

| 边界 | 说明 |
| --- | --- |
| 账户系统 | 本地 Room 账户，不接真实后端，不做多设备同步。 |
| 数据源 | Open-Meteo 默认可用，QWeather 需要在 `local.properties` 手动配置。 |
| 数据迁移 | 当前 Room 使用 `fallbackToDestructiveMigration()`，适合课程开发期快速迭代；生产环境需要补 Migration。 |
| Release | 当前未配置正式 release 签名，答辩和本地测试建议使用 debug APK。 |
| 隐私合规 | 已避免提交 API Key，但正式上线仍需补隐私政策、权限说明和合规文档。 |

## 3. 核心功能总览

| 模块 | 用户能看到什么 | 背后的实现重点 |
| --- | --- | --- |
| 启动页 | 应用名称、启动背景、自动判断登录状态 | `SplashActivity` + 本地会话判断 |
| 登录注册 | 本地账户注册、登录、显示名称 | `AuthRepository` + `UserDao` + PBKDF2 密码哈希 |
| 天气首页 | 当前温度、天气状态、体感、湿度、风、气压、能见度、AQI、逐小时、未来三天、生活建议 | `HomeFragment` + `HomeViewModel` + `WeatherRepository` |
| 定位城市 | 请求定位权限后用当前位置更新默认城市 | `LocationManager` + QWeather 反查或 Open-Meteo 坐标城市 |
| 城市管理 | 添加城市、删除城市、设置默认城市、查看城市天气摘要 | `CityViewModel` + `CityRepository` + 城市唯一约束 |
| 日历天气 | 未来 7 日日历、农历、节日、生活指数 | `LifeIndexFragment` + `LifeIndexRepository` + 本地兜底指数 |
| 天气预警 | 默认城市官方预警、等级样式、详情、已读状态 | `AlertRepository` + QWeather Warning API + `WarningDao` |
| 我的页面 | 账户信息、默认城市、单位、通知、动画、深色模式、主题、缓存状态 | `MineFragment` + `MineViewModel` + `SettingsManager` |
| 后台通知 | 天气预警通知、每日天气提醒 | WorkManager + NotificationChannel + Android 13 权限检查 |
| 本地缓存 | 网络失败时显示缓存数据 | Room `weather_cache` + `UiState.CACHE` |

## 4. 技术栈与选型理由

| 类型 | 选型 | 为什么这样选 |
| --- | --- | --- |
| 开发语言 | Java | 课程环境兼容性强，适合讲解 Android 基础组件和面向对象分层。 |
| UI | XML Views + ViewBinding | 结构直观，和 AppCompat、Material、Navigation 配合稳定。 |
| 页面架构 | Single Activity + Fragment | 主流程集中在一个 Activity，底部导航页面由 Fragment 承载，避免多 Activity 状态跳转复杂。 |
| 状态管理 | ViewModel + LiveData + `UiState` | 页面状态可以区分 loading、success、error、empty、cache。 |
| 数据层 | Repository + DAO | Repository 处理业务编排，DAO 只处理数据库读写。 |
| 数据库 | Room | 提供实体、DAO、索引和编译期校验，比直接写 SQLite 更可维护。 |
| 网络 | Retrofit 3 + Gson Converter | 接口定义清晰，同步调用放在后台线程，响应模型可测试。 |
| 后台任务 | WorkManager | 适合周期性、受系统调度约束的预警检查和每日提醒。 |
| 通知 | NotificationChannel + NotificationCompat | 兼容 Android 8 通知渠道和 Android 13 通知权限。 |
| 构建 | Gradle Wrapper 8.11.1 + AGP 8.10.0-alpha05 | 使用项目内 Wrapper 固定构建版本，减少环境差异。 |
| 测试 | JUnit4 + Android Instrumented Test | JVM 测核心逻辑，Instrumented Test 保留 Android 运行环境验证入口。 |

项目 SDK 与版本：

| 项 | 值 |
| --- | --- |
| applicationId | `com.litroenade.yunjiweather` |
| namespace | `com.litroenade.yunjiweather` |
| minSdk | 23 |
| compileSdk | 35 |
| targetSdk | 35 |
| versionCode | 1 |
| versionName | 1.0 |
| Java 编译兼容 | Java 11 |
| 推荐 Gradle JVM | JDK 17 或 JDK 21 |

## 5. 页面与导航结构

```text
SplashActivity
└── 根据本地会话进入 AuthActivity 或 MainActivity

AuthActivity
├── 登录模式
└── 注册模式

MainActivity
├── HomeFragment        天气首页
├── LifeIndexFragment   日历天气
├── CityFragment        城市管理
├── AlertFragment       天气预警
└── MineFragment        我的页面
```

底部导航对应：

| 导航 | Fragment | 主要职责 |
| --- | --- | --- |
| 天气 | `HomeFragment` | 展示默认城市天气，处理刷新、定位、缓存提示和天气动画。 |
| 日历 | `LifeIndexFragment` | 展示未来 7 日、农历、节日和生活指数。 |
| 城市 | `CityFragment` | 管理关注城市和默认城市。 |
| 预警 | `AlertFragment` | 展示官方天气预警和已读状态。 |
| 我的 | `MineFragment` | 管理账户、偏好、主题、通知和本地数据。 |

## 6. 包结构说明

```text
app/src/main/java/com/litroenade/yunjiweather
├── auth              登录会话、密码哈希、会话校验
├── common            通用 UI 状态封装
├── data
│   ├── api           Retrofit 客户端、天气网关、API 配置、响应模型
│   ├── entity        Room 实体
│   ├── local         DAO、RoomDatabase、缓存网关
│   ├── model         页面数据模型、生活指数模型、天气模型
│   └── repository    Auth、City、Weather、LifeIndex、Alert 仓库
├── notification      通知渠道和通知发送
├── settings          用户维度设置
├── ui                各页面 Fragment、ViewModel、Adapter、自定义 View
├── utils             天气映射、单位格式化、日期农历、主题、权限等工具
└── worker            WorkManager 后台任务
```

答辩时可以说明：包不是按“工具/页面”随意堆放，而是按职责拆分。UI 层不直接写 SQL，DAO 不关心界面，Repository 负责把网络和缓存组合成业务结果。

## 7. 整体架构设计

```text
用户操作
  ↓
Fragment / Adapter / Custom View
  ↓
ViewModel
  ↓
Repository
  ├── RemoteGateway / Retrofit API
  ├── CacheGateway / Room DAO
  └── SettingsManager / SharedPreferences
  ↓
UiState
  ↓
Fragment 渲染 loading / success / cache / error
```

### 分层职责

| 层 | 负责什么 | 不负责什么 |
| --- | --- | --- |
| Fragment | 绑定视图、响应点击、展示状态 | 不直接拼接口、不直接写数据库业务 |
| ViewModel | 页面数据加载、线程切换、LiveData 状态 | 不持有 Activity 视图引用 |
| Repository | 网络和缓存编排、业务规则、数据降级 | 不操作具体控件 |
| DAO | Room 表查询、插入、更新、删除 | 不处理 UI 和远程接口 |
| Gateway | 把第三方接口响应转换为项目内部模型 | 不关心页面怎么显示 |
| Utils | 纯函数转换，例如天气编码、风力、日期、单位 | 不保存全局状态 |

### 为什么没有堆很多抽象层

毕业设计项目需要结构清晰，但也不能为了“架构感”把简单场景拆到过度复杂。本项目使用 Repository + Gateway + DAO 已经能隔离主要变化点：

- 天气数据源可以从 Open-Meteo 切到 QWeather。
- 缓存实现可以从 Room 替换成其他存储。
- UI 页面只感知 `UiState`，不关心远程接口细节。
- 测试可以直接替换 Gateway 或 DAO 依赖。

## 8. 核心业务流程

### 8.1 启动与登录判断

```text
SplashActivity
  ↓
检查本地 AuthSession
  ↓
未登录：进入 AuthActivity
已登录：进入 MainActivity
  ↓
MainActivity 再校验 userId 是否仍存在
  ↓
初始化底部导航、通知渠道、后台任务
```

关键点：

- `MainActivity` 不只相信 SharedPreferences 中的登录状态，还会通过 `AuthSessionValidator` 查询数据库确认用户仍存在。
- 启动时先显示轻量 loading 页，避免数据库校验期间出现空白。
- 主题模式在进入主页面前应用，减少页面创建后再切深色模式造成的闪烁。

### 8.2 注册与登录

注册流程：

```text
输入用户名、密码、显示名称
  ↓
用户名 trim + 小写归一化
  ↓
校验用户名格式：3-20 位英文、数字或下划线
  ↓
校验密码长度：6-32 位
  ↓
生成随机盐
  ↓
PBKDF2WithHmacSHA256 生成密码哈希
  ↓
写入 user 表
```

登录流程：

```text
输入用户名和密码
  ↓
用户名归一化
  ↓
查询 user 表
  ↓
用保存的 salt 重新计算哈希
  ↓
常量时间比较 hash
  ↓
更新 lastLoginTime 并保存会话
```

设计理由：

- 不保存明文密码。
- 用户名统一小写，避免 `Alice` 和 `alice` 被当成两个账户。
- 密码验证失败时统一返回“用户名或密码错误”，不暴露账号是否存在。
- 常量时间比较避免不同字符串比较耗时差异过大。

### 8.3 首页天气加载

```text
HomeFragment 触发 loadHomeWeather()
  ↓
HomeViewModel 解析当前用户默认城市
  ↓
WeatherRepository.loadHomeWeather()
  ↓
RemoteGateway 请求天气
  ↓
成功：保存 Room 缓存，返回 UiState.SUCCESS
失败：读取 Room 缓存
  ↓
有缓存：返回 UiState.CACHE
无缓存：返回 UiState.ERROR
```

缓存策略：

| 项 | 当前策略 |
| --- | --- |
| 首页天气缓存 TTL | 30 分钟 |
| 缓存存储 | `weather_cache` 表 |
| 缓存粒度 | `ownerUserId + locationId + weatherType` |
| 失败兜底 | 有缓存显示缓存，无缓存显示错误 |
| 过期提示 | 缓存过期时提示“仅供参考” |

### 8.4 城市管理

城市添加有三种来源：

| 来源 | 触发方式 | locationId 形式 |
| --- | --- | --- |
| 预设热门城市 | 北京、上海、广州、深圳 | QWeather 城市 ID，例如 `101010100` |
| QWeather 搜索 | 配置 QWeather 后搜索城市 | QWeather 城市 ID |
| Open-Meteo 搜索 | 未配置 QWeather 时搜索城市 | `openmeteo:<id>` |

设计重点：

- `city` 表使用 `ownerUserId + locationId` 唯一索引，防止同一用户重复添加同一城市。
- 如果第一个城市被添加，会自动成为默认城市。
- 删除默认城市后，如果还有其他城市，会自动选择剩余城市中的第一个作为默认城市。
- 城市摘要通过复用天气仓库加载，每个城市能展示天气概览。

### 8.5 定位城市

首页提供定位入口：

```text
点击定位按钮
  ↓
检查定位权限
  ↓
使用 LocationManager 获取当前位置
  ↓
有 QWeather：用经纬度反查城市
无 QWeather：生成 openmeteo:lat,lon 形式的坐标城市
  ↓
保存为默认城市
  ↓
刷新首页天气
```

异常处理：

- 用户拒绝权限：提示“未授予定位权限，请手动选择城市”。
- 系统定位服务不可用：提示开启系统定位服务。
- 反查失败：回退到原默认城市天气。

### 8.6 日历天气与生活指数

生活指数数据来自 `LifeIndexRepository`：

```text
有 QWeather API
  ↓
请求 QWeather 生活指数
  ↓
保存 6 小时缓存
  ↓
返回远程数据

无 QWeather 或请求失败
  ↓
优先读取有效缓存
  ↓
没有缓存则使用本地默认生活指数
```

设计重点：

- `INDEX_CACHE_TTL_MILLIS` 为 6 小时。
- QWeather 未配置时页面仍然可用，不会因为增强数据源缺失导致日历页崩掉。
- `LifeIndexDefaults` 用于补齐缺失指数，保证 UI 卡片结构稳定。

### 8.7 天气预警

天气预警只来自 QWeather 官方接口。

```text
AlertFragment 刷新预警
  ↓
AlertRepository.refreshWarnings(locationId)
  ↓
未配置 QWeather：读取本地 warning 表
已配置 QWeather：请求官方预警接口
  ↓
把响应映射为 WarningEntity
  ↓
replaceByLocation 更新当前城市预警
  ↓
保留旧预警的已读和已通知状态
```

设计原则：

- 未配置 QWeather 时不伪造官方预警。
- 预警数据用 `ownerUserId + locationId + warningId` 唯一约束。
- 刷新时保留旧记录的 `isRead` 和 `isNotified`，避免用户读过的预警重复变成未读。
- 通知发送成功后才标记 `isNotified`。

### 8.8 我的页面与偏好设置

每个用户的偏好保存在独立 SharedPreferences：

```text
yunji_weather_settings_user_<userId>
```

当前设置项：

| 设置 | 默认值 | 作用 |
| --- | --- | --- |
| 天气预警通知 | 开启 | 控制预警后台通知。 |
| 每日天气提醒 | 关闭 | 控制每日天气摘要通知。 |
| 天气动画 | 开启 | 控制首页天气动画显示。 |
| 深色模式 | 关闭 | 控制 AppCompat 夜间模式。 |
| 温度单位 | 摄氏度 | 控制首页、日历、提醒中的温度显示。 |
| 风速单位 | 风力等级 | 控制风信息展示。 |
| 背景主题 | 晴空主题 | 控制首页和应用背景风格。 |

设置隔离的意义：账户 A 可以使用深色模式和华氏度，账户 B 仍保持默认浅色和摄氏度。

## 9. 数据源设计

### 9.1 为什么使用两个天气源

| 数据源 | 作用 | 优点 | 限制 |
| --- | --- | --- | --- |
| Open-Meteo | 默认天气、空气质量、城市搜索 | 免费、免 Key、项目克隆后可运行 | 没有中国官方天气预警 |
| QWeather | 官方预警、生活指数、增强天气和城市反查 | 中文天气生态更完整，适合预警和指数 | 需要 API Host 和 API Key |

设计逻辑是“基础能力不依赖 Key，增强能力可配置”。这比只接一个需要 Key 的接口更适合答辩，因为老师拿到项目时即使没有 Key 也能跑起来。

### 9.2 QWeather 配置

在项目根目录的 `local.properties` 中加入：

```properties
qweather.apiHost=你的 QWeather API Host
qweather.apiKey=你的 QWeather API Key
```

构建脚本会把它们写入 `BuildConfig`：

```gradle
buildConfigField "String", "QWEATHER_API_HOST", ...
buildConfigField "String", "QWEATHER_API_KEY", ...
```

运行时处理：

- `ApiConfig.getQWeatherBaseUrl()` 会规范化 Host。
- 只接受 HTTPS URL。
- API Key 通过 `X-QW-Api-Key` 请求头发送。
- Host 或 Key 为空时，`ApiConfig.isConfigured()` 返回 false。

### 9.3 数据源切换规则

首页天气使用 `WeatherGatewayFactory` 创建远程网关：

| 条件 | 使用网关 |
| --- | --- |
| QWeather 未配置 | Open-Meteo |
| QWeather 已配置，城市 ID 不以 `openmeteo:` 开头 | QWeather |
| QWeather 已配置，但城市 ID 以 `openmeteo:` 开头 | Open-Meteo |

这样可以避免把 Open-Meteo 搜索出来的城市 ID 错误传给 QWeather。

## 10. 数据库设计

数据库：`yunji_weather.db`

Room 版本：`4`

Room 配置：

```java
Room.databaseBuilder(context, AppDatabase.class, "yunji_weather.db")
        .fallbackToDestructiveMigration()
        .build();
```

当前是课程项目开发阶段，所以使用破坏性迁移。正式上线时应该写清楚版本升级路径，并为每个 schema 变化补 Room Migration。

### 10.1 表设计

| 表 | Entity | 说明 |
| --- | --- | --- |
| `user` | `UserEntity` | 本地账户、密码哈希、密码盐、显示名称、创建时间、最后登录时间。 |
| `city` | `CityEntity` | 用户关注城市、默认城市、经纬度、排序和更新时间。 |
| `weather_cache` | `WeatherCacheEntity` | 天气缓存 JSON、缓存类型、更新时间、过期时间。 |
| `warning` | `WarningEntity` | 天气预警内容、等级、发布时间、已读、已通知。 |

### 10.2 唯一约束

| 表 | 唯一约束 | 目的 |
| --- | --- | --- |
| `user` | `username` | 防止重复注册同名账户。 |
| `city` | `ownerUserId + locationId` | 防止同一用户重复添加城市，同时允许不同用户添加同一城市。 |
| `weather_cache` | `ownerUserId + locationId + weatherType` | 每个用户、城市和缓存类型只有一份最新缓存。 |
| `warning` | `ownerUserId + locationId + warningId` | 防止重复预警，同时保留用户维度状态。 |

### 10.3 用户隔离

| 数据 | 隔离方式 |
| --- | --- |
| 城市 | 每条 `city` 记录带 `ownerUserId`。 |
| 首页天气缓存 | 每条 `weather_cache` 记录带 `ownerUserId`。 |
| 生活指数缓存 | 复用 `weather_cache`，同样带 `ownerUserId`。 |
| 天气预警 | 每条 `warning` 记录带 `ownerUserId`。 |
| 设置 | SharedPreferences 文件名带 `userId`。 |
| 后台任务 | WorkManager unique work name 带 `userId`。 |

答辩演示时可以重点展示：账户 A 添加上海并切深色模式，账户 B 登录后不会继承账户 A 的城市和设置。

## 11. 异常处理与降级设计

### 11.1 天气接口失败

```text
远程接口成功
  → 写缓存
  → 展示最新数据

远程接口失败 + 有缓存
  → 展示缓存
  → 标记 UiState.CACHE
  → 显示缓存更新时间

远程接口失败 + 无缓存
  → 标记 UiState.ERROR
  → 展示明确错误文案
```

### 11.2 QWeather 未配置

| 场景 | 处理 |
| --- | --- |
| 首页天气 | 使用 Open-Meteo。 |
| 城市搜索 | 使用 Open-Meteo Geocoding。 |
| 生活指数 | 读取缓存，没有缓存则使用本地默认指数。 |
| 官方预警 | 不请求接口，不伪造预警，只读取本地预警缓存。 |
| 后台预警 | 直接跳过任务。 |

### 11.3 权限未授予

| 权限 | 处理 |
| --- | --- |
| 定位权限 | 首页定位按钮请求权限；拒绝后提示手动选择城市。 |
| 通知权限 | 发送通知前检查权限；无权限时不发送，不把预警标记为已通知。 |

### 11.4 第三方接口字段异常

QWeather 和 Open-Meteo 的网关层没有用空值默认值掩盖错误，而是对关键字段做 `requireText`、`requireNonNull`、`requireDouble` 等校验。字段缺失会抛出带字段名的 `IOException`，再由 Repository 降级到缓存或错误状态。

## 12. 后台任务与通知设计

### 12.1 WorkManager 任务

| 任务 | 周期 | 条件 | 作用 |
| --- | --- | --- | --- |
| `WeatherAlertWorker` | 6 小时 | 需要网络、当前用户匹配、预警开关开启、QWeather 已配置 | 刷新默认城市预警并发送未通知预警。 |
| `DailyWeatherWorker` | 24 小时 | 需要网络、当前用户匹配、每日提醒开关开启 | 加载默认城市天气并发送每日摘要。 |

任务名：

```text
weather_alert_check_user_<userId>
daily_weather_reminder_user_<userId>
```

为什么任务名要带 userId：

- 用户 A 和用户 B 的默认城市可能不同。
- 用户 A 关闭提醒不应该影响用户 B。
- 退出登录后旧用户任务不应该继续给新用户发通知。

### 12.2 通知渠道

| 渠道 | ID | 重要性 | 用途 |
| --- | --- | --- | --- |
| 天气预警 | `weather_warning` | HIGH | 预警级别更高，需要及时看到。 |
| 每日天气 | `daily_weather` | DEFAULT | 日常提醒，不需要高优先级打扰。 |

发送通知前会检查 `POST_NOTIFICATIONS` 权限，并捕获系统拒绝通知的 `SecurityException`。

## 13. UI 与交互设计

### 13.1 页面风格

项目使用 XML drawable、MaterialCardView、RecyclerView、自定义天气动画 View 和少量位图素材构建天气主题界面。

主要视觉点：

- 天气首页根据主题和天气图标切换背景。
- 支持晴空、幻想夜、樱雨等背景主题。
- 支持深色模式偏好。
- 天气图标可切换为轻量动画。
- 预警等级使用颜色区分蓝、黄、橙、红。
- 首页列表使用横向 RecyclerView 展示逐小时和日历卡片。

位图素材集中在：

```text
app/src/main/res/drawable-nodpi/asset_oga_*.png
```

### 13.2 状态展示

首页不是只有成功态，而是区分：

| 状态 | UI 行为 |
| --- | --- |
| `LOADING` | 显示加载进度。 |
| `SUCCESS` | 展示最新天气。 |
| `CACHE` | 展示缓存天气，并显示缓存提示和更新时间。 |
| `ERROR` | 显示错误容器和重试按钮。 |
| `EMPTY` | 作为空状态兜底。 |

这能回答老师常问的问题：网络差、接口挂了、第一次打开没有缓存时系统怎么表现。

## 14. 测试设计

项目包含：

- 36 个 JVM 单元测试类。
- 1 个 Android Instrumented Test。

测试目录：

```text
app/src/test/java/com/litroenade/yunjiweather
app/src/androidTest/java/com/litroenade/yunjiweather
```

### 14.1 测试覆盖重点

| 测试方向 | 示例 |
| --- | --- |
| 账户安全 | 密码哈希、盐、密码校验、会话校验。 |
| 用户隔离 | 城市、缓存、预警按 `ownerUserId` 隔离。 |
| Repository | 天气请求成功写缓存，失败读缓存，无缓存返回错误。 |
| API 配置 | QWeather Host 规范化、非法 URL 拒绝、Key 判空。 |
| 数据映射 | QWeather 响应、Open-Meteo 天气编码、AQI、风力、图标映射。 |
| 工具函数 | 日期时间、农历、默认城市、位置查询、单位格式化。 |
| UI 辅助 | 城市摘要、预警样式、天气动画类型。 |
| 后台任务 | 用户维度 WorkManager 任务名和当前用户判断。 |

### 14.2 为什么测试有价值

这些测试不是只写 `assertNotNull`，而是验证具体业务结果。例如：

- 删除核心哈希逻辑，密码测试会失败。
- 去掉 `ownerUserId` 条件，用户隔离测试会失败。
- 破坏缓存回退逻辑，Repository 测试会失败。
- 改错 QWeather Host 规范化规则，API 配置测试会失败。

这说明测试覆盖的是业务行为，而不是为了凑数量。

## 15. 构建、运行与 APK

### 15.1 环境要求

| 项 | 建议 |
| --- | --- |
| IDE | IntelliJ IDEA 或 Android Studio |
| Gradle JVM | JDK 17 或 JDK 21 |
| Android SDK | API 35 |
| 设备系统 | Android 6.0（API 23）及以上 |
| 网络 | Open-Meteo 需要联网，QWeather 增强能力需要有效 Host 和 Key |

### 15.2 常用命令

```powershell
.\gradlew.bat :app:compileDebugJavaWithJavac
.\gradlew.bat test
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

有真机或模拟器，并且 `adb` 可用时：

```powershell
.\gradlew.bat connectedAndroidTest
```

### 15.3 APK 输出位置

Debug APK：

```text
app/build/outputs/apk/debug/app-debug.apk
```

Android Test APK：

```text
app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
```

Release APK 默认位置：

```text
app/build/outputs/apk/release/app-release.apk
```

说明：当前项目没有正式 release 签名配置，毕业设计答辩、本地安装和演示建议使用 debug APK。

## 16. 答辩演示路线

建议按下面路线演示，避免一上来就陷入代码细节：

1. **展示 README 首页**：说明项目不是接口 Demo，而是完整天气生活服务 App。
2. **启动 App**：展示开屏页和自动登录判断。
3. **注册账户 A**：说明本地账户、密码哈希和用户数据隔离。
4. **进入天气首页**：展示实时天气、体感、湿度、风、气压、能见度、AQI、生活建议、逐小时和未来三天。
5. **点击刷新**：说明 Repository 远程请求和 Room 缓存写入。
6. **点击定位**：展示定位权限和当前位置默认城市更新；如果环境不方便定位，则说明失败回退。
7. **城市页添加城市**：添加上海或深圳，设置默认城市，回首页查看变化。
8. **日历页**：展示未来 7 日、农历、节日和生活指数。
9. **预警页**：说明官方预警只来自 QWeather，未配置时不会伪造数据。
10. **我的页面**：切换单位、动画、深色模式、背景主题，展示缓存状态。
11. **退出登录并注册账户 B**：说明账户 A 和 B 数据隔离。
12. **展示测试目录**：说明核心逻辑有单元测试支撑。

## 17. 老师可能追问的问题

### Q1：为什么不用真实后端账户？

因为毕业设计重点是 Android 客户端工程能力：页面、网络、缓存、数据库、通知、权限和测试。真实后端会增加部署、接口安全和运维成本，不利于聚焦移动端。为了仍然展示账户体系，我用 Room 实现本地账户，并用密码哈希、盐和用户维度隔离保证基本安全性和完整性。

### Q2：为什么不用 Jetpack Compose？

本项目选择 Java + XML Views 是为了保证课程环境可运行、代码结构容易讲解，并且更贴合传统 Android 教学栈。XML + ViewBinding 已经可以实现完整 UI 和状态绑定。Compose 是可扩展方向，但不是当前项目必要条件。

### Q3：为什么要两个天气数据源？

Open-Meteo 免 Key，保证项目下载后就能运行；QWeather 提供官方预警和中文生活指数，适合作为增强能力。这样的组合比只依赖一个收费或需要 Key 的接口更稳，答辩环境也不容易因为 Key 问题导致项目不可演示。

### Q4：QWeather 没配置时预警怎么处理？

不伪造。预警页只读取已有缓存或提示当前没有官方预警数据。后台预警任务也会在 `ApiConfig.isConfigured()` 为 false 时直接跳过。这样保证数据来源诚实。

### Q5：网络失败时首页会不会空白？

不会。`WeatherRepository` 会先尝试远程请求，失败后读 Room 缓存。有缓存时返回 `UiState.CACHE` 并显示缓存更新时间；没有缓存才返回错误状态和重试入口。

### Q6：怎么保证多账户数据不串？

数据库中的城市、天气缓存和预警都带 `ownerUserId`；用户设置的 SharedPreferences 文件名带 userId；WorkManager 后台任务名也带 userId。查询和更新都按当前登录用户执行。

### Q7：后台任务为什么不用普通线程或定时器？

普通线程在应用退出后不可靠，也不适合系统电量管理。WorkManager 是 Android 推荐的可延迟后台任务方案，适合预警检查和每日提醒这种不要求秒级实时但需要可靠调度的任务。

### Q8：项目最大的难点是什么？

不是把天气接口调通，而是把多个移动端能力串起来：登录态、默认城市、远程天气、缓存、预警、通知、设置和多账户隔离之间互相影响。比如预警通知必须知道当前用户、默认城市、QWeather 是否配置、通知权限是否授予、预警是否已经通知过。

### Q9：如果正式上线还要补什么？

需要补真实后端账户、多设备同步、Room Migration、正式 release 签名、混淆策略、隐私政策、权限弹窗说明、API Key 管理、崩溃监控和更完整的 UI 自动化测试。

## 18. 核心代码定位

| 关注点 | 文件 |
| --- | --- |
| 主页面、导航、后台任务注册 | `app/src/main/java/com/litroenade/yunjiweather/MainActivity.java` |
| 登录注册业务 | `app/src/main/java/com/litroenade/yunjiweather/data/repository/AuthRepository.java` |
| 密码哈希 | `app/src/main/java/com/litroenade/yunjiweather/auth/AuthPasswordUtils.java` |
| 首页状态 | `app/src/main/java/com/litroenade/yunjiweather/ui/home/HomeViewModel.java` |
| 首页 UI | `app/src/main/java/com/litroenade/yunjiweather/ui/home/HomeFragment.java` |
| 天气仓库 | `app/src/main/java/com/litroenade/yunjiweather/data/repository/WeatherRepository.java` |
| 数据源选择 | `app/src/main/java/com/litroenade/yunjiweather/data/api/WeatherGatewayFactory.java` |
| Open-Meteo 网关 | `app/src/main/java/com/litroenade/yunjiweather/data/api/OpenMeteoRemoteGateway.java` |
| QWeather 网关 | `app/src/main/java/com/litroenade/yunjiweather/data/api/QWeatherRemoteGateway.java` |
| API 配置 | `app/src/main/java/com/litroenade/yunjiweather/data/api/ApiConfig.java` |
| Room 数据库 | `app/src/main/java/com/litroenade/yunjiweather/data/local/AppDatabase.java` |
| 城市仓库 | `app/src/main/java/com/litroenade/yunjiweather/data/repository/CityRepository.java` |
| 生活指数仓库 | `app/src/main/java/com/litroenade/yunjiweather/data/repository/LifeIndexRepository.java` |
| 预警仓库 | `app/src/main/java/com/litroenade/yunjiweather/data/repository/AlertRepository.java` |
| 用户设置 | `app/src/main/java/com/litroenade/yunjiweather/settings/SettingsManager.java` |
| 通知 | `app/src/main/java/com/litroenade/yunjiweather/notification/NotificationHelper.java` |
| 预警后台任务 | `app/src/main/java/com/litroenade/yunjiweather/worker/WeatherAlertWorker.java` |
| 每日提醒任务 | `app/src/main/java/com/litroenade/yunjiweather/worker/DailyWeatherWorker.java` |

## 19. 当前不足与后续扩展

| 不足 | 原因 | 改进方向 |
| --- | --- | --- |
| 本地账户不能跨设备同步 | 当前没有后端 | 接入 Spring Boot、Firebase 或 Supabase 等后端服务。 |
| Room 使用破坏性迁移 | 开发期迭代快 | 为每个数据库版本补 Migration。 |
| Release 未签名 | 当前用于课程答辩和本地演示 | 配置 keystore、签名、混淆和打包流程。 |
| 预警依赖 QWeather | Open-Meteo 不提供中国官方预警 | 增加更多预警数据源或服务端聚合。 |
| UI 自动化覆盖少 | 当前重点在核心逻辑单测 | 增加 Espresso 页面流测试和截图回归。 |
| 隐私合规文档不完整 | 未正式上线 | 补隐私政策、权限说明、数据删除策略。 |

## 20. License

本项目使用 MIT License，详见 [LICENSE](LICENSE)。
