# 项目目录结构

本文档用于快速定位代码，适合答辩、交接和后续维护。路径均以仓库根目录 `yunjiweather/` 为基准。

## 1. 根目录

```text
yunjiweather/
|-- app/                         Android 应用模块
|-- docs/                        项目文档
|-- gradle/wrapper/              Gradle Wrapper
|-- scripts/                     辅助脚本
|-- build.gradle                 根构建脚本，包含插件版本
|-- settings.gradle              Gradle 工程设置
|-- gradle.properties            Gradle 参数
|-- gradlew / gradlew.bat        Wrapper 启动脚本
`-- README.md                    项目简介
```

根目录中可能出现本地生成文件，例如 JetBrains inspection 导出的 `index.html`、`script.js`、`styles.css`。这些不是项目源码，已在 `.gitignore` 中按根目录文件忽略；需要保留报告时建议移动到 `artifacts/` 下。

本地产物目录：

```text
artifacts/                      QA、截图、logcat、gfxinfo 等本地验收产物，已忽略
.qa/                            手工 QA 图片材料，当前包含已跟踪截图
image/                          README 或说明文档使用的图片资源，当前包含已跟踪图片
```

## 2. 文档目录

```text
docs/
|-- README.md                    文档索引
|-- development-guide.md         开发文档
|-- project-structure.md         当前目录结构说明
|-- defense-guide.md             答辩提纲
|-- packages.md                  Dokka 包说明
`-- superpowers/                 工具链生成的辅助材料
```

`packages.md` 被 `app/build.gradle` 中 Dokka 配置引用，移动或改名时需要同步构建脚本。

## 2.1 脚本目录

```text
scripts/
`-- android-ui-qa.ps1            Android 真机/模拟器 UI 验收脚本
```

`android-ui-qa.ps1` 会安装 debug APK、清理并导出 logcat、截屏、采集 gfxinfo/meminfo，并写入 `artifacts/ui_qa/`。运行前需要连接设备或模拟器，并配置 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`；也可以通过 `-AdbPath` 显式传入 adb 路径。

## 3. Android 主模块

```text
app/
|-- build.gradle                 App 模块构建脚本
|-- proguard-rules.pro           Release 混淆保留规则
`-- src/
    |-- main/
    |-- test/
    `-- androidTest/
```

### 3.1 app/src/main

```text
app/src/main/
|-- AndroidManifest.xml          Activity、Application、权限、Receiver、Worker 入口声明
|-- assets/themes/               主题 manifest
|-- java/com/litroenade/yunjiweather/
`-- res/
```

### 3.2 主题资源

```text
app/src/main/assets/themes/
|-- official/manifest.json       默认主题
|-- panorama/manifest.json       全景天气主题
|-- fantasy/manifest.json        幻想乡预留主题
`-- custom/manifest.json         自定义主题槽位
```

这些 manifest 用于描述主题身份、标题、状态和展示信息。真正的渲染逻辑在 `ui/compose/theme`。

## 4. 主源码包

```text
com/litroenade/yunjiweather/
|-- MainActivity.kt
|-- YunJiWeatherApplication.java
|-- common/
|-- data/
|-- di/
|-- domain/
|-- notification/
|-- ui/
|-- utils/
|-- widget/
`-- worker/
```

### 4.1 应用入口

| 文件 | 职责 |
| --- | --- |
| `MainActivity.kt` | 主 Activity，装载 Compose、处理定位权限、初始化后台任务和主题状态 |
| `YunJiWeatherApplication.java` | Hilt Application 入口 |

## 5. data 数据层

```text
data/
|-- api/
|-- entity/
|-- local/
|-- model/
`-- repository/
```

### 5.1 data/api

```text
data/api/
|-- ApiClient.java
|-- CityLookupGateway.java
|-- OpenMeteoApiService.java
|-- OpenMeteoCitySearchGateway.java
|-- OpenMeteoRemoteGateway.java
`-- model/
```

职责：

- 创建 Retrofit 客户端。
- 定义 Open-Meteo API。
- 实现天气、空气质量、城市搜索、经纬度反查。
- 将远端响应转换为业务模型。

`data/api/model` 中保留 Open-Meteo DTO：

```text
data/api/model/
|-- OpenMeteoAirQualityResponse.java
|-- OpenMeteoForecastResponse.java
`-- OpenMeteoGeocodingResponse.java
```

### 5.2 data/entity

```text
data/entity/
|-- CityEntity.java
|-- WeatherCacheEntity.java
`-- WarningEntity.java
```

职责：

- 定义 Room 表结构。
- 保持 Java 实体，降低 Room 注解处理迁移风险。
- 当前已经没有用户表和 `ownerUserId` 字段。

### 5.3 data/local

```text
data/local/
|-- AppDatabase.java
|-- CityDao.java
|-- WeatherCacheDao.java
|-- WarningDao.java
|-- RoomWeatherCacheGateway.java
|-- LifeIndexCacheGateway.java
|-- WeatherCacheTypes.kt
`-- prefs/
```

职责：

- Room 数据库入口。
- DAO 定义。
- 天气缓存和生活建议缓存读写。
- 统一缓存类型常量，例如首页天气缓存 key。

### 5.4 data/local/prefs

```text
data/local/prefs/
|-- SettingsPreferencesDataSource.java
`-- CustomThemeImageStore.kt
```

职责：

- 读写本地设置 SharedPreferences。
- 兼容旧设置值。
- 保存自定义主题裁剪后的图片文件。

### 5.5 data/model

```text
data/model/
|-- HomeWeatherData.java
|-- HomeWeatherInsight.kt
|-- HomeWeatherInsightBuilder.kt
|-- CityWeatherSummary.kt
|-- WeatherHourlyData.kt
|-- WeatherDailyData.kt
|-- LifeIndexItem.kt
|-- LifeIndexDefaults.kt
`-- CustomThemeCropAnchor.java
```

职责：

- 表示 UI 和 Repository 之间传递的业务模型。
- 首页天气、小时预报、多日预报、生活建议、城市天气摘要都在这里。
- `LifeIndexDefaults.kt` 负责本地生活建议规则。
- `HomeWeatherInsightBuilder.kt` 负责首页资讯摘要规则。

### 5.6 data/repository

```text
data/repository/
|-- WeatherRepository.java
|-- HomeWeatherSource.kt
|-- HomeWeatherCacheSource.java
|-- CityRepository.kt
|-- CityWeatherSummaryRepository.java
|-- LifeIndexRepository.java
|-- LifeIndexStore.kt
|-- AlertRepository.java
|-- WarningStore.kt
|-- WarningSource.kt
|-- WarningRefreshResult.kt
`-- SettingsRepository.java
```

职责：

- Repository 是数据策略层。
- 决定使用远端、缓存还是本地规则。
- 处理弱网 fallback 和缓存过期。
- 隐藏底层 Gateway、DAO、SharedPreferences 细节。

## 6. domain 用例层

```text
domain/usecase/
|-- LoadHomeWeatherPageUseCase.java
|-- LoadLifeIndexUseCase.java
|-- RefreshAllCityWeatherCacheUseCase.java
|-- RefreshWeatherWidgetUseCase.java
|-- RefreshWarningsUseCase.java
|-- DispatchWarningNotificationsUseCase.java
`-- SendDailyWeatherReminderUseCase.java
```

职责：

- 组织一个完整业务动作。
- 将跨 Repository 的流程集中起来。
- 降低 ViewModel 复杂度。

典型例子：

- 首页加载需要城市列表、默认城市、天气缓存、远端刷新、预警摘要，所以放在 `LoadHomeWeatherPageUseCase`。
- 小组件刷新需要默认城市、天气仓库、缓存和 RemoteViews 更新，所以放在 `RefreshWeatherWidgetUseCase`。

## 7. di 依赖注入

```text
di/
|-- DatabaseModule.java
|-- NetworkModule.java
|-- RepositoryModule.java
`-- UseCaseModule.java
```

职责：

- 为 Hilt 提供数据库、DAO、网络服务、Repository、UseCase。
- 替代旧的手动单例和工厂创建方式。
- 让 ViewModel 和 Worker 依赖更容易测试和替换。

## 8. ui 表现层

```text
ui/
|-- alert/
|-- city/
|-- compose/
|-- home/
|-- index/
|-- location/
|-- mine/
`-- splash/
```

### 8.1 ViewModel 包

| 包 | 文件 | 职责 |
| --- | --- | --- |
| `ui/home` | `HomeViewModel.java` | 首页天气、切城、刷新、定位反查后的默认城市更新 |
| `ui/city` | `CityViewModel.java` | 城市搜索、添加、删除、默认城市、城市天气摘要 |
| `ui/alert` | `AlertViewModel.java` | 预警列表、刷新、已读状态 |
| `ui/index` | `LifeIndexViewModel.java` | 生活建议页面状态 |
| `ui/mine` | `MineViewModel.java` | 设置、主题、模块排序、开发者工具 |
| `ui/splash` | `SplashActivity.kt`、`SplashViewModel.java` | 启动页和初始化 |

### 8.2 ui/location

```text
ui/location/
|-- AndroidLocationClient.kt
|-- LocationPermissionResult.java
|-- LocationStatus.java
`-- LocationUiState.java
```

职责：

- 系统定位封装。
- 权限状态判定。
- 定位状态机。
- 定位失败和权限拒绝时的降级信息。

### 8.3 ui/compose

```text
ui/compose/
|-- YunJiApp.kt
|-- WeatherPageScaffold.kt
|-- WeatherUiComponents.kt
|-- WeatherAnimation.kt
|-- WeatherSceneSpec.java
|-- WeatherLightContext.java
|-- WeatherNavigationTarget.java
|-- UriImage.kt
|-- debug/
|-- home/modules/
|-- screens/
`-- theme/
```

职责：

- Compose 根页面和导航。
- 通用天气 UI 组件。
- 天气动效。
- 主题上下文。
- 调试天气覆盖。
- 首页模块定义和渲染。

### 8.4 ui/compose/screens

```text
screens/
|-- HomeScreen.kt
|-- CityScreen.kt
|-- AlertScreen.kt
|-- LifeIndexScreen.kt
|-- MineScreen.kt
|-- PersonalizationScreen.kt
|-- PersonalizationPanel.kt
|-- DesktopWeatherScreen.kt
|-- HomeModuleRenderer.kt
`-- CustomThemeCropActivity.kt
```

职责：

- 所有主要页面均为 Compose 页面。
- `CustomThemeCropActivity.kt` 负责接入裁剪库，完成自定义主题图片裁剪。
- `HomeModuleRenderer.kt` 是模块化首页的统一渲染入口。

### 8.5 ui/compose/home/modules

```text
home/modules/
|-- HomeModuleKeys.kt
|-- HomeModuleDefinition.kt
`-- HomeModuleCatalog.kt
```

职责：

- 定义首页模块 key。
- 定义模块标题、默认显示状态和排序。
- 给主题和设置页提供统一模块清单。

### 8.6 ui/compose/theme

```text
theme/
|-- YunJiTheme.kt
|-- CustomThemeOptions.kt
|-- CustomThemeImageAnalysis.kt
|-- effects/
|-- mixins/
|-- profiles/
`-- skins/
```

子目录职责：

- `skins`：主题基础视觉参数。
- `effects`：天气动效策略。
- `profiles`：个性化页面预览和主题状态。
- `mixins`：主题扩展模块能力。

## 9. notification 通知

```text
notification/
|-- NotificationHelper.java
|-- NotificationCandidateSelector.java
|-- WarningNotificationDispatcher.java
|-- WarningNotifier.java
`-- SystemWarningNotifier.java
```

职责：

- 创建通知渠道。
- 筛选需要通知的预警。
- 去重和权限判断。
- 实际发送系统通知。

## 10. widget 小组件

```text
widget/
|-- WeatherAppWidgetProvider.kt
|-- WeatherWidgetSnapshot.kt
|-- WeatherWidgetSnapshotLoader.kt
|-- WeatherWidgetLayoutMode.java
|-- WeatherWidgetRefreshScheduler.kt
|-- WeatherWidgetEntryPoint.kt
`-- WeatherWidgetBroadcastFinisher.kt
```

职责：

- 接收桌面小组件广播。
- 根据尺寸选择布局模式。
- 从 Room 读取默认城市天气快照。
- 渲染 RemoteViews。
- 调度后台刷新。

## 11. worker 后台任务

```text
worker/
|-- WeatherAlertWorker.java
|-- DailyWeatherWorker.java
|-- WeatherWidgetRefreshWorker.java
`-- WorkerScopeUtils.java
```

职责：

- 预警刷新和通知。
- 每日天气提醒。
- 小组件缓存刷新。
- 后台任务作用域和兼容工具。

## 12. utils 工具规则

```text
utils/
|-- AirQualityUtils.java
|-- DateTimeUtils.java
|-- DefaultCityUtils.java
|-- HomeBlock.java
|-- LocalStorageSummaryUtils.java
|-- LunarCalendarUtils.java
|-- MineCacheStatusUtils.java
|-- PermissionUtils.java
|-- SunriseSunsetProgress.kt
|-- SunProgressState.kt
|-- VisualTheme.java
|-- VisualThemeCatalog.java
|-- VisualThemeUtils.java
|-- WarningListUtils.java
|-- WeatherAdviceUtils.java
|-- WeatherCodeMapper.java
|-- WeatherDisplayUtils.java
|-- WeatherIconUtils.java
`-- WindScaleUtils.java
```

职责：

- 纯规则、格式化、映射和兼容处理。
- 不直接持有 Android 页面状态。
- 大部分适合 JVM 单测覆盖。

## 13. 资源目录

```text
app/src/main/res/
|-- drawable/                    图标、背景、widget drawable
|-- mipmap-*/                    Launcher 图标
|-- values/                      strings、colors、themes 等资源
|-- xml/                         widget provider、备份等 XML
`-- layout/                      RemoteViews widget 布局
```

说明：

- 主 UI 已迁移到 Compose，但桌面小组件仍必须使用 RemoteViews XML 布局。
- `drawable` 中仍保留图标、天气背景、widget 背景等资源，不等同于旧 XML 页面残留。

## 14. 测试目录

```text
app/src/test/java/com/litroenade/yunjiweather/
|-- data/
|-- domain/
|-- notification/
|-- ui/
|-- utils/
|-- widget/
`-- worker/
```

```text
app/src/androidTest/java/com/litroenade/yunjiweather/
|-- YunjiWeatherInstrumentedTest.java
`-- ui/compose/YunJiThemeComposeTest.kt
```

测试重点：

- Repository fallback。
- UseCase 业务流程。
- 本地规则工具类。
- 定位权限判断。
- 小组件快照加载。
- 主题 key 和基础 Compose 渲染。

## 15. 常见修改入口

| 需求 | 优先查看 |
| --- | --- |
| 改首页卡片显示 | `ui/compose/screens/HomeModuleRenderer.kt`、`ui/compose/home/modules` |
| 改首页数据加载 | `LoadHomeWeatherPageUseCase.java`、`WeatherRepository.java` |
| 改城市搜索 | `CityViewModel.java`、`CityRepository.kt`、`OpenMeteoCitySearchGateway.java` |
| 改定位体验 | `MainActivity.kt`、`ui/location`、`CityScreen.kt` |
| 改生活建议规则 | `LifeIndexDefaults.kt`、`LifeIndexRepository.java` |
| 改预警逻辑 | `AlertRepository.java`、`WarningStore.kt`、`WeatherAlertWorker.java` |
| 改小组件 | `widget` 包、`WeatherWidgetRefreshWorker.java` |
| 改主题 | `ui/compose/theme`、`assets/themes`、`VisualThemeCatalog.java` |
| 改设置持久化 | `SettingsRepository.java`、`SettingsPreferencesDataSource.java` |
| 改后台任务 | `worker` 包、`MainActivity.kt` 中的调度入口 |
