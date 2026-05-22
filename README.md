# 云迹天气 YunJi Weather

云迹天气是一个原生 Android 天气生活服务 App，使用 Java 和 XML Views 实现，面向课程项目、期末展示和个人作品集。项目目标不是做一个简单接口 Demo，而是做成一个有启动流程、登录注册、账户隔离、天气查询、城市管理、生活指数、预警通知、缓存回退和设置页的完整移动端应用。

当前默认数据源是 Open-Meteo，不需要 API Key；QWeather 是可选增强源，用于生活指数和官方天气预警。未配置 QWeather 时，应用不会伪造官方预警，只会显示缓存或明确提示数据源未配置。

## 当前实现状态

- 开屏页：启动动画、应用名称、天气主题渐变背景、自动判断登录状态。
- 登录注册：本地账户体系，用户名归一化，密码使用 PBKDF2WithHmacSHA256 加盐哈希。
- 首页天气：实时天气、小时预报、未来预报、空气质量、穿衣建议、出行建议、天气日历、动态背景和轻量天气动画。
- 城市管理：城市搜索、添加、删除、设置默认城市、防重复添加、城市天气摘要。
- 生活指数：穿衣、出行、运动、洗车、紫外线、感冒、空气、舒适度、晾晒、旅游等指数。
- 天气预警：默认城市预警、预警详情、已读状态、通知去重。
- 我的页面：账户信息、默认城市、数据更新时间、单位设置、通知开关、动画开关、深色模式、缓存清理、帮助、数据源说明、项目运行状态。
- 本地能力：Room 持久化、SharedPreferences 设置隔离、网络失败缓存回退、WorkManager 后台任务、Android 13 通知权限适配。

## 技术栈

| 模块 | 选型 |
| --- | --- |
| 项目类型 | 原生 Android 天气生活服务 App |
| 语言 | Java |
| IDE | IntelliJ IDEA 为主，Android Studio 可作为补充 |
| 构建 | Gradle Wrapper 8.11.1 + Android Gradle Plugin 8.10.0-alpha05 |
| 推荐 Gradle JVM | JDK 17 或 JDK 21 |
| Java 编译兼容 | sourceCompatibility / targetCompatibility Java 11 |
| UI | XML Views + ViewBinding + Material Components |
| 布局 | ScrollView / LinearLayout / ConstraintLayout / MaterialCardView |
| 列表 | RecyclerView + 部分轻量动态 LinearLayout |
| 架构 | Splash + Auth + Single Activity + Fragment + MVVM + Repository + DAO |
| 导航 | Jetpack Navigation + BottomNavigationView |
| 状态管理 | LiveData + MutableLiveData + UiState |
| 网络 | Retrofit + OkHttp + Gson |
| 数据库 | Room |
| 本地设置 | SharedPreferences |
| 天气数据源 | Open-Meteo 默认源，QWeather 可选增强源 |
| 后台任务 | WorkManager |
| 通知 | NotificationChannel + Android 13 POST_NOTIFICATIONS 权限 |
| 权限 | 定位权限、通知权限 |
| 测试 | JUnit4 + Android Instrumented Test |

## 为什么这样选型

本项目采用 Java + XML Views，是为了保证课程项目在 IntelliJ IDEA 和常规 Android 环境中容易运行、容易讲解、容易维护。MVVM + Repository + Room 的结构能把 UI、状态、网络、缓存和数据库职责拆开，但没有引入过重的 Clean Architecture 层级。

Open-Meteo 作为默认源可以让项目不依赖 API Key 直接运行；QWeather 只作为增强源，避免未配置 Key 时首页不可用。账户系统采用本地 Room 方案，不连接真实后端，适合单人课程项目展示用户隔离和本地安全存储思路。

## 应用页面结构

```text
SplashActivity
└── AuthActivity 或 MainActivity

AuthActivity
├── 登录模式
└── 注册模式

MainActivity
├── HomeFragment          天气：当日天气、小时预报、未来预报、生活建议
├── LifeIndexFragment     日历：农历、节日、未来 7 日天气和生活指数
├── CityFragment          城市：城市搜索、关注城市和默认城市
├── AlertFragment         预警：官方预警、已读状态和通知状态
└── MineFragment          我的：账户、主题、设置和项目状态
```

## 本地账户与数据隔离

账户系统完全运行在本地：

- 不接真实后端。
- 不上传密码。
- 不保存明文密码。
- 退出登录只清除当前会话，不删除用户数据。

隔离范围：

- 城市列表：`city.ownerUserId`
- 天气缓存：`weather_cache.ownerUserId`
- 预警记录：`warning.ownerUserId`
- 用户设置：`yunji_weather_settings_user_<userId>`

这意味着账户 A 添加的城市、缓存、设置和预警状态不会影响账户 B。

## 数据库设计

Room 数据库版本：`3`。

当前开发期使用 `fallbackToDestructiveMigration()`，因此不保留旧版本本地数据兼容，适合课程项目快速迭代。如果要改成正式生产策略，需要补 Room Migration。

主要表：

- `user`：本地账户。
- `city`：账户隔离的城市列表和默认城市。
- `weather_cache`：账户隔离的天气、首页、生活指数和预警缓存。
- `warning`：账户隔离的预警记录、已读状态和已通知状态。

核心唯一约束：

- `user.username`
- `city.ownerUserId + city.locationId`
- `weather_cache.ownerUserId + weather_cache.locationId + weather_cache.weatherType`
- `warning.ownerUserId + warning.warningId`

`weather_cache.weatherType` 常用值：

```text
HOME
INDEX
NOW
DAILY
HOURLY
AIR
WARNING
```

## API 数据源

默认可用：

- Open-Meteo 天气预报
- Open-Meteo 空气质量
- Open-Meteo 城市搜索

可选增强：

- QWeather 生活指数
- QWeather 官方天气预警
- QWeather 城市查询增强

## 视觉素材

经典晴空主题使用 XML drawable 自绘，不依赖外部位图。幻想夜和樱雨粉主题使用了少量 OpenGameArt 位图素材作为背景装饰，来源记录见 [ASSET_CREDITS.md](ASSET_CREDITS.md)。

## QWeather 配置

默认不需要配置 API Key。需要启用 QWeather 增强能力时，在项目根目录的 `local.properties` 中加入：

```properties
qweather.apiHost=你的 QWeather API Host
qweather.apiKey=你的 QWeather API Key
```

`local.properties` 不应提交到 Git。

## IntelliJ IDEA 运行方式

1. 使用 IntelliJ IDEA 打开项目根目录。
2. 等待 Gradle Sync 完成。
3. 在 IntelliJ IDEA 的 Gradle 设置中，将 Gradle JVM 设为 JDK 17 或 JDK 21。
4. 选择 `app` 运行配置。
5. 运行到 API 23+ 模拟器或真机。
6. 在 Build Output 查看编译结果。
7. 在 Logcat 中过滤 `AndroidRuntime|FATAL EXCEPTION|YunJiWeather` 排查运行问题。

Android Studio 也可以运行本项目，但不是本文档的主工作流。

## 验证命令

推荐在 IntelliJ IDEA 的 Gradle 工具窗口运行：

```bash
gradle test
gradle lintDebug
gradle assembleDebug
```

也可以使用项目自带 Gradle Wrapper：

```powershell
.\gradlew.bat :app:compileDebugJavaWithJavac
.\gradlew.bat :app:compileDebugUnitTestJavaWithJavac
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

有设备或模拟器，并且 `adb` 可用时再运行：

```bash
gradle connectedAndroidTest
```

## 当前环境限制

当前命令行环境如果使用 JDK 25，完整 `gradle test` 可能受 AGP `8.10.0-alpha05` 兼容性影响，在 Android 单元测试任务创建阶段出现 `Type T not present`。课程展示和本地开发建议使用 IntelliJ IDEA，并把 Gradle JVM 设置为 JDK 17 或 JDK 21。

## APK 输出位置

Debug APK：

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release APK：

```text
app/build/outputs/apk/release/app-release.apk
```

## 课程展示建议

1. 启动应用，展示开屏动画和自动登录判断。
2. 注册账户 A，进入首页查看天气、空气质量、生活建议和天气日历。
3. 在城市页添加城市，设置默认城市，展示城市摘要。
4. 在生活指数页展示指数卡片和详情。
5. 在预警页说明官方预警只来自 QWeather，未配置时不会伪造数据。
6. 在我的页展示账户信息、数据隔离、本地存储状态、项目运行状态和设置项。
7. 退出登录后注册账户 B，展示两个账户的数据互不影响。

## 后续可扩展方向

- 接入真实后端账户系统。
- 增加 Room Migration，保留升级前本地数据。
- 将城市页动态卡片逐步迁移为 RecyclerView。
- 增加更多天气动画资源。
- 增加 release 签名、混淆和隐私合规说明。

## License

本项目使用 MIT License，详见 [LICENSE](LICENSE)。
