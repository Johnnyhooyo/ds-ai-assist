package com.github.johnnyhooyo.dsaiassist.ui;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.border.Border;
import java.awt.Color;

/**
 * 主题工具类，用于获取当前IDE主题的颜色和样式
 */
public class ThemeUtils {
    
    /**
     * 检查当前是否为暗色主题
     */
    public static boolean isDarkTheme() {
        return !JBColor.isBright();
    }
    
    /**
     * 获取背景颜色
     */
    public static Color getBackgroundColor() {
        return UIUtil.getPanelBackground();
    }
    
    /**
     * 获取前景色（文本颜色）
     */
    public static Color getForegroundColor() {
        return UIUtil.getLabelForeground();
    }
    
    /**
     * 获取编辑器背景色
     */
    public static Color getEditorBackgroundColor() {
        return UIUtil.getTextFieldBackground();
    }
    
    /**
     * 获取编辑器前景色
     */
    public static Color getEditorForegroundColor() {
        return UIUtil.getTextFieldForeground();
    }
    
    /**
     * 获取用户消息颜色
     */
    public static Color getUserMessageColor() {
        if (isDarkTheme()) {
            return new JBColor(new Color(100, 150, 255), new Color(100, 150, 255)); // 亮蓝色
        } else {
            return new JBColor(new Color(0, 100, 200), new Color(0, 100, 200)); // 深蓝色
        }
    }
    
    /**
     * 获取AI消息颜色
     */
    public static Color getAIMessageColor() {
        return getForegroundColor();
    }
    
    /**
     * 获取时间戳颜色
     */
    public static Color getTimestampColor() {
        if (isDarkTheme()) {
            return new JBColor(new Color(150, 150, 150), new Color(150, 150, 150));
        } else {
            return new JBColor(new Color(120, 120, 120), new Color(120, 120, 120));
        }
    }
    
    /**
     * 获取输入框背景色
     */
    public static Color getInputBackgroundColor() {
        return getEditorBackgroundColor();
    }
    
    /**
     * 获取输入框边框颜色
     */
    public static Color getInputBorderColor() {
        if (isDarkTheme()) {
            return new JBColor(new Color(80, 80, 80), new Color(80, 80, 80));
        } else {
            return new JBColor(new Color(200, 200, 200), new Color(200, 200, 200));
        }
    }
    
    /**
     * 获取浮窗样式的边框
     */
    public static Border getFloatingBorder() {
        Border outer = JBUI.Borders.customLine(getInputBorderColor(), 1);
        Border inner = JBUI.Borders.empty(8, 12);
        Border compound = JBUI.Borders.compound(outer, inner);
        return compound != null ? compound : JBUI.Borders.empty(8, 12);
    }
    
    /**
     * 获取无边框样式
     */
    public static Border getNoBorder() {
        return JBUI.Borders.empty();
    }
    
    /**
     * 获取分隔线颜色
     */
    public static Color getSeparatorColor() {
        if (isDarkTheme()) {
            return new JBColor(new Color(60, 60, 60), new Color(60, 60, 60));
        } else {
            return new JBColor(new Color(230, 230, 230), new Color(230, 230, 230));
        }
    }
    
    /**
     * 获取悬停背景色
     */
    public static Color getHoverBackgroundColor() {
        if (isDarkTheme()) {
            return new JBColor(new Color(70, 70, 70), new Color(70, 70, 70));
        } else {
            return new JBColor(new Color(245, 245, 245), new Color(245, 245, 245));
        }
    }
    
    /**
     * 获取选中背景色
     */
    public static Color getSelectedBackgroundColor() {
        return UIUtil.getListSelectionBackground(true);
    }

    /**
     * 获取代码背景色（用于推理内容等代码块）
     */
    public static Color getCodeBackgroundColor() {
        if (isDarkTheme()) {
            return new JBColor(new Color(40, 40, 40), new Color(40, 40, 40));
        } else {
            return new JBColor(new Color(245, 245, 245), new Color(245, 245, 245));
        }
    }

    /**
     * 获取代码文本色（用于推理内容等代码块）
     */
    public static Color getCodeTextColor() {
        if (isDarkTheme()) {
            return new JBColor(new Color(200, 200, 200), new Color(200, 200, 200));
        } else {
            return new JBColor(new Color(50, 50, 50), new Color(50, 50, 50));
        }
    }
}
