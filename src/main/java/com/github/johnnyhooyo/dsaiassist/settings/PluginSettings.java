package com.github.johnnyhooyo.dsaiassist.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 插件设置持久化服务
 */
@Service
@State(
    name = "DSAIAssistantSettings",
    storages = @Storage("dsai-assistant.xml")
)
public final class PluginSettings implements PersistentStateComponent<PluginSettings> {
    
    // DeepSeek API配置
    public String deepSeekApiKey = "";
    public String deepSeekModel = "deepseek-chat";
    public double temperature = 0.7;
    public int maxTokens = 2048;
    
    // 界面设置
    public boolean enableMarkdown = true;
    public boolean enableAutoComplete = true;
    public boolean enableSyntaxHighlight = true;
    
    // 聊天设置
    public boolean saveHistory = true;
    public int maxHistorySize = 100;

    // DeepSeek 推理内容设置
    public boolean showReasoningContent = true;
    
    public static PluginSettings getInstance() {
        return ApplicationManager.getApplication().getService(PluginSettings.class);
    }
    
    @Override
    public @NotNull PluginSettings getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull PluginSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    // Getter和Setter方法
    public String getDeepSeekApiKey() {
        return deepSeekApiKey != null ? deepSeekApiKey : "";
    }
    
    public void setDeepSeekApiKey(String apiKey) {
        this.deepSeekApiKey = apiKey != null ? apiKey : "";
    }
    
    public String getDeepSeekModel() {
        return deepSeekModel != null ? deepSeekModel : "deepseek-chat";
    }
    
    public void setDeepSeekModel(String model) {
        this.deepSeekModel = model != null ? model : "deepseek-chat";
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = Math.max(0.0, Math.min(2.0, temperature));
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = Math.max(1, Math.min(4096, maxTokens));
    }
    
    public boolean isEnableMarkdown() {
        return enableMarkdown;
    }
    
    public void setEnableMarkdown(boolean enableMarkdown) {
        this.enableMarkdown = enableMarkdown;
    }
    
    public boolean isEnableAutoComplete() {
        return enableAutoComplete;
    }
    
    public void setEnableAutoComplete(boolean enableAutoComplete) {
        this.enableAutoComplete = enableAutoComplete;
    }
    
    public boolean isEnableSyntaxHighlight() {
        return enableSyntaxHighlight;
    }
    
    public void setEnableSyntaxHighlight(boolean enableSyntaxHighlight) {
        this.enableSyntaxHighlight = enableSyntaxHighlight;
    }
    
    public boolean isSaveHistory() {
        return saveHistory;
    }
    
    public void setSaveHistory(boolean saveHistory) {
        this.saveHistory = saveHistory;
    }
    
    public int getMaxHistorySize() {
        return maxHistorySize;
    }
    
    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = Math.max(1, Math.min(1000, maxHistorySize));
    }

    public boolean isShowReasoningContent() {
        return showReasoningContent;
    }

    public void setShowReasoningContent(boolean showReasoningContent) {
        this.showReasoningContent = showReasoningContent;
    }

    /**
     * 检查DeepSeek配置是否有效
     */
    public boolean isDeepSeekConfigured() {
        return getDeepSeekApiKey() != null && 
               !getDeepSeekApiKey().trim().isEmpty() &&
               getDeepSeekApiKey().trim().startsWith("sk-");
    }
    
    /**
     * 重置为默认设置
     */
    public void resetToDefaults() {
        deepSeekApiKey = "";
        deepSeekModel = "deepseek-chat";
        temperature = 0.7;
        maxTokens = 2048;
        enableMarkdown = true;
        enableAutoComplete = true;
        enableSyntaxHighlight = true;
        saveHistory = true;
        maxHistorySize = 100;
        showReasoningContent = true;
    }
}
