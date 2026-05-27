# 云迹天气重构实现状态

更新日期：2026-05-27

## 参考结论

本轮重新核对了华为天气官方资料和截图方向。华为天气的核心不是底部 tab，而是一个城市天气首页：顶部提供城市管理、搜索和更多菜单；城市增删、常驻地、设置等从管理页或菜单进入；天气详情以首页卡片和动态背景纵向承载。

对应资料：
- 华为天气服务介绍：`https://consumer.huawei.com/cn/mobileservices/weather/`
- 管理城市说明：`https://consumer.huawei.com/cn/support/content/zh-cn16010058/`
- 添加/删除城市说明：`https://consumer.huawei.com/cn/support/content/zh-cn15951820/`

## 构建约束

- 因 IDEA 兼容要求，AGP 保持 `8.10.0-alpha05`。
- Gradle Wrapper 保持 `8.11.1`。
- `compileSdk` / `targetSdk` 保持 `35`。
- Compose 使用 `compose-bom:2025.04.01` 和 `activity-compose:1.10.1`，避免引入要求 `compileSdk 36` 的新 Activity / NavigationEvent 链路。
- Kotlin 固定 `2.1.21`，避免 AGP 8.10.0-alpha05 内置 R8 对 Kotlin 2.2/2.3 metadata 产生大量 D8 警告。
- Java Room DAO/Entity 继续使用 `annotationProcessor`，没有引入 KSP/KAPT。

## 已实现

- 删除登录、本地账号、session gate、auth repository/session/password 工具类和相关单测。
- Room 升到 v5，业务表去掉 `ownerUserId`，`user` 表不再属于 schema。
- `SettingsManager` 改为固定本机 SharedPreferences 文件，不再按用户拆分。
- WorkManager 改为固定 unique work name，并保留首次清理旧 user-scoped work 的逻辑。
- `MainActivity`、`SplashActivity` 已迁移为 Kotlin `ComponentActivity` + Compose。
- 主界面从五个底部 tab 改为单页天气首页。
- 首页顶部新增城市管理、搜索、更多菜单；搜索入口打开自动聚焦的城市搜索弹层，城市管理入口打开管理视角。
- 首页新增“更多天气服务”卡片，并在更多菜单接入天气预警和生活指数弹层。
- 城市管理和设置页改为 `ModalBottomSheet`，关闭城市弹层后刷新首页默认城市天气。
- 首页已接入当前位置定位入口；授权后读取系统最近/单次定位坐标，复用 `HomeViewModel.updateDefaultCityByLocation()` 和城市反查链路切换默认城市。
- 城市管理页切换默认城市成功后会关闭弹层并刷新首页，不再依赖用户手动关闭弹层。
- 首页预报数据对齐到 12 小时逐小时和最多 7 天多日预报；多日预报行补入农历/节日信息。
- 首页已支持多城市横向分页：城市列表来自 Room，左右滑动首页天气页会按对应城市重新加载天气，城市胶囊展示当前页。
- 首页补入风和风力大卡、空气质量概览、UV、日出日落轨迹卡、天气资讯/生活热点卡片。
- 空气质量概览补入 AQI 健康活动建议和敏感人群提示；本地资讯流也会在空气质量较差时优先生成空气质量提醒。
- 日出日落轨迹卡已按当前分钟计算太阳位置：日出前停在起点，日出到日落按进度移动，日落后停在终点。
- 天气资讯卡已明确为本地资讯流：优先显示当前城市本地预警摘要，无预警时显示空气质量、出行建议和穿衣建议。
- QWeather / Open-Meteo 首页天气聚合模型已透传 `uvIndex`、`sunrise`、`sunset`；旧缓存缺字段时不会崩溃，刷新后补齐。
- 生活指数页补入今日公历/星期/农历/节日卡；`openmeteo:` 坐标城市不会再被拿去请求 QWeather 生活指数接口。
- 设置页已补回天气偏好、单位与主题、应用信息三段结构；应用信息包含本地存储状态、项目运行状态、清理天气缓存、数据来源说明、开发与验证说明、使用帮助、关于应用。
- 城市页恢复北京、上海、广州、深圳四个热门城市快捷添加入口。
- 首页加入按天气类型变化的背景和 Compose 天气动效；关闭动效设置时回退静态天气图标。
- 首页加入导航栏 inset 避让，去掉底栏后内容不会贴住系统导航栏。
- 预警刷新拆出 `WarningRepository`、`AlertRemoteGateway`、`WarningStore`、`QWeatherWarningMapper`、`WarningRefreshResult`、`WarningSource` 等边界。
- 系统预警通知副作用移动到 Worker/Notifier/Dispatcher，不在页面刷新时直接发送。
- 城市查询从 ViewModel 下沉到 `CityLookupGateway`。
- 城市天气摘要刷新抽到 `CityWeatherSummaryRepository`。
- 分享入口已接入系统 `ACTION_SEND` 分享面板，分享文案由 `WeatherShareUtils` 生成并有 JVM 单测覆盖。
- 反馈当前天气入口已接入系统邮件/文本反馈 Intent，携带当前天气摘要和问题描述模板。
- 桌面天气已新增经典 `AppWidgetProvider` / RemoteViews 小组件、provider XML、manifest receiver；小组件会异步读取默认城市和 `HOME` 首页天气缓存，有缓存时渲染城市、温度、天气、温度范围、更新时间，无缓存或旧缓存缺字段时提示打开应用更新。
- 首页天气刷新成功、定位切换默认城市后会触发已安装小组件刷新。
- 生活指数已拆出 `LifeIndexRemoteGateway`、`LifeIndexStore` 和 `QWeatherLifeIndexRemoteGateway`；`LifeIndexRepository` 只保留 openmeteo 跳过、未配置读缓存/默认建议、远端成功写缓存、远端失败回退缓存/默认建议的策略。
- `WeatherDailyData`、`WeatherHourlyData`、`LifeIndexItem`、`LifeIndexMapper`、`LifeIndexDefaults`、`CityWeatherSummary` 已迁移 Kotlin，保持 Java 构造器、getter 和静态调用面兼容。
- `CityRepository`、`WarningStore`、`WeatherRepositoryFactory` 已迁移 Kotlin，并用 Java 回归测试锁住 nullable 返回、默认城市分支、WarningStore 转发和 `@JvmStatic` 工厂入口。
- `WarningSource`、`AlertRemoteGateway`、`HomeWeatherSource`、`WarningRefreshResult` 已迁移 Kotlin，并保留 Java 侧调用/实现兼容。
- `LifeIndexRemoteGateway`、`LifeIndexStore`、`QWeatherLifeIndexRemoteGateway`、`QWeatherWarningMapper` 已迁移 Kotlin，并通过 JVM 回归测试验证 Java 调用面和异常语义。
- 删除旧 Fragment、Adapter、ViewBinding layout、navigation/menu XML 和无引用底部 tab 图标。
- Android XML 主题从 MaterialComponents 父主题切回平台 Material 主题，主包移除 AppCompat/Material Components 依赖。
- 仪器测试源码同步到当前架构：验证无登录启动、Room v5 单机 schema、无 `user` 表和无 `ownerUserId` 列。

## 未实现 / 受限

- 空气质量概览当前是基于当前城市 AQI 的本地概览卡，没有接入第三方地图 SDK、空气质量瓦片或热力图数据。
- 天气资讯当前只使用本地预警、空气质量和生活建议生成应用内资讯卡，未接新闻/资讯 SDK。
- 潮汐模块没有实现；当前项目没有潮汐数据源、海岸站点模型或权限边界。
- 非 UI 数据层尚未全量迁移 Kotlin；Room Entity/DAO 暂受 `annotationProcessor` 约束，Retrofit/Gson DTO 暂保留 public field Java 形态。
- Compose UI 已补入 `YunJiTheme` smoke test 源码并接入 `ui-test-junit4`；完整真机截图验收未补齐。
- `connectedDebugAndroidTest` 已尝试运行，但当前 adb 没有连接设备，Gradle 报 `No connected devices!`，因此没有真机测试结果和截图产物。
- CodeRabbit review 未运行；当前 Windows 环境没有可用 `coderabbit` CLI/WSL 运行条件。

## 本轮验证

已通过：

```powershell
$env:JAVA_HOME='C:\Users\litroenade\Documents\env\java17'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :app:testDebugUnitTest --no-daemon
.\gradlew.bat :app:compileDebugKotlin :app:compileDebugJavaWithJavac :app:compileDebugAndroidTestKotlin :app:compileDebugAndroidTestJavaWithJavac --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon
git diff --check
```

本轮环境检查：

```powershell
C:\Users\litroenade\Documents\env\androidsdk\platform-tools\adb.exe devices
```

`adb devices` 没有列出设备；本轮未运行 `connectedDebugAndroidTest` 和截图采集，避免伪造真机结果。历史尝试失败原因为 `No connected devices!`。
