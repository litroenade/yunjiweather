# 云迹天气重构实现状态

更新日期：2026-05-25

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
- 城市页恢复北京、上海、广州、深圳四个热门城市快捷添加入口。
- 首页加入按天气类型变化的背景和 Compose 天气动效；关闭动效设置时回退静态天气图标。
- 首页加入导航栏 inset 避让，去掉底栏后内容不会贴住系统导航栏。
- 预警刷新拆出 `WarningRepository`、`AlertRemoteGateway`、`WarningStore`、`QWeatherWarningMapper`、`WarningRefreshResult`、`WarningSource` 等边界。
- 系统预警通知副作用移动到 Worker/Notifier/Dispatcher，不在页面刷新时直接发送。
- 城市查询从 ViewModel 下沉到 `CityLookupGateway`。
- 城市天气摘要刷新抽到 `CityWeatherSummaryRepository`。
- 删除旧 Fragment、Adapter、ViewBinding layout、navigation/menu XML 和无引用底部 tab 图标。
- Android XML 主题从 MaterialComponents 父主题切回平台 Material 主题，主包移除 AppCompat/Material Components 依赖。
- 仪器测试源码同步到当前架构：验证无登录启动、Room v5 单机 schema、无 `user` 表和无 `ownerUserId` 列。

## 未实现

- 多城市首页左右分页未实现；当前仍以默认城市作为首页城市。
- 首页没有完整复刻华为天气的全部模块，比如空气质量地图、日出日落曲线、潮汐、风向风力大卡、天气资讯图文卡等。
- 分享、反馈当前天气、桌面天气是保留入口和说明弹窗，尚未接系统分享面板、反馈通道或小组件能力。
- 生活指数远端访问和缓存 store 尚未独立抽成 `LifeIndexRemoteGateway` / store。
- 非 UI Java 数据层尚未全量迁移 Kotlin。
- Compose UI 自动化测试和真机截图验收未补齐。
- `connectedAndroidTest` 未运行；当前环境没有确认可用 adb 设备。
- CodeRabbit review 未运行；当前 Windows 环境没有可用 `coderabbit` CLI/WSL 运行条件。

## 本轮验证

已通过：

```powershell
$env:JAVA_HOME='C:\Users\litroenade\Documents\env\java17'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :app:compileDebugKotlin --no-daemon
.\gradlew.bat :app:testDebugUnitTest --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --no-daemon
```

`assembleDebug` 干净构建通过；仅剩 `libandroidx.graphics.path.so` 无法 strip 的打包提示，D8 Kotlin metadata 警告已通过 Kotlin/AGP 版本对齐消除。
