# 蓝色大肥鱼记分器

Android 德州扑克记分 App（鲸鱼娘 DeepSeek / 蓝色大肥鱼主题）。

- 对局记录每一手拿取（−）/ 归还（+）  
- 积分 = 归还 − 拿取（入局拿取 1000 显示 -1000）  
- 结束时统一归还手上剩余积分并统计输赢  
- 对账：多还 / 少还差额一目了然  
- 分享记分表图片（可直达微信）

> UI 字体：英文与数字用 Monaco，中文用华文楷体。  
> 视觉参考：[蓝色大肥鱼档案馆](https://github.com/EDMOK/blue-fish-archive) 的鲸鱼娘形象；App 默认柔和深海暗色主题。

## 预制玩家名单

开局页可直接：

- **新建名单**：输入名称与玩家（每行一个）并保存  
- **导入名单**：选择已有名单一键填入开局列表  
- **删除名单**：选择并删除已有名单  

名单保存在 App 私有目录。若仍有旧版 `assets/players.preset.json`，首次启动会自动迁成「默认名单」。

## 环境要求

- JDK 17
- Android SDK（API 34）
- `ANDROID_HOME` 已配置

## 常用命令

```bash
# Debug 包
./gradlew assembleDebug

# 安装到已连接设备
./gradlew installDebug
```

产物路径：`app/build/outputs/apk/debug/app-debug.apk`
