package com.github.johnnyhooyo.dsaiassist.ui;

import com.github.johnnyhooyo.dsaiassist.model.ChatMessage;
import com.github.johnnyhooyo.dsaiassist.settings.PluginSettings;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 增强的聊天历史面板，支持代码块组件
 */
public class EnhancedChatHistoryPanel extends JPanel implements ThemeAware {
    
    private final List<ChatMessage> messages = new ArrayList<>();
    private final JPanel contentPanel;
    private final JBScrollPane scrollPane;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // 代码块正则表达式
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```");
    
    public EnhancedChatHistoryPanel() {
        super(new BorderLayout());

        // 创建内容面板
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 添加底部弹性空间，确保内容从顶部开始排列
        addBottomGlue();

        // 创建滚动面板
        scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                ThemeUtils.getNoBorder(),
                JBUI.Borders.empty(10)
        ));

        setupLayout();
        updateTheme();

        // 注册主题变化监听
        ThemeChangeListener.getInstance().registerComponent(this);
    }
    
    private void setupLayout() {
        add(scrollPane, BorderLayout.CENTER);
        setBorder(ThemeUtils.getNoBorder());
    }

    /**
     * 添加底部弹性空间，确保内容从顶部开始排列
     */
    private void addBottomGlue() {
        contentPanel.add(Box.createVerticalGlue());
    }

    /**
     * 移除底部弹性空间
     */
    private void removeBottomGlue() {
        Component[] components = contentPanel.getComponents();
        for (int i = components.length - 1; i >= 0; i--) {
            if (components[i] instanceof Box.Filler) {
                contentPanel.remove(i);
                break;
            }
        }
    }
    
    @Override
    public void updateTheme() {
        setBackground(ThemeUtils.getBackgroundColor());
        contentPanel.setBackground(ThemeUtils.getBackgroundColor());
        scrollPane.setBackground(ThemeUtils.getBackgroundColor());
        scrollPane.getViewport().setBackground(ThemeUtils.getBackgroundColor());
        
        // 更新所有代码块组件的主题
        updateCodeBlockThemes();
        
        repaint();
    }
    
    private void updateCodeBlockThemes() {
        Component[] components = contentPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                updatePanelCodeBlocks((JPanel) component);
            }
        }
    }
    
    private void updatePanelCodeBlocks(JPanel panel) {
        Component[] components = panel.getComponents();
        for (Component component : components) {
            if (component instanceof CodeBlockComponent) {
                ((CodeBlockComponent) component).updateTheme();
            } else if (component instanceof JPanel) {
                updatePanelCodeBlocks((JPanel) component);
            }
        }
    }
    
    /**
     * 添加聊天消息到历史记录
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);

        // 移除底部弹性空间
        removeBottomGlue();

        // 渲染消息
        renderMessage(message);

        // 重新添加底部弹性空间
        addBottomGlue();

        scrollToBottom();
    }
    
    /**
     * 更新最后一条消息
     */
    public void updateLastMessage(ChatMessage message) {
        if (!messages.isEmpty()) {
            messages.set(messages.size() - 1, message);

            // 移除底部弹性空间
            removeBottomGlue();

            // 移除最后一个消息组件（不包括弹性空间）
            Component[] components = contentPanel.getComponents();
            for (int i = components.length - 1; i >= 0; i--) {
                if (!(components[i] instanceof Box.Filler)) {
                    contentPanel.remove(i);
                    break;
                }
            }

            // 重新渲染消息
            renderMessage(message);

            // 重新添加底部弹性空间
            addBottomGlue();

            scrollToBottom();
        }
    }
    
    /**
     * 渲染单条消息
     */
    private void renderMessage(ChatMessage message) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(ThemeUtils.getBackgroundColor());
        messagePanel.setBorder(JBUI.Borders.empty(5, 0));

        // 设置面板对齐方式
        messagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 添加时间戳和发送者
        String timeStr = message.getTimestamp().format(timeFormatter);
        String sender = message.isUser() ? "您" : "AI助手";
        JLabel headerLabel = new JLabel("[" + timeStr + "] " + sender + ":");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        headerLabel.setForeground(ThemeUtils.getTimestampColor());
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagePanel.add(headerLabel);
        
        // 如果是AI消息且有推理内容，先显示推理内容
        if (!message.isUser() && message.hasReasoningContent() && PluginSettings.getInstance().isShowReasoningContent()) {
            addReasoningContent(messagePanel, message.getReasoningContent());
        }
        
        // 添加消息内容
        addMessageContent(messagePanel, message.getContent(), message.isUser());

        // 设置面板最大尺寸，防止垂直拉伸
        messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, messagePanel.getPreferredSize().height));

        contentPanel.add(messagePanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * 添加推理内容
     */
    private void addReasoningContent(JPanel messagePanel, String reasoningContent) {
        JLabel reasoningLabel = new JLabel("🤔 推理过程：");
        reasoningLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        reasoningLabel.setForeground(ThemeUtils.getTimestampColor());
        reasoningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagePanel.add(reasoningLabel);
        
        CodeBlockComponent reasoningBlock = new CodeBlockComponent(reasoningContent, "reasoning");
        reasoningBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasoningBlock.setMaximumSize(new Dimension(Integer.MAX_VALUE, reasoningBlock.getPreferredSize().height));
        messagePanel.add(reasoningBlock);
        
        // 添加间距
        messagePanel.add(Box.createVerticalStrut(10));
    }
    
    /**
     * 添加消息内容（支持代码块）
     */
    private void addMessageContent(JPanel messagePanel, String content, boolean isUser) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        
        // 解析代码块
        List<ContentPart> parts = parseContentWithCodeBlocks(content);
        
        for (ContentPart part : parts) {
            if (part.isCodeBlock) {
                // 添加代码块组件
                CodeBlockComponent codeBlock = new CodeBlockComponent(part.content, part.language);
                codeBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
                codeBlock.setMaximumSize(new Dimension(Integer.MAX_VALUE, codeBlock.getPreferredSize().height));
                messagePanel.add(codeBlock);
                messagePanel.add(Box.createVerticalStrut(5));
            } else {
                // 添加普通文本
                if (!part.content.trim().isEmpty()) {
                    JTextPane textPane = createTextPane(part.content, isUser);
                    textPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                    messagePanel.add(textPane);
                    messagePanel.add(Box.createVerticalStrut(5));
                }
            }
        }
    }
    
    /**
     * 解析内容，分离代码块和普通文本
     */
    private List<ContentPart> parseContentWithCodeBlocks(String content) {
        List<ContentPart> parts = new ArrayList<>();
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(content);
        int lastEnd = 0;
        
        while (matcher.find()) {
            // 添加代码块前的文本
            if (matcher.start() > lastEnd) {
                String textContent = content.substring(lastEnd, matcher.start());
                if (!textContent.trim().isEmpty()) {
                    parts.add(new ContentPart(textContent, false, null));
                }
            }
            
            // 添加代码块
            String language = matcher.group(1);
            String codeContent = matcher.group(2);
            parts.add(new ContentPart(codeContent, true, language));
            
            lastEnd = matcher.end();
        }
        
        // 添加剩余文本
        if (lastEnd < content.length()) {
            String textContent = content.substring(lastEnd);
            if (!textContent.trim().isEmpty()) {
                parts.add(new ContentPart(textContent, false, null));
            }
        }
        
        // 如果没有代码块，整个内容作为文本
        if (parts.isEmpty()) {
            parts.add(new ContentPart(content, false, null));
        }
        
        return parts;
    }
    
    /**
     * 创建文本面板
     */
    private JTextPane createTextPane(String content, boolean isUser) {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBorder(JBUI.Borders.empty(5));
        
        // 设置样式
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, Font.SANS_SERIF);
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setForeground(style, isUser ? ThemeUtils.getUserMessageColor() : ThemeUtils.getAIMessageColor());
        
        try {
            doc.insertString(0, content, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        return textPane;
    }
    
    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }
    
    /**
     * 清空聊天历史
     */
    public void clearHistory() {
        messages.clear();
        contentPanel.removeAll();

        // 重新添加底部弹性空间
        addBottomGlue();

        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * 获取所有消息
     */
    public List<ChatMessage> getAllMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * 获取所有聊天内容的文本表示
     */
    public String getAllText() {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : messages) {
            String timeStr = message.getTimestamp().format(timeFormatter);
            String sender = message.isUser() ? "您" : "AI助手";
            sb.append("[").append(timeStr).append("] ").append(sender).append(":\n");

            if (!message.isUser() && message.hasReasoningContent() && PluginSettings.getInstance().isShowReasoningContent()) {
                sb.append("🤔 推理过程：\n").append(message.getReasoningContent()).append("\n\n");
            }

            sb.append(message.getContent()).append("\n\n");
        }
        return sb.toString();
    }
    
    /**
     * 内容部分类
     */
    private static class ContentPart {
        final String content;
        final boolean isCodeBlock;
        final String language;
        
        ContentPart(String content, boolean isCodeBlock, String language) {
            this.content = content;
            this.isCodeBlock = isCodeBlock;
            this.language = language;
        }
    }
}
