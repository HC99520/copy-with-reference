# Copy With Reference

IntelliJ IDEA 插件：选中代码 → 右键 → 一键复制文件路径 + 行号 + 代码，专为粘贴到 Claude Code / ChatGPT 设计。

## 效果

选中 `UserService.java` 第 42-58 行，右键 **Copy with File Reference**，剪贴板内容：

    @src/main/java/com/example/UserService.java:42-58
    ```java
    public User findById(Long id) {
        return repository.findById(id).orElseThrow(...);
    }
    ```

## 安装

下载 [最新 Release](https://github.com/HC99520/copy-with-reference/releases) 的 `.zip`，IntelliJ IDEA → `Settings` → `Plugins` → ⚙️ → `Install Plugin from Disk...`

## 使用

| 操作 | 效果 |
|------|------|
| 选中代码 → 右键 → Copy with File Reference | 复制选中代码 + 路径 + 行号范围 |
| 不选中 → 右键 → Copy with File Reference | 复制当前行 + 路径 + 行号 |
| 快捷键 `Ctrl+Shift+Alt+C`（可自定义） | 同上 |

## 开发

```bash
# 需要 JDK 17+
export JAVA_HOME=/path/to/jdk-17
./gradlew buildPlugin
```

产物在 `build/distributions/copy-with-reference-1.0.0.zip`

## 项目结构

```
src/main/
├── java/com/copywithreference/
│   └── CopyWithReferenceAction.java   # 唯一的代码：右键动作
└── resources/META-INF/
    └── plugin.xml                     # 插件注册（挂载到 EditorPopupMenu）
```

## 许可

MIT
