# ExamSystem 使用說明

## 系統需求
- Java 8 或更高版本
- 支援 OpenGL 的圖形環境（Linux 需安裝 mesa-utils）

## 啟動方式
./start-exam.sh
或
java -jar ExamSystem.jar

## 功能簡介
- 選擇學習計劃（8個系統化模組）
- 多種題型：程式設計、選擇、填空、配對、除錯、補全
- 自動儲存進度
- 成就系統與星星獎勵
- 多語言介面（可切換7種語言）
- 內建程式碼編輯器（語法高亮、自動補全）

## 首次使用
1. 執行後進入歡迎畫面
2. 點擊「匯入計劃」匯入官方計劃包（或跳過直接選擇已有計劃）
3. 選擇難度（入門/普通/專業）
4. 開始答題

## 快捷鍵
- Ctrl+S：儲存進度
- Ctrl+R：執行程式碼檢查
- Ctrl+←/→：上一題/下一題
- Esc：返回章節選擇

## 設定檔
進度資料儲存在 .exam_progress.json、.exam_unlocks.json、.exam_achievements.json。

## 故障排除
- 若缺少 native 函式庫，請確認系統已安裝 OpenGL 驅動程式。
- 若無法啟動，請檢查 Java 版本：java -version
