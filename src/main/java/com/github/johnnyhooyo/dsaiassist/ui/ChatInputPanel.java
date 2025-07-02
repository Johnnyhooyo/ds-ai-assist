package com.github.johnnyhooyo.dsaiassist.ui;

import com.github.johnnyhooyo.dsaiassist.command.CommandProcessor;
import com.github.johnnyhooyo.dsaiassist.settings.PluginSettings;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * 聊天输入面板 - 浮窗样式
 */
public class ChatInputPanel extends JPanel implements ThemeAware {

    private final Consumer<String> onSendMessage;
    private final Runnable onClearChat;
    private final Runnable onNewChat;
    private final Project project;
    private final JBTextArea inputTextArea;
    private final JBScrollPane scrollPane;
    private final JButton sendButton;
    private final JPanel bottomBar;
    private final JButton settingsButton;
    private final JButton attachButton;
    private final JComboBox<String> modelComboBox;
    private final JPanel floatingContainer;
    private final CommandProcessor commandProcessor;
    private final AutoCompletePopup autoCompletePopup;
    private final AttachmentManager attachmentManager;

    public ChatInputPanel(Consumer<String> onSendMessage, Runnable onClearChat,
                         Runnable onNewChat, Project project) {
        super(new BorderLayout());
        this.onSendMessage = onSendMessage;
        this.onClearChat = onClearChat;
        this.onNewChat = onNewChat;
        this.project = project;

        // 初始化附件管理器
        this.attachmentManager = new AttachmentManager(project);

        // 初始化命令处理器
        this.commandProcessor = new CommandProcessor(project, this::handleClearCommand, onNewChat, this::handleAttachmentCommand);
        this.autoCompletePopup = new AutoCompletePopup(this, this::insertSuggestion);
        
        inputTextArea = new JBTextArea();
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setRows(3);
        inputTextArea.setFont(inputTextArea.getFont().deriveFont(12f));

        scrollPane = new JBScrollPane(inputTextArea);
        scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(0, 80));
        scrollPane.setBorder(ThemeUtils.getNoBorder());
        scrollPane.setBackground(JBColor.DARK_GRAY);
//        scrollPane.getViewport().setBorder(ThemeUtils.getNoBorder());

        // 初始化底栏按钮
        settingsButton = new JButton("⚙️");
        settingsButton.setToolTipText("设置");
        settingsButton.addActionListener(e -> showSettingsDialog());

        attachButton = new JButton("📎");
        attachButton.setToolTipText("附件 (" + attachmentManager.getAttachmentCount() + ")");
        attachButton.addActionListener(e -> showAttachmentDialog());

        // 模型选择下拉框
        modelComboBox = new JComboBox<>(new String[]{"deepseek-chat", "deepseek-coder", "deepseek-reasoner"});
        modelComboBox.setSelectedItem(PluginSettings.getInstance().getDeepSeekModel());
        modelComboBox.addActionListener(e -> {
            String selectedModel = (String) modelComboBox.getSelectedItem();
            PluginSettings.getInstance().setDeepSeekModel(selectedModel);
        });

        sendButton = new JButton("发送");
        sendButton.addActionListener(e -> sendMessage());

        // 底栏布局：设置按钮(左) | 附件按钮 | 弹簧 | 模型选择 | 发送按钮(右)
        bottomBar = new JPanel();
        bottomBar.setPreferredSize(new Dimension(0, 35));
        bottomBar.setLayout(new BoxLayout(bottomBar, BoxLayout.X_AXIS));

        bottomBar.add(settingsButton);
        bottomBar.add(Box.createHorizontalStrut(5));
        bottomBar.add(attachButton);
        bottomBar.add(Box.createHorizontalGlue()); // 中间弹簧
        bottomBar.add(modelComboBox);
        bottomBar.add(Box.createHorizontalStrut(5));
        bottomBar.add(sendButton);
        
        // 内部容器，用于创建浮窗效果
        floatingContainer = new JPanel(new BorderLayout());
        floatingContainer.add(scrollPane, BorderLayout.CENTER);
        floatingContainer.add(bottomBar, BorderLayout.SOUTH);
        floatingContainer.setBorder(ThemeUtils.getFloatingBorder());
        
        setupLayout();
        setupKeyBindings();
        setupAutoComplete();
        updateTheme();
        // 注册主题变化监听
        ThemeChangeListener.getInstance().registerComponent(this);
    }
    
    private void setupLayout() {
        // 添加浮窗容器到主面板，并在周围留出边距
        setBorder(JBUI.Borders.empty(10, 15, 15, 15));
        add(floatingContainer, BorderLayout.CENTER);
    }
    
    private void setupKeyBindings() {
        inputTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 首先检查自动完成弹窗是否处理了按键
                if (autoCompletePopup.handleKeyNavigation(e)) {
                    return;
                }

                if ((e.isControlDown() || e.isMetaDown()) && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Ctrl+Enter 或 Cmd+Enter 发送消息
                    e.consume();
                    sendMessage();
                } else if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Shift+Enter 换行 - 默认行为，允许换行
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // 单独的 Enter 键发送消息
                    e.consume();
                    sendMessage();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // ESC键隐藏自动完成
                    autoCompletePopup.setVisible(false);;
                }
            }
        });
    }

    private void setupAutoComplete() {
        if (!PluginSettings.getInstance().isEnableAutoComplete()) {
            return;
        }

        inputTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> updateAutoComplete());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> updateAutoComplete());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> updateAutoComplete());
            }
        });
    }

    private void updateAutoComplete() {
        String text = inputTextArea.getText();
        String currentLine = getCurrentLine();

        if (currentLine.startsWith("/")) {
            List<String> suggestions = commandProcessor.getCommandSuggestions(currentLine);
            if (!suggestions.isEmpty()) {
                Point location = getCaretLocation();
                autoCompletePopup.showSuggestions(suggestions, location);
            } else {
                autoCompletePopup.setVisible(false);;
            }
        } else {
            autoCompletePopup.setVisible(false);;
        }
    }

    private String getCurrentLine() {
        try {
            int caretPos = inputTextArea.getCaretPosition();
            String text = inputTextArea.getText();
            int lineStart = text.lastIndexOf('\n', caretPos - 1) + 1;
            return text.substring(lineStart, caretPos);
        } catch (Exception e) {
            return "";
        }
    }

    private Point getCaretLocation() {
        try {
            Rectangle rect = inputTextArea.modelToView(inputTextArea.getCaretPosition());
            Point location = new Point(rect.x, rect.y + rect.height);
            SwingUtilities.convertPointToScreen(location, inputTextArea);
            return location;
        } catch (Exception e) {
            Point location = inputTextArea.getLocationOnScreen();
            return new Point(location.x, location.y + inputTextArea.getHeight());
        }
    }

    private void insertSuggestion(String suggestion) {
        String currentLine = getCurrentLine();
        if (currentLine.startsWith("/")) {
            try {
                int caretPos = inputTextArea.getCaretPosition();
                String text = inputTextArea.getText();
                int lineStart = text.lastIndexOf('\n', caretPos - 1) + 1;

                // 替换当前行
                inputTextArea.replaceRange(suggestion, lineStart, caretPos);
            } catch (Exception e) {
                inputTextArea.setText(suggestion);
            }
        }
    }
    
    @Override
    public void updateTheme() {
        setBackground(ThemeUtils.getBackgroundColor());

        // 更新输入框主题
        inputTextArea.setBackground(ThemeUtils.getInputBackgroundColor());
        inputTextArea.setForeground(ThemeUtils.getEditorForegroundColor());

        // 更新滚动面板主题
        scrollPane.setBackground(ThemeUtils.getInputBackgroundColor());
        scrollPane.getViewport().setBackground(ThemeUtils.getInputBackgroundColor());

        // 更新按钮主题
        Color buttonBg = ThemeUtils.getSelectedBackgroundColor();
        Color buttonFg = ThemeUtils.getForegroundColor();

        sendButton.setBackground(buttonBg);
        sendButton.setForeground(buttonFg);

        settingsButton.setBackground(buttonBg);
        settingsButton.setForeground(buttonFg);

        attachButton.setBackground(buttonBg);
        attachButton.setForeground(buttonFg);

        // 更新模型选择框主题
        modelComboBox.setBackground(ThemeUtils.getInputBackgroundColor());
        modelComboBox.setForeground(ThemeUtils.getEditorForegroundColor());

        // 底栏样式
        bottomBar.setBackground(ThemeUtils.getInputBackgroundColor());
        bottomBar.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, ThemeUtils.getInputBorderColor() // 顶部细线分隔
        ));

        // 更新浮窗容器主题
        floatingContainer.setBackground(ThemeUtils.getInputBackgroundColor());
        floatingContainer.setBorder(ThemeUtils.getFloatingBorder());

        // 重新绘制组件
        repaint();
    }
    
    private void sendMessage() {
        String message = inputTextArea.getText().trim();
        if (!message.isEmpty()) {
            // 隐藏自动完成弹窗
            autoCompletePopup.setVisible(false);;

            // 检查是否为命令
            if (commandProcessor.isCommand(message)) {
                boolean handled = commandProcessor.processCommand(message);
                if (!handled) {
                    // 显示未知命令错误，但不通过onClearChat
                    System.err.println("未知命令: " + message);
                }
            } else {
                // 普通消息，添加附件内容
                String fullMessage = message;
                if (attachmentManager.hasAttachments()) {
                    fullMessage += attachmentManager.generateAttachmentContent();
                    // 发送后清空附件
                    attachmentManager.clearAll();
                    updateAttachmentButton();
                }
                onSendMessage.accept(fullMessage);
            }

            inputTextArea.setText("");
            inputTextArea.requestFocus();
        }
    }

    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog();
        if (dialog.showAndGet()) {
            // 设置已保存，更新模型选择框
            modelComboBox.setSelectedItem(PluginSettings.getInstance().getDeepSeekModel());
        }
    }

    private void showAttachmentDialog() {
        // 简单的附件管理对话框
        StringBuilder message = new StringBuilder("当前附件:\n");
        List<AttachmentManager.AttachedFile> files = attachmentManager.getAttachedFiles();

        if (files.isEmpty()) {
            message.append("无附件");
        } else {
            for (int i = 0; i < files.size(); i++) {
                message.append((i + 1)).append(". ").append(files.get(i).getFilePath()).append("\n");
            }
        }

        message.append("\n使用 /@文件路径 命令添加附件");

        JOptionPane.showMessageDialog(this, message.toString(), "附件管理", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleClearCommand() {
        // 清空聊天记录和输入框
        onClearChat.run();
        inputTextArea.setText("");
        attachmentManager.clearAll();
        updateAttachmentButton();
    }

    private void handleAttachmentCommand(String filePath) {
        boolean added = attachmentManager.addFile(filePath);
        if (added) {
            updateAttachmentButton();
            // 可以显示一个简短的提示
            attachButton.setToolTipText("附件 (" + attachmentManager.getAttachmentCount() + ") - 已添加: " + filePath);
        } else {
            attachButton.setToolTipText("附件 (" + attachmentManager.getAttachmentCount() + ") - 添加失败: " + filePath);
        }

        // 3秒后恢复正常提示
        Timer timer = new Timer(3000, e -> updateAttachmentButton());
        timer.setRepeats(false);
        timer.start();
    }

    private void updateAttachmentButton() {
        int count = attachmentManager.getAttachmentCount();
        attachButton.setToolTipText("附件 (" + count + ")");

        // 如果有附件，改变按钮样式
        if (count > 0) {
            attachButton.setText("📎(" + count + ")");
        } else {
            attachButton.setText("📎");
        }
    }
    
    /**
     * 设置输入框焦点
     */
    public void focusInput() {
        inputTextArea.requestFocus();
    }
    
    /**
     * 获取当前输入的文本
     */
    public String getCurrentText() {
        return inputTextArea.getText();
    }
    
    /**
     * 设置输入框文本
     */
    public void setText(String text) {
        inputTextArea.setText(text);
    }
    
    /**
     * 清空输入框
     */
    public void clearInput() {
        inputTextArea.setText("");
    }
}
