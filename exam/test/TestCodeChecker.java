import compiler.CodeChecker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestCodeChecker {
    
    @Test
    public void testEmptyCode() {
        String code = "";
        String[] keywords = {"public", "class"};
        CodeChecker.CheckResult result = CodeChecker.check(code, keywords, "");
        assertFalse(result.success);
        assertEquals(0, result.score);
    }
    
    @Test
    public void testNullKeywords() {
        String code = "public class Test {}";
        CodeChecker.CheckResult result = CodeChecker.check(code, null, "");
        assertNotNull(result);
        assertTrue(result.checks.containsKey("关键词检查"));
    }
}
