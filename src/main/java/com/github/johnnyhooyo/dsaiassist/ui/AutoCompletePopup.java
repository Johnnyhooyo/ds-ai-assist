package com.github.johnnyhooyo.dsaiassist.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * 自动完成弹出窗口
 */
public class AutoCompletePopup extends JWindow {
    
    private final JBList<String> suggestionList;
    private final JBScrollPane scrollPane;
    private final Consumer<String> onSelection;
    private final JComponent parentComponent;
    
    public AutoCompletePopup(JComponent parent, Consumer<String> onSelection) {
        super(SwingUtilities.getWindowAncestor(parent));
        this.parentComponent = parent;
        this.onSelection = onSelection;
        
        suggestionList = new JBList<>();
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setCellRenderer(new SuggestionCellRenderer());
        
        scrollPane = new JBScrollPane(suggestionList);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        scrollPane.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));
        
        setupEventHandlers();
        
        setContentPane(scrollPane);
        setFocusableWindowState(false);
        updateTheme();
    }
    
    private void setupEventHandlers() {
        // 鼠标点击选择
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectCurrentItem();
                }
            }
        });
        
        // 键盘导航
        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        selectCurrentItem();
                        e.consume();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        setVisible(false);
                        e.consume();
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                        // 让列表处理上下键
                        break;
                    default:
                        // 其他键传递给父组件
                        parentComponent.dispatchEvent(e);
                        break;
                }
            }
        });
    }
    
    /**
     * 显示建议列表
     */
    public void showSuggestions(List<String> suggestions, Point location) {
        if (suggestions == null || suggestions.isEmpty()) {
            setVisible(false);
            return;
        }
        
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String suggestion : suggestions) {
            model.addElement(suggestion);
        }
        
        suggestionList.setModel(model);
        suggestionList.setSelectedIndex(0);
        
        // 调整窗口大小
        int itemCount = Math.min(suggestions.size(), 8);
        int itemHeight = suggestionList.getFixedCellHeight();
        if (itemHeight <= 0) {
            itemHeight = 20; // 默认高度
        }
        
        int height = itemCount * itemHeight + 10;
        scrollPane.setPreferredSize(new Dimension(300, height));
        pack();
        
        // 设置位置
        setLocation(location);
        setVisible(true);
        
        // 请求焦点以支持键盘导航
        SwingUtilities.invokeLater(suggestionList::requestFocus);
    }
    
    /**
     * 选择当前项
     */
    private void selectCurrentItem() {
        String selected = suggestionList.getSelectedValue();
        if (selected != null) {
            // 提取命令部分（去掉描述）
            String command = selected.split(" - ")[0];
            onSelection.accept(command);
            setVisible(false);
        }
    }
    
    /**
     * 更新主题
     */
    private void updateTheme() {
        suggestionList.setBackground(ThemeUtils.getBackgroundColor());
        suggestionList.setForeground(ThemeUtils.getForegroundColor());
        suggestionList.setSelectionBackground(ThemeUtils.getSelectedBackgroundColor());
        suggestionList.setSelectionForeground(ThemeUtils.getForegroundColor());
        
        scrollPane.setBackground(ThemeUtils.getBackgroundColor());
        scrollPane.getViewport().setBackground(ThemeUtils.getBackgroundColor());
    }
    
    /**
     * 检查是否可见
     */
    public boolean isPopupVisible() {
        return isVisible();
    }
    
    /**
     * 处理上下键导航
     */
    public boolean handleKeyNavigation(KeyEvent e) {
        if (!isVisible()) {
            return false;
        }
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                int currentIndex = suggestionList.getSelectedIndex();
                if (currentIndex > 0) {
                    suggestionList.setSelectedIndex(currentIndex - 1);
                }
                return true;
            case KeyEvent.VK_DOWN:
                int current = suggestionList.getSelectedIndex();
                if (current < suggestionList.getModel().getSize() - 1) {
                    suggestionList.setSelectedIndex(current + 1);
                }
                return true;
            case KeyEvent.VK_ENTER:
                selectCurrentItem();
                return true;
            case KeyEvent.VK_ESCAPE:
                setVisible(false);
                return true;
        }
        
        return false;
    }
    
    /**
     * 自定义单元格渲染器
     */
    private static class SuggestionCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            String text = value.toString();
            if (text.contains(" - ")) {
                String[] parts = text.split(" - ", 2);
                String command = parts[0];
                String description = parts[1];
                
                setText("<html><b>" + command + "</b> - <i>" + description + "</i></html>");
            }
            
            return this;
        }
    }
}
