# 云迹天气 YunJi Weather

云迹天气是一款原生 Android 天气应用。当前版本已经从旧的 Java + XML/ViewBinding + Fragment 结构，重构为以 Kotlin + Jetpack Compose 为主的现代化单机天气应用。

项目重点不是接入完整商业天气平台，而是围绕课程大作业和答辩场景，展示一个天气 App 从传统 Android 架构到 Compose、Hilt、UseCase、Room、WorkManager、小组件和主题系统的完整重构过程。

## 功能概览

- 沉浸式天气首页：当前天气、逐小时预报、多日预报、空气质量、风力、日出日落、生活建议等模块集中在一个可滚动首页。
- 多城市管理：支持搜索城市、添加城市、设置默认城市、删除城市、调整城市顺序。
- 首页多城市切换：在首页左右滑动即可轮转城市，优先展示本地缓存，避免切换时长时间白屏。
- 下拉刷新：手动触发当前城市天气同步；缓存过期时先显示旧数据并提示后台同步。
- 定位降级：支持系统定位入口；用户拒绝权限时降级到手动搜索城市。
- 生活建议：不伪装成权威付费指数，基于本地天气缓存生成穿衣、出行、运动、洗车、紫外线、空气等建议。
- 天气预警：保留本地预警缓存和通知能力；没有可靠数据源时不伪造预警。
- 主题系统：支持默认主题、全景天气主题、自定义底图主题，幻想乡主题作为预留位。
- 自定义主题：支持按天气场景上传静态底图、裁剪、预览和应用；动态雨雪、光影、昼夜变化由主题引擎叠加。
- 桌面小组件：提供标准、紧凑、详细三种 AppWidget 样式，读取本地天气缓存并由后台任务刷新。
- 后台任务：使用 WorkManager 调度天气预警、每日提醒和小组件缓存刷新。

## 技术栈

| 分类 | 当前实现 |
| --- | --- |
| 主语言 | Kotlin + Java 混合 |
| UI | Jetpack Compose, Material3 |
| Activity | ComponentActivity + setContent |
| 状态绑定 | ViewModel + LiveData + observeAsState |
| 依赖注入 | Hilt |
| 数据库 | Room 2.8.4 |
| 网络 | Retrofit 3.0.0 + Gson |
| 后台任务 | WorkManager 2.11.2 |
| 小组件 | Android AppWidget + RemoteViews |
| 图片裁剪 | com.vanniktech:android-image-cropper |
| 动态取色 | AndroidX Palette |
| 文档 | Dokka 2.0.0 + Markdown |
| 构建 | AGP 8.10.0-alpha05, Gradle 8.11.1 |
| SDK | compileSdk 35, targetSdk 35, minSdk 23 |
| JDK | 17 |

说明：项目当前没有继续升级 AGP，是为了兼容现有 IDEA/Android 开发环境。Activity Compose 也固定在 1.10.1，避免引入需要 compileSdk 36 的依赖链。

## 项目结构

```text
yunjiweather/
|-- app/                         Android 应用模块
|   |-- build.gradle             App 模块构建脚本
|   `-- src/
|       |-- main/                主源码、资源、Manifest
|       |-- test/                JVM 单元测试
|       `-- androidTest/         Android/Compose UI 测试
|-- docs/                        项目文档、答辩文档、包说明
|-- gradle/wrapper/              Gradle Wrapper
|-- artifacts/                   本地 QA 产物目录
|-- build.gradle                 根构建脚本
|-- settings.gradle              Gradle 工程配置
|-- gradle.properties            Gradle 参数
|-- gradlew / gradlew.bat        Wrapper 启动脚本
`-- README.md                    当前说明文档
```

主包结构：

```text
com.litroenade.yunjiweather/
|-- MainActivity.kt              Compose 主入口
|-- YunJiWeatherApplication.java Hilt Application 入口
|-- common/                      通用状态模型
|-- data/                        API、Room、Repository、Model
|-- di/                          Hilt 依赖注入模块
|-- domain/                      UseCase 业务用例层
|-- notification/                通知封装
|-- ui/                          Compose 页面、ViewModel、定位状态
|-- utils/                       工具类
|-- widget/                      桌面小组件
`-- worker/                      WorkManager 后台任务
```

更详细的目录说明见 [docs/project-structure.md](docs/project-structure.md)。

## 架构说明

当前项目采用接近 Clean Architecture 的分层：

```text
Compose UI / ViewModel
        |
        v
domain/usecase
        |
        v
data/repository
        |
        v
Gateway / Store / DataSource
        |
        v
Retrofit / Room / SharedPreferences / Android System
```

各层职责：

- UI：只处理页面展示、用户交互、动画和权限入口。
- ViewModel：把 UseCase 返回结果转换成页面状态。
- UseCase：组织一个完整业务动作，例如首页加载、生活建议加载、预警刷新。
- Repository：决定数据来自远端、缓存还是本地规则，并处理 fallback 策略。
- Gateway/Store/DataSource：封装 Retrofit、Room、SharedPreferences、系统服务等具体实现。

## 环境要求

### 必需环境

- Windows 10/11、macOS 或 Linux。
- JDK 17。
- Android SDK Platform 35。
- Android SDK Build Tools。
- Android SDK Platform Tools，也就是 adb。
- IntelliJ IDEA 或 Android Studio。
- 可选：Android 模拟器或 Android 6.0 以上真机。

### 推荐版本

- Gradle Wrapper：项目内置 `gradle-8.11.1`，不需要手动安装 Gradle。
- Kotlin：2.1.21，由 Gradle 插件管理。
- Android Gradle Plugin：8.10.0-alpha05。

### 本机环境示例

如果你的本地环境和开发机一致，可以使用：

```powershell
$env:JAVA_HOME='C:\Users\litroenade\Documents\env\java17'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

Android SDK 示例路径：

```text
C:\Users\litroenade\Documents\env\androidsdk
```

实际路径以你自己机器为准。

## 部署流程：IDEA / Android Studio

1. 打开 IDEA 或 Android Studio。
2. 选择 `Open`，打开仓库根目录 `yunjiweather/`。
3. 等待 Gradle Sync 完成。
4. 检查 `local.properties` 是否存在。

如果不存在，手动创建：

```properties
sdk.dir=C\:\\Users\\litroenade\\Documents\\env\\androidsdk
```

注意 Windows 路径中的反斜杠需要转义，也可以让 IDE 自动生成。

5. 在设备管理器中启动模拟器，或连接 Android 真机并开启 USB 调试。
6. 选择运行配置 `app`。
7. 点击 Run。

启动 Activity 是：

```text
com.litroenade.yunjiweather/.ui.splash.SplashActivity
```

## 部署流程：命令行构建 Debug APK

Windows PowerShell：

```powershell
$env:JAVA_HOME='C:\Users\litroenade\Documents\env\java17'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :app:assembleDebug --no-daemon
```

构建产物：

```text
app/build/outputs/apk/debug/app-debug.apk
```

macOS / Linux：

```bash
export JAVA_HOME=/path/to/jdk17
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :app:assembleDebug --no-daemon
```

## 部署流程：安装到真机或模拟器

1. 确认设备连接：

```powershell
C:\Users\litroenade\Documents\env\androidsdk\platform-tools\adb.exe devices
```

2. 安装 Debug APK：

```powershell
C:\Users\litroenade\Documents\env\androidsdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

3. 启动应用：

```powershell
C:\Users\litroenade\Documents\env\androidsdk\platform-tools\adb.exe shell am start -n com.litroenade.yunjiweather/.ui.splash.SplashActivity
```

4. 查看崩溃日志：

```powershell
C:\Users\litroenade\Documents\env\androidsdk\platform-tools\adb.exe logcat -d -t 300 | Select-String -Pattern 'FATAL EXCEPTION|AndroidRuntime|com.litroenade.yunjiweather'
```

## Release 打包说明

当前仓库没有提交正式签名配置。课程演示建议使用 Debug APK。

如果需要生成未签名 Release APK：

```powershell
.\gradlew.bat :app:assembleRelease --no-daemon
```

产物通常位于：

```text
app/build/outputs/apk/release/
```

如果要发布到真实设备或应用商店，需要自行增加签名配置：

```gradle
android {
    signingConfigs {
        release {
            storeFile file("your-release-key.jks")
            storePassword "******"
            keyAlias "******"
            keyPassword "******"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
        }
    }
}
```

不要把真实 keystore 和密码提交到仓库。

## 测试与验收

### 编译主源码

```powershell
.\gradlew.bat :app:compileDebugKotlin :app:compileDebugJavaWithJavac --no-daemon
```

### 编译 AndroidTest

```powershell
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --no-daemon
```

### 运行 JVM 单元测试

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon
```

### 运行 Android Lint

```powershell
.\gradlew.bat :app:lintDebug --no-daemon
```

Lint 报告位置：

```text
app/build/reports/lint-results-debug.html
```

### 构建 Debug APK

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

### 真机 / 模拟器测试

连接设备后运行：

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --no-daemon
```

如果没有连接设备，该命令会失败或跳过。答辩前建议至少做一次真机启动、首页刷新、城市切换、个性化主题、小组件添加和定位权限拒绝流程。

## 常用开发命令

完整本地验证建议：

```powershell
$env:JAVA_HOME='C:\Users\litroenade\Documents\env\java17'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :app:compileDebugKotlin :app:compileDebugJavaWithJavac --no-daemon
.\gradlew.bat :app:testDebugUnitTest --no-daemon
.\gradlew.bat :app:lintDebug --no-daemon
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon
git diff --check
```

生成 Dokka 文档：

```powershell
.\gradlew.bat :app:dokkaGeneratePublicationHtml --no-daemon
```

## 数据与权限说明

应用权限：

- `INTERNET`：请求天气、城市搜索、空气质量数据。
- `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION`：定位当前城市。
- `POST_NOTIFICATIONS`：Android 13+ 通知权限。
- `RECEIVE_BOOT_COMPLETED`：重启后恢复小组件和后台刷新调度。

本地数据：

- Room 数据库：`yunji_weather.db`
- Schema version：5
- 主要表：`city`、`weather_cache`、`warning`
- SharedPreferences：设置项、主题配置、模块顺序、自定义底图等。

当前版本允许 destructive migration，用于清理旧登录和 ownerUserId 维度带来的旧表结构。已有旧版本本地数据可能被清空。

## 主题与个性化

主题资源目录：

```text
app/src/main/assets/themes/
|-- official/manifest.json
|-- panorama/manifest.json
|-- fantasy/manifest.json
`-- custom/manifest.json
```

主题运行时代码：

```text
app/src/main/java/com/litroenade/yunjiweather/ui/compose/theme/
|-- skins/                       主题皮肤
|-- effects/                     天气动效
|-- mixins/                      模块扩展能力
|-- CustomThemeOptions.kt
`-- YunJiTheme.kt
```

自定义主题的设计边界：

- 用户裁剪的是静态底图，不是动态天气。
- 雨雪、光影、星空、昼夜变化由 `ThemeWeatherEffect` 叠加。
- 全景天气主题按天气和时间选择白天、雨天、雪天、夜间素材。

## 小组件说明

小组件代码：

```text
app/src/main/java/com/litroenade/yunjiweather/widget/
```

小组件布局：

```text
app/src/main/res/layout/
|-- widget_weather.xml
|-- widget_weather_compact.xml
`-- widget_weather_expanded.xml
```

小组件 Provider 配置：

```text
app/src/main/res/xml/
|-- weather_app_widget_info.xml
|-- weather_app_widget_compact_info.xml
`-- weather_app_widget_expanded_info.xml
```

刷新策略：

- 小组件渲染时读取本地缓存快照。
- 后台 WorkManager 周期任务刷新默认城市天气缓存。
- 应用首页刷新成功后主动刷新已安装小组件。
- 点击小组件进入应用。

## 常见问题

### 1. Gradle Sync 失败

检查：

- JDK 是否为 17。
- `local.properties` 中 `sdk.dir` 是否正确。
- Android SDK 是否安装 Platform 35。
- 网络是否可以访问 `google()` 和 `mavenCentral()`。

### 2. Activity Compose 版本为什么不是最新

当前项目保持 compileSdk 35 和 AGP 8.10.0-alpha05，以兼容现有 IDEA 环境。部分新版本 AndroidX 依赖会要求 compileSdk 36，因此没有盲目升级。

### 3. 首次启动没有天气数据

可能原因：

- 网络不可用。
- Open-Meteo 接口暂时不可访问。
- 默认城市缓存为空。

处理方式：

- 进入城市管理添加城市。
- 下拉刷新首页。
- 检查 logcat 中网络错误。

### 4. 定位失败

可能原因：

- 未授予定位权限。
- 模拟器没有设置定位。
- 真机关闭系统定位服务。

应用会降级到手动搜索城市。

### 5. 小组件不更新

检查：

- 是否已至少打开过一次应用。
- 是否有默认城市。
- 是否有天气缓存。
- WorkManager 是否被系统电池策略限制。

可以手动打开 App 并刷新首页，刷新成功后小组件会同步更新。

### 6. 根目录的 `index.html`、`script.js`、`styles.css` 是什么

这些通常是 IDE inspection 或本地报告导出的文件，不属于 Android 应用源码。真正的项目源码在 `app/` 下，文档在 `docs/` 下。

## 文档索引

- [开发文档](docs/development-guide.md)
- [项目结构](docs/project-structure.md)
- [答辩提纲](docs/defense-guide.md)
- [包说明 / Dokka include](docs/packages.md)
- [生成的周记与 AI 协作记录](docs/generated/)

## 当前边界与后续计划

已完成：

- 登录模块移除。
- 主 UI Compose 化。
- 多城市管理与首页横滑切换。
- 本地缓存优先策略。
- 生活建议本地化。
- 主题/个性化系统。
- 全景天气主题和动态天气效果。
- 小组件三种预设样式。
- Hilt、UseCase、WorkManager、Room 数据链路整理。

仍建议继续完善：

- 真机长时间动态效果性能和耗电测试。
- 更完整的官方预警数据源。
- 自定义主题编辑体验继续打磨，例如更多裁剪比例、预览设备框、导入失败恢复。
- 数据层进一步 Kotlin 化。
- Release 签名和发布流水线。

## License

本项目包含 `LICENSE` 文件，具体授权以仓库中的 [LICENSE](LICENSE) 为准。
