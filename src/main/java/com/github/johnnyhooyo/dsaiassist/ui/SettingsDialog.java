package com.github.johnnyhooyo.dsaiassist.ui;

import com.github.johnnyhooyo.dsaiassist.service.DeepSeekService;
import com.github.johnnyhooyo.dsaiassist.settings.PluginSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 设置对话框
 */
public class SettingsDialog extends DialogWrapper {
    
    private JBPasswordField apiKeyField;
    private JBTextField modelField;
    private JSpinner temperatureSpinner;
    private JSpinner maxTokensSpinner;
    private JBCheckBox enableMarkdownCheckBox;
    private JBCheckBox enableAutoCompleteCheckBox;
    private JBCheckBox showReasoningContentCheckBox;
    private JButton testConnectionButton;
    
    public SettingsDialog() {
        super(true);
        setTitle("DS AI Assistant 设置");
        init();
        loadSettings();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        initializeComponents();
        return createMainPanel();
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

        // 聊天设置
        showReasoningContentCheckBox = new JBCheckBox("显示推理过程（DeepSeek Reasoner）");
    }
    
    private JComponent createMainPanel() {
        // API设置面板
        JPanel apiKeyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        apiKeyPanel.add(apiKeyField);
        apiKeyPanel.add(Box.createHorizontalStrut(10));
        apiKeyPanel.add(testConnectionButton);
        
        JPanel apiPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("DeepSeek API设置"), new JPanel())
                .addLabeledComponent("API Key:", apiKeyPanel)
                .addLabeledComponent("模型:", modelField)
                .addLabeledComponent("Temperature:", temperatureSpinner)
                .addLabeledComponent("Max Tokens:", maxTokensSpinner)
                .getPanel();
        
        // 界面设置面板
        JPanel uiPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("界面设置"), new JPanel())
                .addComponent(enableMarkdownCheckBox)
                .addComponent(enableAutoCompleteCheckBox)
                .getPanel();

        // 聊天设置面板
        JPanel chatPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("聊天设置"), new JPanel())
                .addComponent(showReasoningContentCheckBox)
                .getPanel();

        // 主面板
        return FormBuilder.createFormBuilder()
                .addComponent(apiPanel)
                .addVerticalGap(10)
                .addComponent(uiPanel)
                .addVerticalGap(10)
                .addComponent(chatPanel)
                .addComponentFillVertically(new JPanel(), 0)
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
    
    private void loadSettings() {
        PluginSettings settings = PluginSettings.getInstance();

        apiKeyField.setText(settings.getDeepSeekApiKey());
        modelField.setText(settings.getDeepSeekModel());
        temperatureSpinner.setValue(settings.getTemperature());
        maxTokensSpinner.setValue(settings.getMaxTokens());
        enableMarkdownCheckBox.setSelected(settings.isEnableMarkdown());
        enableAutoCompleteCheckBox.setSelected(settings.isEnableAutoComplete());
        showReasoningContentCheckBox.setSelected(settings.isShowReasoningContent());
    }
    
    @Override
    protected void doOKAction() {
        // 保存设置
        PluginSettings settings = PluginSettings.getInstance();

        settings.setDeepSeekApiKey(new String(apiKeyField.getPassword()));
        settings.setDeepSeekModel(modelField.getText());
        settings.setTemperature((Double) temperatureSpinner.getValue());
        settings.setMaxTokens((Integer) maxTokensSpinner.getValue());
        settings.setEnableMarkdown(enableMarkdownCheckBox.isSelected());
        settings.setEnableAutoComplete(enableAutoCompleteCheckBox.isSelected());
        settings.setShowReasoningContent(showReasoningContentCheckBox.isSelected());

        super.doOKAction();
    }
    
    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction()};
    }
}
