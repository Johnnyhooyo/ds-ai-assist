package com.github.johnnyhooyo.dsaiassist.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ChatMessage 测试类
 */
public class ChatMessageTest {

    @Test
    public void testBasicChatMessage() {
        ChatMessage message = new ChatMessage("Hello", true);
        
        assertEquals("Hello", message.getContent());
        assertTrue(message.isUser());
        assertNotNull(message.getTimestamp());
        assertNull(message.getReasoningContent());
        assertFalse(message.hasReasoningContent());
    }

    @Test
    public void testReasoningContent() {
        ChatMessage message = new ChatMessage("AI Response", false);
        
        // 初始状态没有推理内容
        assertFalse(message.hasReasoningContent());
        
        // 设置推理内容
        message.setReasoningContent("This is reasoning content");
        assertTrue(message.hasReasoningContent());
        assertEquals("This is reasoning content", message.getReasoningContent());
        
        // 设置空推理内容
        message.setReasoningContent("");
        assertFalse(message.hasReasoningContent());
        
        // 设置null推理内容
        message.setReasoningContent(null);
        assertFalse(message.hasReasoningContent());
        
        // 设置只有空格的推理内容
        message.setReasoningContent("   ");
        assertFalse(message.hasReasoningContent());
    }

    @Test
    public void testContentUpdate() {
        ChatMessage message = new ChatMessage("Initial content", false);
        assertEquals("Initial content", message.getContent());
        
        // 更新内容
        message.setContent("Updated content");
        assertEquals("Updated content", message.getContent());
        
        // 同时设置推理内容和正式内容
        message.setReasoningContent("Reasoning process");
        message.setContent("Final answer");
        
        assertEquals("Final answer", message.getContent());
        assertEquals("Reasoning process", message.getReasoningContent());
        assertTrue(message.hasReasoningContent());
    }
}
