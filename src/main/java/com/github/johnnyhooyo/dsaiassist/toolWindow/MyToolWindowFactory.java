package com.github.johnnyhooyo.dsaiassist.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.github.johnnyhooyo.dsaiassist.ui.ChatPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DS AI 助手工具窗口工厂类
 * 创建右侧聊天界面工具窗口，支持多标签页聊天
 */
public class MyToolWindowFactory implements ToolWindowFactory {

    private static final Logger LOG = Logger.getInstance(MyToolWindowFactory.class);

    // 用于生成聊天标签页的序号
    private static final AtomicInteger chatCounter = new AtomicInteger(1);

    public MyToolWindowFactory() {
        LOG.info("DS AI Assistant Tool Window Factory initialized");
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建第一个聊天标签页
        createNewChatTab(project, toolWindow, "AI 助手");
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    /**
     * 创建新的聊天标签页
     */
    public static void createNewChatTab(@NotNull Project project, @NotNull ToolWindow toolWindow, String title) {
        ChatToolWindow chatToolWindow = new ChatToolWindow(project, toolWindow);
        Content content = ContentFactory.getInstance().createContent(
            chatToolWindow.getContent(),
            title,
            false
        );
        content.setCloseable(true); // 允许关闭标签页

        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addContent(content);
        contentManager.setSelectedContent(content); // 选中新创建的标签页

        LOG.info("Created new chat tab: " + title);
    }

    /**
     * 创建新的聊天会话（由 /newchat 命令调用）
     */
    public static void createNewChatSession(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        int chatNumber = chatCounter.getAndIncrement();
        String title = "聊天 " + chatNumber;
        createNewChatTab(project, toolWindow, title);
    }

    /**
     * 聊天工具窗口实现类
     */
    public static class ChatToolWindow {

        private final Project project;
        private final ToolWindow toolWindow;
        private final ChatPanel chatPanel;

        public ChatToolWindow(Project project, ToolWindow toolWindow) {
            this.project = project;
            this.toolWindow = toolWindow;
            // 传递创建新聊天会话的回调函数
            this.chatPanel = new ChatPanel(project, () -> createNewChatSession(project, toolWindow));
        }

        public JComponent getContent() {
            return chatPanel;
        }

        /**
         * 获取聊天面板实例
         */
        public ChatPanel getChatPanel() {
            return chatPanel;
        }
    }
}
