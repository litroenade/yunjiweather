# YunJi Weather

YunJi Weather 是一个基于 Java + XML Views 的 Android 天气 / 生活服务课程项目。应用不是单纯的 API Demo，而是包含登录注册、本地账户隔离、天气查询、城市管理、生活指数、预警通知、缓存回退和设置页的完整移动端应用。

项目默认使用 Open-Meteo 作为无 API Key 天气数据源；配置 QWeather 后，会增强生活指数和官方天气预警能力。未配置 QWeather 时，应用不会伪造官方预警，只会显示本地缓存或明确提示未配置数据源。

## 技术栈

- Java
- XML Views
- Material Components
- BottomNavigationView
- ViewModel + LiveData
- Retrofit + Gson
- Room
- SharedPreferences
- WorkManager
- JUnit4
- Android Gradle Plugin `8.10.0-alpha05`
- Gradle Wrapper `8.11.1`

## 功能总览

- 登录 / 注册：本地账户，用户名归一化，密码使用 PBKDF2WithHmacSHA256 加盐哈希。
- 账户隔离：不同账户的城市、天气缓存、生活指数缓存、预警状态和设置互相隔离。
- 首页天气：实时天气、小时预报、未来预报、空气质量、穿衣建议、出行建议、天气背景和轻量动画。
- 城市管理：搜索城市、添加城市、删除城市、设置默认城市、防重复添加、城市天气摘要。
- 生活指数：穿衣、出行、运动、洗车、紫外线、感冒、空气、舒适度、晾晒、旅游等指数。
- 天气预警：显示默认城市预警、预警详情、已读状态、通知去重。
- 我的页面：账户信息、默认城市、数据更新时间、单位设置、通知开关、动画开关、深色模式、清理缓存、帮助和关于。
- 后台任务：天气预警检查和每日天气提醒。

## 登录注册与账户隔离

应用启动后先进入 `AuthActivity`。已有登录会话时会直接进入 `MainActivity`；没有会话时需要登录或注册。

账户系统是本地账户方案，不连接真实后端，不上传密码，不使用第三方认证 SDK。密码不会明文保存，数据库中只保存：

- `passwordHash`
- `passwordSalt`

用户数据隔离范围：

- `city.ownerUserId`
- `weather_cache.ownerUserId`
- `warning.ownerUserId`
- `yunji_weather_settings_user_<userId>`

退出登录只清除当前 session，不删除本地账户数据。

## 页面结构

登录注册页：

- `AuthActivity`
- `activity_auth.xml`

主界面采用 5 个底部导航页面：

- 首页：`HomeFragment`
- 城市：`CityFragment`
- 指数：`LifeIndexFragment`
- 预警：`AlertFragment`
- 我的：`MineFragment`

`MainActivity` 只负责登录检查、底部导航初始化、通知 Channel 创建和 Worker 调度。

## 本地数据库设计

Room 数据库版本：`3`。当前使用 `fallbackToDestructiveMigration()`，课程项目开发期接受本地数据重建。

主要表：

- `user`：本地账户。
- `city`：账户隔离的城市列表和默认城市。
- `weather_cache`：账户隔离的天气 / 生活指数缓存。
- `warning`：账户隔离的预警记录、已读状态和已通知状态。

核心唯一约束：

- `user.username`
- `city.ownerUserId + city.locationId`
- `weather_cache.ownerUserId + weather_cache.locationId + weather_cache.weatherType`
- `warning.ownerUserId + warning.warningId`

`weather_cache.weatherType` 主要取值：

- `HOME`
- `INDEX`
- `NOW`
- `DAILY`
- `HOURLY`
- `AIR`
- `WARNING`

## API 数据源

默认数据源：

- Open-Meteo 天气预报
- Open-Meteo 空气质量
- Open-Meteo 城市搜索

可选增强：

- QWeather 生活指数
- QWeather 官方天气预警
- QWeather 城市查询增强

## QWeather 配置方式

默认不需要配置 API Key。需要启用 QWeather 增强能力时，在项目根目录的 `local.properties` 中加入：

```properties
qweather.apiHost=你的 QWeather API Host
qweather.apiKey=你的 QWeather API Key
```

`local.properties` 不应提交到 Git。

## IntelliJ IDEA 运行方式

1. 用 IntelliJ IDEA 打开项目根目录。
2. 等待 Gradle Sync 完成。
3. 在 IntelliJ IDEA 的 Gradle 设置中，将 Gradle JVM 设为 JDK 17 或 JDK 21。
4. 选择 `app` 运行配置，运行到模拟器或真机。
5. 在 Build Output 查看编译结果。
6. 在 Logcat 查看运行日志。

不要把当前命令行默认 JDK 25 作为主要验证环境。AGP `8.10.0-alpha05` 在 JDK 25 下运行完整 `test` 任务可能出现 `Type T not present`。

## 验证命令

推荐在 IntelliJ IDEA 的 Gradle 工具窗口运行：

```bash
gradle test
gradle lintDebug
gradle assembleDebug
```

也可以使用项目自带 Wrapper：

```powershell
.\gradlew.bat compileDebugUnitTestJavaWithJavac
.\gradlew.bat compileDebugAndroidTestJavaWithJavac
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

有设备或模拟器，并且 `adb` 可用时再运行：

```bash
gradle connectedAndroidTest
```

## 当前环境限制

当前命令行环境使用 JDK 25。该环境下完整 `gradle test` 可能受 AGP alpha 兼容性影响，出现 `Type T not present`。请在 IntelliJ IDEA 中使用 JDK 17 或 JDK 21 运行完整单元测试。

## APK 输出位置

Debug APK 通常位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release APK 通常位于：

```text
app/build/outputs/apk/release/app-release.apk
```

## 课程展示建议

建议展示流程：

1. 注册两个本地账户，演示不同账户城市和设置互不影响。
2. 登录账户 A，添加城市并切换默认城市。
3. 返回首页，展示天气、小时预报、空气质量、穿衣和出行建议。
4. 进入生活指数页，说明 QWeather 成功、缓存数据、本地建议三种状态。
5. 进入预警页，说明官方预警只来自 QWeather，未配置时不会伪造预警。
6. 进入我的页，展示账户信息、单位切换、动画开关、通知开关和清理缓存。
7. 退出登录后切换账户 B，展示数据隔离效果。

## License

本项目使用 MIT License，详见 [LICENSE](LICENSE)。
