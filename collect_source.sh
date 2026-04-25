#!/bin/bash
OUTPUT_FILE="/storage/emulated/0/Download/ExamSystem_source.txt"
echo "=== ExamSystem 源代码收集 ===" > $OUTPUT_FILE
echo "生成时间: $(date)" >> $OUTPUT_FILE
echo "" >> $OUTPUT_FILE

echo "=== 项目结构 ===" >> $OUTPUT_FILE
find ~/ExamSystem -type f -name "*.java" -o -name "*.json" -o -name "*.sh" -o -name "*.yml" | sort >> $OUTPUT_FILE
echo "" >> $OUTPUT_FILE

echo "=== start.sh ===" >> $OUTPUT_FILE
cat ~/ExamSystem/start.sh >> $OUTPUT_FILE 2>/dev/null
echo "" >> $OUTPUT_FILE

echo "=== questions.json ===" >> $OUTPUT_FILE
cat ~/ExamSystem/questions.json >> $OUTPUT_FILE 2>/dev/null
echo "" >> $OUTPUT_FILE

echo "=== src/ 目录所有 Java 文件 ===" >> $OUTPUT_FILE
for file in $(find ~/ExamSystem/src -name "*.java" | sort); do
    echo "=== $file ===" >> $OUTPUT_FILE
    cat "$file" >> $OUTPUT_FILE
    echo "" >> $OUTPUT_FILE
done

echo "=== questions/ 目录所有 JSON 文件 ===" >> $OUTPUT_FILE
for file in $(find ~/ExamSystem/questions -name "*.json" | sort); do
    echo "=== $file ===" >> $OUTPUT_FILE
    cat "$file" >> $OUTPUT_FILE
    echo "" >> $OUTPUT_FILE
done

echo "=== 收集完成，输出文件: $OUTPUT_FILE ==="
echo "文件大小: $(du -h $OUTPUT_FILE | cut -f1)"
