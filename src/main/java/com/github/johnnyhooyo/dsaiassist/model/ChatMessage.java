package com.github.johnnyhooyo.dsaiassist.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 聊天消息数据模型
 */
public class ChatMessage {
    private String content; // 改为可变，支持流式更新
    private String reasoningContent; // 推理内容，支持流式更新
    private final boolean isUser; // true表示用户消息，false表示AI回复
    private final LocalDateTime timestamp;

    public ChatMessage(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String content, boolean isUser, LocalDateTime timestamp) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReasoningContent() {
        return reasoningContent;
    }

    public void setReasoningContent(String reasoningContent) {
        this.reasoningContent = reasoningContent;
    }

    public boolean hasReasoningContent() {
        return reasoningContent != null && !reasoningContent.trim().isEmpty();
    }

    public boolean isUser() {
        return isUser;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return isUser == that.isUser &&
                Objects.equals(content, that.content) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, isUser, timestamp);
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "content='" + content + '\'' +
                ", isUser=" + isUser +
                ", timestamp=" + timestamp +
                '}';
    }
}
