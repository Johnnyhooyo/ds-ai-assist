package com.github.johnnyhooyo.dsaiassist.ui;

import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.application.ApplicationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 主题变化监听器
 * 当IDE主题发生变化时，通知所有注册的组件更新主题
 */
public class ThemeChangeListener {
    
    private static final ThemeChangeListener INSTANCE = new ThemeChangeListener();
    private final List<ThemeAware> themeAwareComponents = new ArrayList<>();
    
    private ThemeChangeListener() {
        // 注册主题变化监听器
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(
            LafManagerListener.TOPIC,
            source -> {
                // 主题变化时通知所有组件
                notifyThemeChanged();
            }
        );
    }
    
    public static ThemeChangeListener getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册需要主题更新的组件
     */
    public void registerComponent(ThemeAware component) {
        themeAwareComponents.add(component);
    }
    
    /**
     * 取消注册组件
     */
    public void unregisterComponent(ThemeAware component) {
        themeAwareComponents.remove(component);
    }
    
    /**
     * 通知所有注册的组件主题已变化
     */
    private void notifyThemeChanged() {
        for (ThemeAware component : themeAwareComponents) {
            try {
                component.updateTheme();
            } catch (Exception e) {
                // 忽略更新失败的组件
                e.printStackTrace();
            }
        }
    }
}

/**
 * 主题感知接口
 * 实现此接口的组件可以在主题变化时自动更新
 */
interface ThemeAware {
    void updateTheme();
}
