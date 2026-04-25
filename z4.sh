#!/data/data/com.termux/files/usr/bin/bash

# 输出目录和文件名
OUT_DIR="/storage/emulated/0/Download"
mkdir -p "$OUT_DIR" 2>/dev/null
DATE=$(date +'%Y-%m-%d')
RAND_SUFFIX=$(printf "%04d" $((RANDOM % 10000)))
OUT_FILE="${OUT_DIR}/${DATE}-${RAND_SUFFIX}.txt"

err=$(mktemp)
p=$(pwd)

exec > "$OUT_FILE"

# 项目信息
cat <<DOC
请基于以下项目完整结构与文件内容分析，直接给出解决方案代码，不提问、不要求额外文件、保持原有包名与项目结构、仅修改问题部分不重构，工作环境为Android Termux终端。
====================================
项目路径：$p
生成时间：$(date +'%Y-%m-%d %H:%M:%S')
====================================
完整项目树形结构（包含所有文件）
====================================
DOC

# 树形结构：所有文件（排除编译/版本控制目录）
find . -type f \
    ! -path "*/build/*" \
    ! -path "*/.git/*" \
    ! -path "*/node_modules/*" \
    ! -path "*/__pycache__/*" \
    -print | sort | sed -e 's/^\.\///' -e 's/[^/]*\//│   /g' -e 's/│   \([^│]*\)$/├── \1/' -e '$s/├──/└──/' 2>>"$err"

cat <<DOC

====================================
项目源代码文件内容
====================================
DOC

c=0

# 方案二：进程替换，避免子 shell 导致计数丢失
while IFS= read -r -d '' fpath; do
    if [ -f "$fpath" ] && [ -s "$fpath" ]; then
        c=$((c+1))
        echo -e "\n============================================================"
        echo "文件路径：$fpath"
        echo "============================================================"
        cat "$fpath" 2>>"$err"
        echo -e "\n------------------------------------------------------------"
    fi
done < <(find . -type f \( \
    -name "*.py" -o -name "*.java" -o -name "*.kt" -o -name "*.gradle" -o -name "*.gradle.kts" \
    -o -name "*.xml" -o -name "*.json" -o -name "*.properties" -o -name "*.txt" -o -name "*.md" \
    -o -name "*.mcmeta" -o -name "*.yml" -o -name "*.yaml" -o -name "*.cfg" \
\) \
    ! -path "*/build/*" \
    ! -path "*/.git/*" \
    ! -path "*/node_modules/*" \
    ! -path "*/__pycache__/*" \
    -print0)

# 统计与错误
echo -e "\n============================================================"
echo "有效源代码文件数：$c"
echo "生成时间：$(date +'%Y-%m-%d %H:%M:%S')"
echo "============================================================"
echo "错误信息："
if [ -s "$err" ]; then
    cat "$err"
else
    echo "无"
fi
echo "============================================================"

rm -f "$err"
exec > /dev/tty
echo "生成完成"
echo "输出文件：$OUT_FILE"
echo "有效源代码文件数：$c"
