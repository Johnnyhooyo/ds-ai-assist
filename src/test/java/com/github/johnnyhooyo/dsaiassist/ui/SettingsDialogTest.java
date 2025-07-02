package com.github.johnnyhooyo.dsaiassist.ui;

import com.github.johnnyhooyo.dsaiassist.settings.PluginSettings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

/**
 * SettingsDialog 测试类
 */
public class SettingsDialogTest extends BasePlatformTestCase {

    @Test
    public void testSettingsDialogCreation() {
        // 测试设置对话框是否能正常创建
        try {
            SettingsDialog dialog = new SettingsDialog();
            assertNotNull("SettingsDialog should not be null", dialog);
            assertNotNull("Dialog component should not be null", dialog.createCenterPanel());
        } catch (Exception e) {
            fail("Should not throw exception when creating SettingsDialog: " + e.getMessage());
        }
    }

    @Test
    public void testReasoningContentSetting() {
        // 测试推理内容设置的默认值
        PluginSettings settings = PluginSettings.getInstance();
        
        // 默认应该启用推理内容显示
        assertTrue("Reasoning content should be enabled by default", settings.isShowReasoningContent());
        
        // 测试设置修改
        settings.setShowReasoningContent(false);
        assertFalse("Reasoning content should be disabled", settings.isShowReasoningContent());
        
        settings.setShowReasoningContent(true);
        assertTrue("Reasoning content should be enabled", settings.isShowReasoningContent());
    }

    @Test
    public void testSettingsDialogTitle() {
        // 测试设置对话框的标题
        SettingsDialog dialog = new SettingsDialog();
        assertEquals("Dialog title should be correct", "DS AI Assistant 设置", dialog.getTitle());
    }
}
