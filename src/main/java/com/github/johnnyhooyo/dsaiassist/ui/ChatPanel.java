package com.github.johnnyhooyo.dsaiassist.ui;

import com.github.johnnyhooyo.dsaiassist.model.ChatMessage;
import com.github.johnnyhooyo.dsaiassist.service.DeepSeekService;
import com.github.johnnyhooyo.dsaiassist.settings.PluginSettings;
import com.google.gson.JsonArray;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 主聊天面板，包含聊天历史和输入区域
 * 上方80%显示聊天历史，下方20%显示输入框
 */
public class ChatPanel extends JPanel implements ThemeAware {

    private final EnhancedChatHistoryPanel chatHistoryPanel;
    private final ChatInputPanel chatInputPanel;
    private final JSplitPane splitPane;
    private final Random random = new Random();
    private final Project project;
    private final DeepSeekService deepSeekService;
    private final Runnable onNewChatCallback;

    public ChatPanel(Project project) {
        this(project, null);
    }

    public ChatPanel(Project project, Runnable onNewChatCallback) {
        super(new BorderLayout());
        this.project = project;
        this.onNewChatCallback = onNewChatCallback;
        this.deepSeekService = ApplicationManager.getApplication().getService(DeepSeekService.class);

        chatHistoryPanel = new EnhancedChatHistoryPanel();
        chatInputPanel = new ChatInputPanel(
            this::handleUserMessage,
            this::handleClearCommand,
            this::handleNewChat,
            project
        );
        
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(chatHistoryPanel);
        splitPane.setBottomComponent(chatInputPanel);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerSize(0); // 隐藏分割线
        splitPane.setBorder(ThemeUtils.getNoBorder());
        
        // 设置分割比例：80% 给历史记录，20% 给输入框
        splitPane.setResizeWeight(0.8);
        
        // 隐藏分割线的UI
        splitPane.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        // 不绘制分割线
                    }
                };
            }
        });
        
        setupLayout();
        addWelcomeMessage();
        updateTheme();
        // 注册主题变化监听
        ThemeChangeListener.getInstance().registerComponent(this);
    }
    
    private void setupLayout() {
        add(splitPane, BorderLayout.CENTER);
        setBorder(ThemeUtils.getNoBorder());
    }
    
    @Override
    public void updateTheme() {
        setBackground(ThemeUtils.getBackgroundColor());
        splitPane.setBackground(ThemeUtils.getBackgroundColor());
        
        // 重新绘制组件
        repaint();
    }
    
    private void addWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
            "欢迎使用 DS AI 助手！\n您可以在下方输入框中输入问题，按 Enter 键或点击发送按钮来发送消息。\n使用 Shift+Enter 可以换行。",
            false
        );
        chatHistoryPanel.addMessage(welcomeMessage);
    }
    
    private void handleUserMessage(String message) {
        // 添加用户消息到历史记录
        ChatMessage userMessage = new ChatMessage(message, true);
        chatHistoryPanel.addMessage(userMessage);
        List<ChatMessage> allMessages = chatHistoryPanel.getAllMessages();
        JsonArray parsedMessages = deepSeekService.parseMessages(allMessages);

        // 检查DeepSeek配置
        PluginSettings settings = PluginSettings.getInstance();
        if (settings.isDeepSeekConfigured()) {
            // 使用DeepSeek API
            sendToDeepSeek(parsedMessages);
        } else {
            // 回退到模拟回复
            simulateAIResponse(message);
        }
    }

    private void handleClearCommand() {
        // 清空聊天历史
        clearChat();
    }

    private void handleNewChat() {
        // 如果有回调函数，创建新的聊天标签页
        if (onNewChatCallback != null) {
            onNewChatCallback.run();
        } else {
            // 回退到清空当前聊天的行为（兼容性）
            clearChat();
            ChatMessage systemMessage = new ChatMessage("已开始新的聊天会话", false);
            chatHistoryPanel.addMessage(systemMessage);
        }
    }

    private void sendToDeepSeek(JsonArray message) {
        PluginSettings settings = PluginSettings.getInstance();
        String apiKey = settings.getDeepSeekApiKey();

        // 创建AI回复消息占位符
        ChatMessage aiMessage = new ChatMessage("", false);
        chatHistoryPanel.addMessage(aiMessage);

        StringBuilder responseBuilder = new StringBuilder();
        StringBuilder reasoningBuilder = new StringBuilder();

        deepSeekService.sendMessageStreamWithReasoning(
            message,
            apiKey,
            chunk -> {
                // 正式内容流式更新
                responseBuilder.append(chunk);
                aiMessage.setContent(responseBuilder.toString());
                chatHistoryPanel.updateLastMessage(aiMessage);
            },
            reasoningChunk -> {
                // 推理内容流式更新（仅在启用时）
                if (settings.isShowReasoningContent()) {
                    reasoningBuilder.append(reasoningChunk);
                    aiMessage.setReasoningContent(reasoningBuilder.toString());
                    chatHistoryPanel.updateLastMessage(aiMessage);
                }
            },
            fullResponse -> {
                // 流式完成：确保最终内容正确
                responseBuilder.append("\n finished!");
                aiMessage.setContent(responseBuilder.toString());
                chatHistoryPanel.updateLastMessage(aiMessage);
            },
            error -> {
                // 错误处理：显示错误消息
                aiMessage.setContent("❌ " + error);
                chatHistoryPanel.updateLastMessage(aiMessage);
            }
        );
    }
    
    private void simulateAIResponse(String userMessage) {
        // 这是一个简单的模拟回复，后续可以替换为真实的AI服务调用
        List<String> responses = Arrays.asList(
            "我理解您的问题：\"" + userMessage + "\"。这是一个很好的问题！",
            "关于\"" + userMessage + "\"，我需要更多信息来为您提供准确的答案。",
            "您提到的\"" + userMessage + "\"是一个有趣的话题。让我为您分析一下...",
            "感谢您的问题：\"" + userMessage + "\"。我正在思考最佳的解决方案。",
            "对于\"" + userMessage + "\"这个问题，我建议您考虑以下几个方面..."
        );
        
        String randomResponse = responses.get(random.nextInt(responses.size()));
        ChatMessage aiMessage = new ChatMessage(randomResponse, false);
        
        // 模拟一点延迟，让回复看起来更自然
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            chatHistoryPanel.addMessage(aiMessage);
        });
    }
    
    /**
     * 清空聊天历史
     */
    public void clearChat() {
        chatHistoryPanel.clearHistory();
        addWelcomeMessage();
    }
    
    /**
     * 设置输入框焦点
     */
    public void focusInput() {
        chatInputPanel.focusInput();
    }
    
    /**
     * 获取聊天历史文本
     */
    public String getChatHistory() {
        return chatHistoryPanel.getAllText();
    }
    
    /**
     * 添加消息到聊天历史
     */
    public void addMessage(ChatMessage message) {
        chatHistoryPanel.addMessage(message);
    }
    
    /**
     * 重写 addNotify 方法，在组件显示时设置分割比例
     */
    @Override
    public void addNotify() {
        super.addNotify();
        // 确保分割比例正确设置
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.8));
    }
}
