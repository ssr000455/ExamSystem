# ExamSystem 使用说明

## 系统要求
- Java 8 或更高版本
- 支持 OpenGL 的图形环境（Linux 需安装 mesa-utils）

## 启动方法
./start-exam.sh
或
java -jar ExamSystem.jar

## 功能简介
- 选择学习计划（8个系统化模块）
- 多种题型：编程、选择、填空、匹配、调试、补全
- 自动保存进度
- 成就系统与星星奖励
- 多语言界面（7种语言可切换）
- 内置代码编辑器（支持语法高亮、自动补全）

## 首次使用
1. 运行后进入欢迎界面
2. 点击“导入计划”导入官方计划包（或跳过直接选择已有计划）
3. 选择难度（入门/普通/专业）
4. 开始答题

## 快捷键
- Ctrl+S：保存进度
- Ctrl+R：运行代码检查
- Ctrl+←/→：上一题/下一题
- Esc：返回章节选择

## 配置文件
进度数据保存在 .exam_progress.json、.exam_unlocks.json、.exam_achievements.json。

## 故障排除
- 若出现“缺少 native 库”，请确保系统已安装 OpenGL 驱动。
- 若无法启动，请检查 Java 版本：java -version
