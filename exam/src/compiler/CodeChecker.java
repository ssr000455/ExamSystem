package compiler;

import java.util.*;
import java.util.regex.*;

public class CodeChecker {
    
    public static class CheckResult {
        public boolean success;
        public double score;
        public int maxScore;
        public List<String> errors = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();
        public Map<String, ItemStatus> checks = new LinkedHashMap<>();
        public ScoreDetail scoreDetail = new ScoreDetail();
        public boolean advanced = false;  // 新增：是否为高级代码
    }
    
    public static class ScoreDetail {
        public double syntaxScore = 0;
        public double keywordScore = 0;
        public double structureScore = 0;
        public double logicScore = 0;
        public double penalty = 0;
    }
    
    public static class ItemStatus {
        public boolean passed;
        public String message;
        public ItemStatus(boolean passed, String message) {
            this.passed = passed;
            this.message = message;
        }
    }
    
    public static CheckResult check(String code, String[] keywords, String template) {
        CheckResult result = new CheckResult();
        result.maxScore = 10;
        result.success = true;
        
        if (isEmptyOrTemplate(code, template)) {
            result.errors.add("请编写代码实现功能，不要只保留模板");
            result.checks.put("代码完成度", new ItemStatus(false, "未编写代码"));
            result.success = false;
            result.score = 0;
            return result;
        }
        
        boolean bracketsOk = checkBrackets(code, result);
        result.checks.put("括号配对", new ItemStatus(bracketsOk, bracketsOk ? "正确" : "有错误"));
        
        boolean quotesOk = checkQuotes(code, result);
        result.checks.put("引号闭合", new ItemStatus(quotesOk, quotesOk ? "正确" : "有错误"));
        
        boolean semicolonOk = checkSemicolons(code, result);
        result.checks.put("语句分号", new ItemStatus(semicolonOk, semicolonOk ? "正确" : "可能缺少分号"));
        
        int keywordFound = 0;
        if (keywords != null && keywords.length > 0) {
            keywordFound = checkKeywords(code, keywords, result);
        } else {
            result.checks.put("关键词检查", new ItemStatus(true, "无关键词要求"));
            keywordFound = checkKeywords(code, keywords, result);
        } else {
            result.checks.put("关键词检查", new ItemStatus(true, "无关键词要求"));
            keywordFound = checkKeywords(code, keywords, result);
        }
        
        boolean todoOk = !code.contains("// TODO") && !code.contains("//TODO");
        result.checks.put("TODO完成", new ItemStatus(todoOk, todoOk ? "已完成" : "有待完成项"));
        if (!todoOk) {
            result.warnings.add("代码中仍有 TODO，请完成实现");
        }
        
        int totalKw = (keywords == null) ? 0 : keywords.length;
        calculateScore(result, totalKw, keywordFound);
        
        // 高级代码检测与优标记
        if (isAdvancedCode(code)) {
            result.advanced = true;
            result.score = Math.min(result.score + 1.5, result.maxScore + 2);
        }
        
        result.success = result.errors.isEmpty();
        
        return result;
    }
    
    private static boolean isEmptyOrTemplate(String code, String template) {
        String normalizedCode = code.replaceAll("\\s+", "").replaceAll("//.*", "");
        String normalizedTemplate = template.replaceAll("\\s+", "").replaceAll("//.*", "");
        String codeNoTODO = code.replace("// TODO", "").replace("//TODO", "").replaceAll("\\s+", "");
        String templateNoTODO = template.replace("// TODO", "").replace("//TODO", "").replaceAll("\\s+", "");
        return normalizedCode.equals(normalizedTemplate) || codeNoTODO.length() < 20;
    }
    
    private static boolean checkBrackets(String code, CheckResult result) {
        Stack<Character> stack = new Stack<>();
        int line = 1;
        boolean inString = false;
        boolean inChar = false;
        boolean escaped = false;
        boolean hasError = false;
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '\n') line++;
            
            if (c == '\\' && !escaped) {
                escaped = true;
                continue;
            }
            
            if (c == '"' && !escaped && !inChar) inString = !inString;
            if (c == '\'' && !escaped && !inString) inChar = !inChar;
            
            escaped = false;
            
            if (inString || inChar) continue;
            
            if (c == '{' || c == '(' || c == '[') {
                stack.push(c);
            } else if (c == '}' || c == ')' || c == ']') {
                if (stack.isEmpty()) {
                    result.errors.add("第" + line + "行: 多余的闭合括号 '" + c + "'");
                    hasError = true;
                } else {
                    char open = stack.pop();
                    if ((c == '}' && open != '{') || (c == ')' && open != '(') || (c == ']' && open != '[')) {
                        result.errors.add("第" + line + "行: 括号不匹配");
                        hasError = true;
                    }
                }
            }
        }
        
        if (!stack.isEmpty()) {
            result.errors.add("有未闭合的括号");
            hasError = true;
        }
        
        return !hasError;
    }
    
    private static boolean checkQuotes(String code, CheckResult result) {
        String[] lines = code.split("\n");
        boolean hasError = false;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int count = 0;
            boolean escaped = false;
            
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                if (c == '\\') {
                    escaped = !escaped;
                } else if (c == '"' && !escaped) {
                    count++;
                } else {
                    escaped = false;
                }
            }
            
            if (count % 2 != 0) {
                result.errors.add("第" + (i+1) + "行: 字符串引号未闭合");
                hasError = true;
            }
        }
        
        return !hasError;
    }
    
    private static boolean checkSemicolons(String code, CheckResult result) {
        String[] lines = code.split("\n");
        boolean hasWarning = false;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) continue;
            if (line.startsWith("@") || line.startsWith("if") || line.startsWith("else")) continue;
            if (line.startsWith("for") || line.startsWith("while") || line.startsWith("try")) continue;
            if (line.startsWith("catch") || line.startsWith("finally") || line.startsWith("switch")) continue;
            if (line.endsWith("{") || line.endsWith("}") || line.endsWith(";")) continue;
            if (line.contains("class") || line.contains("interface") || line.contains("enum")) continue;
            
            if (!line.endsWith(";") && !line.endsWith("{") && !line.endsWith("}")) {
                result.warnings.add("第" + (i+1) + "行: 可能缺少分号");
                hasWarning = true;
            }
        }
        
        return !hasWarning;
    }
    
    private static int checkKeywords(String code, String[] keywords, CheckResult result) {
        int found = 0;
        String[] lines = code.split("\n");
        
        for (String kw : keywords) {
            boolean kwFound = false;
            boolean inComment = false;
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                if (line.contains("/*")) inComment = true;
                if (line.contains("*/")) {
                    inComment = false;
                    continue;
                }
                
                String trimmed = line.trim();
                if (inComment || trimmed.startsWith("//")) {
                    if (line.contains(kw)) {
                        result.warnings.add("关键词 '" + kw + "' 在注释中");
                    }
                    continue;
                }
                
                if (line.contains(kw)) {
                    int commentPos = line.indexOf("//");
                    int kwPos = line.indexOf(kw);
                    if (commentPos == -1 || kwPos < commentPos) {
                        kwFound = true;
                        result.checks.put("关键词: " + kw, new ItemStatus(true, "已使用"));
                        break;
                    }
                }
            }
            
            if (kwFound) {
                found++;
            } else {
                result.checks.put("关键词: " + kw, new ItemStatus(false, "未找到"));
                result.warnings.add("缺少关键词: " + kw);
            }
        }
        
        return found;
    }
    
    private static void calculateScore(CheckResult result, int totalKeywords, int foundKeywords) {
        if (!result.errors.isEmpty()) {
            result.scoreDetail.syntaxScore = 0;
        } else {
            result.scoreDetail.syntaxScore = 3;
        }
        
        result.scoreDetail.keywordScore = totalKeywords > 0 ? (foundKeywords * 4.0 / totalKeywords) : 4;
        result.scoreDetail.structureScore = result.checks.get("括号配对").passed ? 1.5 : 0;
        result.scoreDetail.structureScore += result.checks.get("引号闭合").passed ? 1.5 : 0;
        result.scoreDetail.penalty = result.warnings.size() * 0.5;
        
        double total = result.scoreDetail.syntaxScore + result.scoreDetail.keywordScore 
                     + result.scoreDetail.structureScore - result.scoreDetail.penalty;
        result.score = Math.max(0, Math.min(10, total));
        result.score = Math.round(result.score * 10) / 10.0;
    }
    
    private static boolean isAdvancedCode(String code) {
        if (Pattern.compile("->\\s*\\{?").matcher(code).find()) return true;
        if (Pattern.compile("\\w+::\\w+").matcher(code).find()) return true;
        if (code.contains(".stream()") || code.contains("Stream.")) return true;
        if (code.contains("Optional.")) return true;
        if (code.contains("try (") && code.contains(") {")) return true;
        return false;
    }
}
