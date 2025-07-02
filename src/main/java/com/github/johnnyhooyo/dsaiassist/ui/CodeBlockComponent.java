package com.github.johnnyhooyo.dsaiassist.ui;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 代码块组件，包含框头、复制按钮和语言标识
 */
public class CodeBlockComponent extends JPanel {
    
    private final String code;
    private final String language;
    private final JTextArea codeArea;
    private JButton copyButton;
    private JBLabel languageLabel;
    
    public CodeBlockComponent(String code, String language) {
        this.code = code;
        this.language = language != null && !language.trim().isEmpty() ? language.trim() : "text";
        
        setLayout(new BorderLayout());
        setBorder(createCodeBlockBorder());
        
        // 创建框头
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // 创建代码区域
        codeArea = createCodeArea();
        JBScrollPane scrollPane = new JBScrollPane(codeArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(JBUI.Borders.empty());
        add(scrollPane, BorderLayout.CENTER);
        
        // 按钮和标签已在createHeaderPanel中创建
        
        updateTheme();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(JBUI.Borders.empty(5, 10));
        
        // 左侧：语言标签
        languageLabel = createLanguageLabel();
        headerPanel.add(languageLabel, BorderLayout.WEST);
        
        // 右侧：复制按钮
        copyButton = createCopyButton();
        headerPanel.add(copyButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JBLabel createLanguageLabel() {
        JBLabel label = new JBLabel(language.toUpperCase());
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        return label;
    }
    
    private JButton createCopyButton() {
        JButton button = new JButton("复制");
        button.setFont(button.getFont().deriveFont(10f));
        button.setMargin(JBUI.insets(2, 8));
        button.setFocusPainted(false);
        
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyCodeToClipboard();
            }
        });
        
        return button;
    }
    
    private JTextArea createCodeArea() {
        JTextArea textArea = new JTextArea(code);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setTabSize(4);
        textArea.setLineWrap(false);
        textArea.setWrapStyleWord(false);
        
        // 设置边距
        textArea.setBorder(JBUI.Borders.empty(10));
        
        return textArea;
    }
    
    private Border createCodeBlockBorder() {
        // 创建简单边框
        return JBUI.Borders.compound(
            JBUI.Borders.customLine(ThemeUtils.getCodeBackgroundColor().darker(), 1),
            JBUI.Borders.empty(0)
        );
    }
    
    private void copyCodeToClipboard() {
        try {
            StringSelection selection = new StringSelection(code);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            
            // 临时改变按钮文本以提供反馈
            String originalText = copyButton.getText();
            copyButton.setText("已复制");
            copyButton.setEnabled(false);
            
            // 1秒后恢复
            Timer timer = new Timer(1000, e -> {
                copyButton.setText(originalText);
                copyButton.setEnabled(true);
            });
            timer.setRepeats(false);
            timer.start();
            
        } catch (Exception e) {
            // 复制失败时的处理
            copyButton.setText("复制失败");
            Timer timer = new Timer(1000, evt -> copyButton.setText("复制"));
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    public void updateTheme() {
        // 更新主题颜色
        setBackground(ThemeUtils.getCodeBackgroundColor());
        
        // 更新代码区域主题
        if (codeArea != null) {
            codeArea.setBackground(ThemeUtils.getCodeBackgroundColor());
            codeArea.setForeground(ThemeUtils.getCodeTextColor());
        }
        
        // 更新框头主题
        Component headerPanel = getComponent(0);
        if (headerPanel instanceof JPanel) {
            headerPanel.setBackground(ThemeUtils.getCodeBackgroundColor().brighter());
            
            // 更新语言标签
            if (languageLabel != null) {
                languageLabel.setForeground(ThemeUtils.getTimestampColor());
            }
            
            // 更新复制按钮
            if (copyButton != null) {
                copyButton.setBackground(ThemeUtils.getSelectedBackgroundColor());
                copyButton.setForeground(ThemeUtils.getForegroundColor());
            }
        }
        
        repaint();
    }
    
    public String getCode() {
        return code;
    }
    
    public String getLanguage() {
        return language;
    }
}
