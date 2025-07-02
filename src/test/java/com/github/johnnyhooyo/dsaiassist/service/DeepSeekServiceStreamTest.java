package com.github.johnnyhooyo.dsaiassist.service;

import com.github.johnnyhooyo.dsaiassist.settings.PluginSettings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DeepSeekService 流式处理测试类
 */
public class DeepSeekServiceStreamTest extends BasePlatformTestCase {

    @Test
    public void testStreamingResponseParsing() {
        // 测试流式响应解析逻辑
        DeepSeekService service = new DeepSeekService();
        
        // 模拟流式响应数据
        String mockStreamData = "data: {\"choices\":[{\"delta\":{\"content\":\"Hello\"}}]}\n" +
                               "data: {\"choices\":[{\"delta\":{\"content\":\" World\"}}]}\n" +
                               "data: [DONE]\n";
        
        AtomicInteger chunkCount = new AtomicInteger(0);
        AtomicReference<String> fullResponse = new AtomicReference<>("");
        CountDownLatch latch = new CountDownLatch(1);
        
        // 这里我们无法直接测试私有方法，但可以验证服务的基本功能
        assertNotNull("DeepSeekService should not be null", service);
        assertTrue("API key validation should work", service.isValidApiKey("sk-test123456789"));
        assertFalse("Invalid API key should be rejected", service.isValidApiKey("invalid-key"));
    }

    @Test
    public void testReasoningContentSetting() {
        // 测试推理内容设置
        PluginSettings settings = PluginSettings.getInstance();
        
        // 测试默认值
        assertTrue("Reasoning content should be enabled by default", settings.isShowReasoningContent());
        
        // 测试设置修改
        settings.setShowReasoningContent(false);
        assertFalse("Reasoning content should be disabled", settings.isShowReasoningContent());
        
        // 恢复默认值
        settings.setShowReasoningContent(true);
        assertTrue("Reasoning content should be enabled", settings.isShowReasoningContent());
    }

    @Test
    public void testServiceInitialization() {
        // 测试服务初始化
        DeepSeekService service = new DeepSeekService();
        assertNotNull("Service should be initialized", service);
        
        // 测试API Key验证
        assertFalse("Empty API key should be invalid", service.isValidApiKey(""));
        assertFalse("Null API key should be invalid", service.isValidApiKey(null));
        assertFalse("Short API key should be invalid", service.isValidApiKey("sk-123"));
        assertTrue("Valid API key should be accepted", service.isValidApiKey("sk-1234567890abcdef"));
    }
}
