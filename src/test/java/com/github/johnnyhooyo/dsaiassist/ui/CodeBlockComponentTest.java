package com.github.johnnyhooyo.dsaiassist.ui;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

/**
 * CodeBlockComponent 测试类
 */
public class CodeBlockComponentTest extends BasePlatformTestCase {

    @Test
    public void testCodeBlockCreation() {
        // 测试代码块组件的创建
        String code = "public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}";
        String language = "java";
        
        CodeBlockComponent codeBlock = new CodeBlockComponent(code, language);
        
        assertNotNull("CodeBlock should not be null", codeBlock);
        assertEquals("Code content should match", code, codeBlock.getCode());
        assertEquals("Language should match", language, codeBlock.getLanguage());
    }

    @Test
    public void testCodeBlockWithEmptyLanguage() {
        // 测试没有语言标识的代码块
        String code = "console.log('Hello, World!');";
        String language = "";
        
        CodeBlockComponent codeBlock = new CodeBlockComponent(code, language);
        
        assertNotNull("CodeBlock should not be null", codeBlock);
        assertEquals("Code content should match", code, codeBlock.getCode());
        assertEquals("Language should default to 'text'", "text", codeBlock.getLanguage());
    }

    @Test
    public void testCodeBlockWithNullLanguage() {
        // 测试语言为null的代码块
        String code = "echo 'Hello, World!'";
        
        CodeBlockComponent codeBlock = new CodeBlockComponent(code, null);
        
        assertNotNull("CodeBlock should not be null", codeBlock);
        assertEquals("Code content should match", code, codeBlock.getCode());
        assertEquals("Language should default to 'text'", "text", codeBlock.getLanguage());
    }

    @Test
    public void testEnhancedChatHistoryPanel() {
        // 测试增强聊天历史面板的创建
        EnhancedChatHistoryPanel panel = new EnhancedChatHistoryPanel();
        
        assertNotNull("Enhanced chat history panel should not be null", panel);
        assertTrue("Panel should have no messages initially", panel.getAllMessages().isEmpty());
        assertNotNull("getAllText should not return null", panel.getAllText());
    }
}
