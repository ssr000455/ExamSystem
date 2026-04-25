#!/bin/bash
cd "$(dirname "$0")"
rm -rf build
mkdir -p build
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d build @sources.txt
cp -r questions models scenes build/ 2>/dev/null
cp -r src/i18n/*.properties build/i18n/ 2>/dev/null
cat > MANIFEST.MF << 'MANIFEST'
Manifest-Version: 1.0
Main-Class: Main
Class-Path: lib/rsyntaxtextarea-3.6.2.jar lib/gson-2.10.1.jar lib/autocomplete-3.3.2.jar lib/flatlaf-3.5.2.jar lib/swingx-all-1.6.5-1.jar lib/gluegen-rt-2.3.2.jar lib/gluegen-rt-2.3.2-natives-linux-amd64.jar lib/jogl-all-2.3.2.jar lib/jogl-all-2.3.2-natives-linux-amd64.jar
MANIFEST
cd build
jar cfm ../ExamSystem.jar ../MANIFEST.MF *
cd ..
rm -f sources.txt MANIFEST.MF
echo "打包完成: ExamSystem.jar"
