package com.github.johnnyhooyo.dsaiassist.settings;

import com.github.johnnyhooyo.dsaiassist.service.DeepSeekService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 插件设置配置面板
 */
public class SettingsConfigurable implements Configurable {
    
    private JPanel mainPanel;
    private JBPasswordField apiKeyField;
    private JBTextField modelField;
    private JSpinner temperatureSpinner;
    private JSpinner maxTokensSpinner;
    private JBCheckBox enableMarkdownCheckBox;
    private JBCheckBox enableAutoCompleteCheckBox;
    private JBCheckBox enableSyntaxHighlightCheckBox;
    private JBCheckBox saveHistoryCheckBox;
    private JSpinner maxHistorySizeSpinner;
    private JBCheckBox showReasoningContentCheckBox;
    private JButton testConnectionButton;
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "DS AI Assistant";
    }
    
    @Nullable
    @Override
    public JComponent createComponent() {
        if (mainPanel == null) {
            initializeComponents();
            createMainPanel();
        }
        return mainPanel;
    }
    
    private void initializeComponents() {
        // API设置
        apiKeyField = new JBPasswordField();
        apiKeyField.setColumns(30);
        
        modelField = new JBTextField("deepseek-chat");
        modelField.setColumns(20);
        
        temperatureSpinner = new JSpinner(new SpinnerNumberModel(0.7, 0.0, 2.0, 0.1));
        maxTokensSpinner = new JSpinner(new SpinnerNumberModel(2048, 1, 4096, 100));
        
        testConnectionButton = new JButton("测试连接");
        testConnectionButton.addActionListener(this::testConnection);
        
        // 界面设置
        enableMarkdownCheckBox = new JBCheckBox("启用Markdown渲染");
        enableAutoCompleteCheckBox = new JBCheckBox("启用命令自动完成");
        enableSyntaxHighlightCheckBox = new JBCheckBox("启用语法高亮");
        
        // 聊天设置
        saveHistoryCheckBox = new JBCheckBox("保存聊天历史");
        maxHistorySizeSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 10));
        showReasoningContentCheckBox = new JBCheckBox("显示推理过程（DeepSeek Reasoner）");
    }
    
    private void createMainPanel() {
        // API设置面板
        JPanel apiPanel = createApiSettingsPanel();
        
        // 界面设置面板
        JPanel uiPanel = createUISettingsPanel();
        
        // 聊天设置面板
        JPanel chatPanel = createChatSettingsPanel();
        
        // 主面板
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(apiPanel)
                .addVerticalGap(10)
                .addComponent(uiPanel)
                .addVerticalGap(10)
                .addComponent(chatPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }
    
    private JPanel createApiSettingsPanel() {
        JPanel apiKeyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        apiKeyPanel.add(apiKeyField);
        apiKeyPanel.add(Box.createHorizontalStrut(10));
        apiKeyPanel.add(testConnectionButton);
        
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("DeepSeek API设置"), new JPanel())
                .addLabeledComponent("API Key:", apiKeyPanel)
                .addLabeledComponent("模型:", modelField)
                .addLabeledComponent("Temperature:", temperatureSpinner)
                .addLabeledComponent("Max Tokens:", maxTokensSpinner)
                .getPanel();
    }
    
    private JPanel createUISettingsPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("界面设置"), new JPanel())
                .addComponent(enableMarkdownCheckBox)
                .addComponent(enableAutoCompleteCheckBox)
                .addComponent(enableSyntaxHighlightCheckBox)
                .getPanel();
    }
    
    private JPanel createChatSettingsPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("聊天设置"), new JPanel())
                .addComponent(saveHistoryCheckBox)
                .addLabeledComponent("最大历史记录数:", maxHistorySizeSpinner)
                .addComponent(showReasoningContentCheckBox)
                .getPanel();
    }
    
    private void testConnection(ActionEvent e) {
        String apiKey = new String(apiKeyField.getPassword());
        if (apiKey.trim().isEmpty()) {
            Messages.showWarningDialog("请先输入API Key", "测试连接");
            return;
        }
        
        testConnectionButton.setEnabled(false);
        testConnectionButton.setText("测试中...");
        
        DeepSeekService service = ApplicationManager.getApplication().getService(DeepSeekService.class);
        service.testConnection(apiKey).thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                testConnectionButton.setEnabled(true);
                testConnectionButton.setText("测试连接");
                
                if (success) {
                    Messages.showInfoMessage("连接成功！", "测试连接");
                } else {
                    Messages.showErrorDialog("连接失败，请检查API Key是否正确", "测试连接");
                }
            });
        });
    }
    
    @Override
    public boolean isModified() {
        PluginSettings settings = PluginSettings.getInstance();
        
        return !settings.getDeepSeekApiKey().equals(new String(apiKeyField.getPassword())) ||
               !settings.getDeepSeekModel().equals(modelField.getText()) ||
               settings.getTemperature() != (Double) temperatureSpinner.getValue() ||
               settings.getMaxTokens() != (Integer) maxTokensSpinner.getValue() ||
               settings.isEnableMarkdown() != enableMarkdownCheckBox.isSelected() ||
               settings.isEnableAutoComplete() != enableAutoCompleteCheckBox.isSelected() ||
               settings.isEnableSyntaxHighlight() != enableSyntaxHighlightCheckBox.isSelected() ||
               settings.isSaveHistory() != saveHistoryCheckBox.isSelected() ||
               settings.getMaxHistorySize() != (Integer) maxHistorySizeSpinner.getValue() ||
               settings.isShowReasoningContent() != showReasoningContentCheckBox.isSelected();
    }
    
    @Override
    public void apply() throws ConfigurationException {
        PluginSettings settings = PluginSettings.getInstance();
        
        settings.setDeepSeekApiKey(new String(apiKeyField.getPassword()));
        settings.setDeepSeekModel(modelField.getText());
        settings.setTemperature((Double) temperatureSpinner.getValue());
        settings.setMaxTokens((Integer) maxTokensSpinner.getValue());
        settings.setEnableMarkdown(enableMarkdownCheckBox.isSelected());
        settings.setEnableAutoComplete(enableAutoCompleteCheckBox.isSelected());
        settings.setEnableSyntaxHighlight(enableSyntaxHighlightCheckBox.isSelected());
        settings.setSaveHistory(saveHistoryCheckBox.isSelected());
        settings.setMaxHistorySize((Integer) maxHistorySizeSpinner.getValue());
        settings.setShowReasoningContent(showReasoningContentCheckBox.isSelected());
    }
    
    @Override
    public void reset() {
        PluginSettings settings = PluginSettings.getInstance();
        
        apiKeyField.setText(settings.getDeepSeekApiKey());
        modelField.setText(settings.getDeepSeekModel());
        temperatureSpinner.setValue(settings.getTemperature());
        maxTokensSpinner.setValue(settings.getMaxTokens());
        enableMarkdownCheckBox.setSelected(settings.isEnableMarkdown());
        enableAutoCompleteCheckBox.setSelected(settings.isEnableAutoComplete());
        enableSyntaxHighlightCheckBox.setSelected(settings.isEnableSyntaxHighlight());
        saveHistoryCheckBox.setSelected(settings.isSaveHistory());
        maxHistorySizeSpinner.setValue(settings.getMaxHistorySize());
        showReasoningContentCheckBox.setSelected(settings.isShowReasoningContent());
    }
}
