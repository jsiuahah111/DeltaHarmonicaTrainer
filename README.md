# DeltaHarmonicaTrainer

三角洲口琴练习器（Android 版）

## 功能
- 竖屏深色 UI
- 8+4 口琴面板
- 允许双指缩放/拖动按键面板
- 本地 Room 数据库存储乐谱
- 粘贴 JSON 乐谱导入
- BPM 调节
- 开始预览 / 暂停 / 重播
- 全离线，不联网，不申请无障碍权限

## 运行要求
- Android Studio Ladybug 或更新版本
- JDK 17
- Android SDK 34

## 构建
```bash
./gradlew assembleDebug
```

生成 APK：
```text
app/build/outputs/apk/debug/app-debug.apk
```

## 说明
- 本应用完全离线运行
- 不尝试操控任何其他 App
- 不申请任何无障碍权限
- 乐谱保存在本地数据库中
