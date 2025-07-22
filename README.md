# NoInternet

一个 Minecraft Fabric Mod，可以在游戏启动阶段拦截非白名单的网络请求，避免不必要的更新检查等拖慢启动速度。

> 参考自 [NonUpdate Reloaded](https://modrinth.com/mod/non-update-reloaded)

## 功能

- 🚫 拦截启动阶段的非白名单网络请求
- ⚡ 加快游戏启动速度
- 📋 灵活的白名单配置系统
- 📝 可选的拦截日志记录

## 兼容性

- **Minecraft**: 1.21.4
- **Fabric Loader**: >= 0.16.14
- **Java**: >= 21
- **Fabric API**: 必需

## 配置

### 配置文件位置
`config/nointernet.json`（首次启动自动创建）

### 默认配置
```json
{
  "whitelist": [
    "$emojang.com",
    "$eminecraft.net",
    "$ctoriifind.json",
    "$clynn.json"
  ],
  "logBlockedRequests": true
}
```

> toriifind.json 和 lynn.json 是我的另一个mod需要的，你如果用不到，可以将其删除

### 配置项说明

| 配置项 | 类型 | 说明 |
|--------|------|------|
| `whitelist` | 字符串数组 | 不会拦截的域名列表 |
| `logBlockedRequests` | 布尔值 | 是否记录被拦截的请求（默认：true） |

### 如何添加网址到白名单

支持多种匹配方式，通过前缀指定：

#### 匹配方式

| 前缀 | 说明 | 示例 | 匹配结果 |
|------|------|------|----------|
| **$r** | 正则表达式 | `$r.+\.mojang\.com` | `sessionserver.mojang.com` |
| **$e** | 结尾匹配 | `$emojang.com` | `sessionserver.mojang.com` |
| **$s** | 开头匹配 | `$swww` | `www.mojang.com` |
| **$c** | 包含匹配 | `$cminecraft` | `minecraft.example.com` |
| **无前缀** | 精确匹配 | `hypixel.net` | `hypixel.net` |

### 匹配规则

- 匹配会检查完整的 URL（域名 + 路径 + 参数）
- 游戏完全加载后，所有网络请求都会被允许
- 本地 socket 连接始终允许

## 许可证

本项目采用 [CC0-1.0](LICENSE) 许可证。
