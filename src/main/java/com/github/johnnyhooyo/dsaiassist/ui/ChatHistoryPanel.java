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

/**
 * 聊天历史显示面板
 */
public class ChatHistoryPanel extends JPanel implements ThemeAware {
    
    private final JTextPane textPane;
    private final JBScrollPane scrollPane;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final List<ChatMessage> messages = new ArrayList<>();
    
    public ChatHistoryPanel() {
        super(new BorderLayout());
        
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textPane.setEditorKit(new WrapEditorKit()); // 添加自动换行支持

        scrollPane = new JBScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(ThemeUtils.getNoBorder());
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                ThemeUtils.getNoBorder(), // 外部浮动边框
                JBUI.Borders.empty(10)          // 内部5像素留白
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
    
    @Override
    public void updateTheme() {
        setBackground(ThemeUtils.getBackgroundColor());
        textPane.setBackground(ThemeUtils.getBackgroundColor());
        textPane.setForeground(ThemeUtils.getForegroundColor());
        scrollPane.setBackground(ThemeUtils.getBackgroundColor());
        scrollPane.getViewport().setBackground(ThemeUtils.getBackgroundColor());
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                ThemeUtils.getNoBorder(), // 外部浮动边框
                JBUI.Borders.empty(10)          // 内部5像素留白
        ));
        // 重新绘制组件
        repaint();
    }
    
    /**
     * 添加聊天消息到历史记录
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
        renderAllMessages();
    }

    /**
     * 更新最后一条消息（用于流式更新）
     */
    public void updateLastMessage(ChatMessage message) {
        if (!messages.isEmpty()) {
            // 更新最后一条消息
            messages.set(messages.size() - 1, message);
            renderAllMessages();
        }
    }

    /**
     * 获取所有消息(用于多轮对话）
     */
    public List<ChatMessage> getAllMessages() {
        return messages;
    }

    /**
     * 渲染所有消息
     */
    private void renderAllMessages() {
        textPane.setText(""); // 清空现有内容
        StyledDocument doc = textPane.getStyledDocument();

        try {
            for (ChatMessage message : messages) {
                String timeStr = message.getTimestamp().format(timeFormatter);

                // 添加时间戳和发送者
                SimpleAttributeSet timeStyle = new SimpleAttributeSet();
                StyleConstants.setFontFamily(timeStyle, Font.SANS_SERIF);
                StyleConstants.setFontSize(timeStyle, 10);
                StyleConstants.setForeground(timeStyle, ThemeUtils.getTimestampColor());

                String sender = message.isUser() ? "您" : "AI助手";
                doc.insertString(doc.getLength(), "[" + timeStr + "] " + sender + ":\n", timeStyle);

                // 如果是AI消息且有推理内容，先显示推理内容
                if (!message.isUser() && message.hasReasoningContent() && PluginSettings.getInstance().isShowReasoningContent()) {
                    addReasoningContent(message.getReasoningContent());
                    doc.insertString(doc.getLength(), "\n", null); // 推理内容和正式内容之间的分隔
                }

                // 根据设置决定是否使用Markdown渲染
                if (PluginSettings.getInstance().isEnableMarkdown() && !message.isUser()) {
                    // AI消息使用Markdown渲染
                    addMarkdownContent(message.getContent(), message.isUser());
                } else {
                    // 用户消息或禁用Markdown时使用普通文本
                    addPlainTextContent(message.getContent(), message.isUser());
                }

                // 添加空行分隔
                doc.insertString(doc.getLength(), "\n", null);
            }

            // 自动滚动到底部
            SwingUtilities.invokeLater(() -> textPane.setCaretPosition(doc.getLength()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加Markdown内容
     */
    private void addMarkdownContent(String content, boolean isUser) {
        try {
            // 创建临时文档用于Markdown渲染
            StyledDocument tempDoc = new DefaultStyledDocument();
            MarkdownRenderer.renderMarkdown(content, tempDoc, isUser);

            // 将渲染结果复制到主文档
            StyledDocument mainDoc = textPane.getStyledDocument();
            for (int i = 0; i < tempDoc.getLength(); i++) {
                String text = tempDoc.getText(i, 1);
                AttributeSet attrs = tempDoc.getCharacterElement(i).getAttributes();
                mainDoc.insertString(mainDoc.getLength(), text, attrs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果Markdown渲染失败，回退到普通文本
            addPlainTextContent(content, isUser);
        }
    }

    /**
     * 添加普通文本内容
     */
    private void addPlainTextContent(String content, boolean isUser) {
        try {
            StyledDocument doc = textPane.getStyledDocument();

            SimpleAttributeSet messageStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(messageStyle, Font.SANS_SERIF);
            StyleConstants.setFontSize(messageStyle, 12);
            if (isUser) {
                StyleConstants.setForeground(messageStyle, ThemeUtils.getUserMessageColor());
                StyleConstants.setBold(messageStyle, true);
            } else {
                StyleConstants.setForeground(messageStyle, ThemeUtils.getAIMessageColor());
                StyleConstants.setBold(messageStyle, false);
            }

            doc.insertString(doc.getLength(), content + "\n", messageStyle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加推理内容（灰色背景的代码框样式）
     */
    private void addReasoningContent(String reasoningContent) {
        try {
            StyledDocument doc = textPane.getStyledDocument();

            // 创建推理内容的样式（灰色背景的代码框样式）
            SimpleAttributeSet reasoningStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(reasoningStyle, Font.MONOSPACED);
            StyleConstants.setFontSize(reasoningStyle, 11);
            StyleConstants.setForeground(reasoningStyle, ThemeUtils.getCodeTextColor());
            StyleConstants.setBackground(reasoningStyle, ThemeUtils.getCodeBackgroundColor());

            // 添加推理内容标题
            SimpleAttributeSet titleStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(titleStyle, Font.SANS_SERIF);
            StyleConstants.setFontSize(titleStyle, 11);
            StyleConstants.setForeground(titleStyle, ThemeUtils.getTimestampColor());
            StyleConstants.setItalic(titleStyle, true);

            doc.insertString(doc.getLength(), "🤔 推理过程：\n", titleStyle);
            doc.insertString(doc.getLength(), reasoningContent + "\n", reasoningStyle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空聊天历史
     */
    public void clearHistory() {
        messages.clear();
        textPane.setText("");
    }
    
    /**
     * 获取所有聊天内容
     */
    public String getAllText() {
        return textPane.getText();
    }

    /**
     * 自定义编辑器工具包 - 实现自动换行
     */
    private static class WrapEditorKit extends StyledEditorKit {
        private final ViewFactory viewFactory = new WrapViewFactory();

        @Override
        public ViewFactory getViewFactory() {
            return viewFactory;
        }

        private static class WrapViewFactory implements ViewFactory {
            public View create(Element elem) {
                String kind = elem.getName();
                if (kind != null) {
                    switch (kind) {
                        case AbstractDocument.ContentElementName:
                            return new WrapLabelView(elem);
                        case AbstractDocument.ParagraphElementName:
                            return new ParagraphView(elem);
                        case AbstractDocument.SectionElementName:
                            return new BoxView(elem, View.Y_AXIS);
                        case StyleConstants.ComponentElementName:
                            return new ComponentView(elem);
                        case StyleConstants.IconElementName:
                            return new IconView(elem);
                    }
                }
                return new LabelView(elem);
            }
        }

        private static class WrapLabelView extends LabelView {
            public WrapLabelView(Element elem) {
                super(elem);
            }

            @Override
            public float getMinimumSpan(int axis) {
                // 关键换行逻辑：X轴方向最小跨度为0，允许换行
                return switch (axis) {
                    case View.X_AXIS -> 0;
                    case View.Y_AXIS -> super.getMinimumSpan(axis);
                    default -> throw new IllegalArgumentException("Invalid axis: " + axis);
                };
            }
        }
    }
}
