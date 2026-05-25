# 云迹天气 YunJi Weather

云迹天气是一个原生 Android 天气应用。当前主线已经从 Java + XML/Fragment/ViewBinding 重构为 Kotlin + Jetpack Compose UI，数据层仍以 Java 为主，保留 Repository / Gateway / Room / WorkManager 的分层。

## 当前状态

- 登录、本地账号、session gate、`UserEntity`、`ownerUserId` 用户维度已经移除。
- 主界面不再是底部五 tab。现在是单页天气首页，城市管理、搜索入口、预警、生活指数、设置入口放在顶部城市区、更多菜单和首页服务卡片里。
- 首页信息结构参考华为天气：当前城市天气、动态天气背景、逐小时预报、多日预报、空气质量、天气指数、生活建议和预警入口集中在一条纵向滚动页面。
- 城市管理、搜索、天气预警、生活指数和设置页以 `ModalBottomSheet` 复用原 Compose Screen；切换默认城市后关闭城市弹层会刷新首页天气。
- 预警刷新已拆到 repository/gateway/store/mapper，页面刷新不再直接发送系统通知；通知副作用交给 Worker/Notifier。
- Room 数据库版本为 v5，只保留 `city`、`weather_cache`、`warning` 三类业务表，允许 destructive rebuild。

参考方向：
- 华为天气官方服务介绍：`https://consumer.huawei.com/cn/mobileservices/weather/`
- 华为天气“管理城市”说明：`https://consumer.huawei.com/cn/support/content/zh-cn16010058/`
- 华为天气添加/删除城市说明：`https://consumer.huawei.com/cn/support/content/zh-cn15951820/`

## 技术栈

| 项 | 当前选择 |
| --- | --- |
| UI | Kotlin + Jetpack Compose + Material3 |
| Activity | `ComponentActivity` + `setContent` |
| 状态 | Java ViewModel + LiveData，Compose 使用 `observeAsState` |
| 数据层 | Java Repository + Gateway + Room DAO |
| 数据库 | Room 2.8.4，schema v5 |
| 网络 | Retrofit 3 + Gson Converter |
| 后台任务 | WorkManager 2.11.2 |
| 通知 | NotificationChannel + NotificationCompat |
| 构建 | AGP 8.10.0-alpha05，Gradle 8.11.1 |
| SDK | compileSdk 35，targetSdk 35，minSdk 23 |
| Kotlin | 2.1.21，JVM target 11 |

AGP 没有继续升级到 8.11，是为了兼容当前 IDEA 开发环境；因此 Activity Compose 固定在 `1.10.1`，避免引入要求 `compileSdk 36` 的链路。Kotlin 固定在 `2.1.21`，用于避开 AGP 8.10.0-alpha05 内置 R8 对 Kotlin 2.2/2.3 metadata 的 D8 警告。

## 目录结构

```text
app/src/main/java/com/litroenade/yunjiweather
├── MainActivity.kt              Compose 主入口
├── common/                      UiState 等通用状态
├── data/
│   ├── api/                     Open-Meteo、QWeather、城市查询网关
│   ├── entity/                  Room 实体
│   ├── local/                   DAO、RoomDatabase、缓存网关
│   ├── model/                   页面数据模型
│   └── repository/              天气、城市、生活指数、预警仓库
├── notification/                预警通知选择、调度和发送
├── settings/                    本机设置
├── ui/
│   ├── compose/                 Compose 页面、组件、主题、天气动效
│   ├── home/                    HomeViewModel 与天气分类契约
│   ├── city/                    CityViewModel
│   ├── alert/                   AlertViewModel
│   ├── index/                   LifeIndexViewModel
│   ├── mine/                    MineViewModel
│   └── splash/                  Compose 启动页
├── utils/                       天气、日期、权限、单位、主题工具
└── worker/                      预警和每日天气后台任务
```

旧的 `AuthActivity`、Fragment、Adapter、layout/navigation/menu XML 已从主流程删除。

## 数据源

| 数据源 | 用途 |
| --- | --- |
| Open-Meteo | 默认天气源，无需 API Key，用于开箱运行 |
| QWeather | 可选增强源，用于官方预警、城市查询和更完整天气数据 |

在仓库根目录创建或编辑 `local.properties`：

```properties
qweather.apiHost=your-qweather-host
qweather.apiKey=your-qweather-key
```

未配置 QWeather 时，不伪造官方预警；天气首页仍可使用 Open-Meteo 回退。

## 构建与验证

Windows 建议使用 JDK 17：

```powershell
$env:JAVA_HOME='C:\Users\litroenade\Documents\env\java17'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :app:compileDebugKotlin --no-daemon
.\gradlew.bat :app:testDebugUnitTest --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon
```

可选编译仪器测试源码：

```powershell
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --no-daemon
```

Debug APK 输出：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 已知未完成

- 多城市首页左右分页还未实现；当前通过城市管理弹层添加/设置默认城市，首页展示默认城市。
- 首页已恢复天气预警和生活指数入口，但尚未完全做成华为天气那种全量首页模块。
- 分享、反馈当前天气、桌面天气入口已保留说明弹窗，尚未接系统分享面板、反馈通道和小组件能力。
- Java 数据层尚未全量迁移 Kotlin；当前只清理了登录/用户维度并精进了城市查询、城市摘要和预警链路。
- `connectedAndroidTest` 和真机截图验收依赖可用设备/adb，本轮未作为默认构建门槛。
