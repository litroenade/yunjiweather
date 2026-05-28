# 云迹天气项目目录说明

本文档记录当前 Compose 重构后的主目录职责。QWeather 仍作为可选增强能力保留；默认天气、空气质量和城市搜索可走 Open-Meteo。

## 源码目录

```text
app/src/main/java/com/litroenade/yunjiweather/
├── common/          # 跨层 UI 状态等小型共享类型
├── data/
│   ├── api/         # Retrofit 服务、Open-Meteo/QWeather 网关、城市查询网关
│   ├── api/model/   # 外部接口 DTO，只表达 provider payload
│   ├── entity/      # Room 表实体：城市、天气缓存、预警
│   ├── local/       # AppDatabase、DAO、本地缓存 gateway
│   ├── model/       # App 内部天气、预报、生活指数、洞察模型
│   └── repository/  # 远程、本地缓存、本地 fallback 的业务策略
├── notification/    # 系统通知 channel 和通知内容构建
├── settings/        # SharedPreferences 设置与兼容迁移
├── ui/
│   ├── alert/       # 预警页 ViewModel
│   ├── city/        # 城市管理 ViewModel
│   ├── compose/     # Compose shell、导航、通用组件、动画、主题
│   │   └── theme/skins/ # 默认、全景天气、幻想乡空位、自定义空位的运行时皮肤规格
│   ├── home/        # 首页 ViewModel
│   ├── index/       # 生活指数 ViewModel
│   ├── mine/        # 我的/设置页 ViewModel
│   └── splash/      # Compose 启动页
├── utils/           # 纯工具：时间、单位、天气映射、主题目录、太阳轨迹等
├── widget/          # 桌面天气小组件和本地快照加载
└── worker/          # WorkManager 后台预警、每日提醒和周期任务
```

## 资源目录

```text
app/src/main/res/
├── drawable/        # 应用图标、天气图标、通知图标、小组件背景
├── layout/          # 仅保留 widget_weather.xml；桌面小组件必须使用 RemoteViews XML
├── values/          # 字符串、颜色、基础主题
├── values-night/    # 夜间颜色/主题覆盖
├── values-v27/      # API 27+ 主题覆盖
├── values-night-v27/# API 27+ 夜间主题覆盖
└── xml/             # 备份规则、数据提取规则、小组件 provider 配置
```

```text
app/src/main/assets/themes/
├── official/        # 默认主题 manifest，后续默认预览/分层素材放这里
├── panorama/        # 全景天气 manifest，后续沉浸式天空/雨雪/风场素材放这里
├── fantasy/         # 幻想乡预留位
└── custom/          # 自建主题预留位
```

## 已清理目录

以下目录为空且不再承载功能，已删除：

- `app/src/main/java/com/litroenade/yunjiweather/auth`
- `app/src/main/java/com/litroenade/yunjiweather/ui/auth`
- `app/src/main/java/com/litroenade/yunjiweather/ui/calendar`
- `app/src/main/java/com/litroenade/yunjiweather/ui/dashboard`
- `app/src/main/java/com/litroenade/yunjiweather/ui/notifications`
- `app/src/main/res/color`
- `app/src/main/res/drawable-night`
- `app/src/main/res/drawable-nodpi`
- `app/src/main/res/menu`
- `app/src/main/res/navigation`

## 保留原因

- `layout/widget_weather.xml` 保留：Android 桌面小组件使用 `RemoteViews`，不能直接用 Compose 替代。
- `xml/weather_app_widget_info.xml` 保留：Manifest receiver metadata 引用。
- `xml/backup_rules.xml`、`xml/data_extraction_rules.xml` 保留：Manifest 直接引用。
- QWeather 相关 API/DTO/Repository 保留：当前需求是暂时保留可选增强能力，不删除收费 API 接入。
