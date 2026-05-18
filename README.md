# YunJi Weather

YunJi Weather 是一个基于 Java + XML Views 的 Android 天气 / 生活服务 App。项目目标是做成课程项目可展示的完整应用，而不是单纯 API Demo。

项目默认使用 Open-Meteo 提供免 API Key 的天气、空气质量和城市搜索能力；如果配置 QWeather，则会增强生活指数和官方天气预警能力。未配置 QWeather 时，应用不会伪造官方预警，只会显示本地缓存或明确提示未配置数据源。

## 技术栈

- Java
- XML Views
- BottomNavigationView
- ViewModel + LiveData
- Retrofit + Gson
- Room
- WorkManager
- Material Components
- JUnit4
- Android Gradle Plugin `8.10.0-alpha05`
- Gradle Wrapper `8.11.1`

## 页面结构

应用包含 5 个底部导航页面：

- 天气：首页天气、小时预报、未来预报、空气质量、穿衣建议、出行建议、缓存状态、天气动画。
- 城市：城市搜索、热门城市、添加城市、删除城市、切换默认城市、城市天气摘要。
- 指数：穿衣、出行、运动、洗车、紫外线、感冒、空气、舒适度、晾晒、旅游等生活指数。
- 预警：默认城市天气预警、本地预警缓存、预警详情、通知去重。
- 我的：默认城市、单位设置、通知开关、动画开关、深色模式、数据更新时间、清理缓存、帮助、数据源说明。

## 已实现功能

### 天气首页

- 实时天气展示
- 小时天气展示
- 未来天气展示
- 空气质量展示
- 湿度、风向、风力、气压、能见度等详情
- 穿衣建议和出行建议
- 下拉刷新和按钮刷新
- 定位按钮，用户主动点击后才申请定位权限
- 天气图标和背景切换
- 晴、云、雨、雪轻量动画
- 加载、成功、错误、空数据、缓存状态
- 网络失败时回退 Room 本地缓存

### 城市管理

- 添加城市
- 删除城市
- 设置默认城市
- 防止重复添加城市
- 热门城市快捷添加
- Open-Meteo 免 Key 城市搜索
- QWeather 可选城市搜索
- 城市列表展示天气摘要
- 默认城市统一保存在 Room 中，首页、指数、预警、我的页共享同一默认城市

### 生活指数

- QWeather 生活指数优先
- QWeather 成功后写入 `WeatherCacheEntity`，`weatherType = INDEX`
- QWeather 失败后读取生活指数缓存
- 无 QWeather 或无缓存时显示本地生活建议
- 页面文案区分实时数据、缓存数据、本地建议，避免误导用户
- 远端数据不足 10 项时使用本地建议补齐展示项

### 天气预警

- QWeather 作为唯一官方预警来源
- 未配置 QWeather 时明确提示未配置数据源
- 有本地缓存时显示本地预警缓存
- 无缓存时显示“暂无本地预警缓存”
- 支持预警详情弹窗
- 支持标记已读
- 支持通知去重，避免同一 warningId 反复通知
- 预警通知开关保存在 SharedPreferences

### 我的 / 设置

- 默认城市展示
- 数据更新时间展示
- 摄氏度 / 华氏度切换
- 风力等级 / 米每秒切换
- 天气动画开关
- 深色模式开关
- 天气预警通知开关
- 每日天气提醒开关
- 清理天气缓存
- 数据源说明 Dialog
- 使用帮助 Dialog
- 关于 App Dialog

### 后台与通知

- WeatherAlertWorker 周期检查预警
- DailyWeatherWorker 周期生成每日天气提醒
- Android 13+ 通知权限适配
- NotificationChannel 创建
- 通知发送前检查权限

## 本地存储设计

Room 数据库主要包含：

- `city`：城市列表、默认城市、经纬度、排序信息
- `weather_cache`：首页天气、小时预报、未来预报、空气质量、生活指数、预警等缓存 JSON
- `warning`：预警记录、已读状态、已通知状态

`weather_cache.weatherType` 主要取值：

- `HOME`
- `INDEX`
- `NOW`
- `DAILY`
- `HOURLY`
- `AIR`
- `WARNING`

当前没有修改数据库表结构即可完成生活指数缓存和数据更新时间展示。

## API 配置

默认不需要配置 API Key。未配置 QWeather 时，应用使用 Open-Meteo 获取：

- 天气预报
- 空气质量
- 城市搜索

如果需要启用 QWeather 增强能力，在项目根目录的 `local.properties` 中加入：

```properties
qweather.apiHost=你的 QWeather API Host
qweather.apiKey=你的 QWeather API Key
```

`local.properties` 已加入 `.gitignore`，不要提交真实 API Key。

## IntelliJ IDEA 运行方式

1. 使用 IntelliJ IDEA 打开项目根目录。
2. 等待 Gradle Sync 完成。
3. 在 IntelliJ IDEA 的 Gradle 设置中，把 Gradle JVM 设置为 JDK 17 或 JDK 21。
4. 选择 `app` 运行配置，运行到模拟器或真机。
5. 在 Build Output 查看编译结果。
6. 在 Logcat 查看运行时日志。

不要把当前命令行默认 JDK 25 作为本项目的主要测试环境。AGP `8.10.0-alpha05` 在 JDK 25 下创建 `test` 任务会出现 `Type T not present`。

## 命令行验证

推荐在 IntelliJ IDEA 的 Gradle 工具窗口运行：

```bash
gradle test
gradle lintDebug
gradle assembleDebug
```

也可以使用仓库自带 Wrapper：

```bash
.\gradlew.bat test
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

有设备或模拟器，并且 `adb` 可用时再运行：

```bash
gradle connectedAndroidTest
```

## 当前环境验证结果

当前命令行环境使用 JDK 25，因此完整 `test` 任务受 AGP alpha 兼容性影响。已完成的验证如下：

- `.\gradlew.bat -v`：通过，Gradle Wrapper 为 8.11.1。
- `.\gradlew.bat compileDebugUnitTestJavaWithJavac`：通过。
- `.\gradlew.bat compileDebugAndroidTestJavaWithJavac`：通过。
- `.\gradlew.bat lintDebug`：通过。
- `.\gradlew.bat assembleDebug`：通过。
- 手动 JUnitCore 已运行关键新增测试并通过。
- `.\gradlew.bat test`：当前 JDK 25 环境下失败，错误为 `Type T not present`。请在 IntelliJ IDEA 中使用 JDK 17 或 JDK 21 再运行完整单元测试。

## 测试覆盖

项目已包含 JVM 单元测试，覆盖重点业务逻辑：

- 天气建议生成
- 缓存过期判断
- 城市重复检测
- 默认城市切换
- 默认城市统一工具
- 城市天气摘要
- 生活指数缓存
- 生活指数补齐
- 天气网关选择
- 天气动画类型映射
- 预警列表已读状态
- 预警无 QWeather 文案
- 预警通知去重
- 时间格式化
- AQI 分类
- 风力等级映射
- 温度和风速单位展示

仪器测试覆盖：

- 应用包名检查
- Room 数据库可创建
- MainActivity 可启动

## 课程展示建议

建议演示流程：

1. 打开首页，展示天气、小时预报、未来预报、空气质量、穿衣和出行建议。
2. 进入城市页，添加一个城市，切换默认城市，观察城市天气摘要。
3. 返回首页，确认默认城市变化。
4. 进入指数页，展示生活指数和本地建议 / 缓存状态。
5. 进入预警页，说明 QWeather 是官方预警增强来源，未配置时不伪造预警。
6. 进入我的页，展示单位切换、动画开关、通知开关、数据更新时间和清理缓存。
7. 断网或制造接口失败后展示缓存回退逻辑。

## 许可证

本项目使用 MIT License，详见 [LICENSE](LICENSE)。
