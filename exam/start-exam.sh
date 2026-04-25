#!/bin/bash
cd "$(dirname "$0")"
java -Xmx512m -cp "ExamSystem.jar:lib/*" -Djava.library.path=lib Main "$@"
