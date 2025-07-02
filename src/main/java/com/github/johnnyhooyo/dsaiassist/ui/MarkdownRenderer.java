package com.github.johnnyhooyo.dsaiassist.ui;

import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown渲染器 - 将Markdown文本转换为富文本样式
 */
public class MarkdownRenderer {
    
    // Markdown模式
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.*?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*(.*?)\\*");
    private static final Pattern CODE_INLINE_PATTERN = Pattern.compile("`(.*?)`");
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(.*?)```", Pattern.DOTALL);
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)$", Pattern.MULTILINE);
    private static final Pattern LIST_PATTERN = Pattern.compile("^[\\s]*[-*+]\\s+(.*)$", Pattern.MULTILINE);
    private static final Pattern NUMBERED_LIST_PATTERN = Pattern.compile("^[\\s]*\\d+\\.\\s+(.*)$", Pattern.MULTILINE);
    
    /**
     * 将Markdown文本渲染到StyledDocument
     */
    public static void renderMarkdown(String markdown, StyledDocument doc, boolean isUser) {
        try {
            // 清空文档
            doc.remove(0, doc.getLength());
            
            // 创建基础样式
            Style baseStyle = createBaseStyle(doc, isUser);
            Style boldStyle = createBoldStyle(doc, isUser);
            Style italicStyle = createItalicStyle(doc, isUser);
            Style codeStyle = createCodeStyle(doc, isUser);
            Style headerStyle = createHeaderStyle(doc, isUser);
            Style listStyle = createListStyle(doc, isUser);
            
            // 处理代码块（优先处理，避免被其他格式干扰）
            String processedText = processCodeBlocks(markdown, doc, codeStyle);
            
            // 处理其他格式
            processFormatting(processedText, doc, baseStyle, boldStyle, italicStyle, codeStyle, headerStyle, listStyle);
            
        } catch (BadLocationException e) {
            // 如果渲染失败，回退到纯文本
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, markdown, createBaseStyle(doc, isUser));
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * 处理代码块
     */
    private static String processCodeBlocks(String text, StyledDocument doc, Style codeStyle) {
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        
        while (matcher.find()) {
            // 添加代码块前的文本
            result.append(text, lastEnd, matcher.start());
            
            // 添加代码块标记（稍后处理）
            result.append("__CODE_BLOCK_").append(matcher.group(1)).append("__");
            
            lastEnd = matcher.end();
        }
        
        result.append(text.substring(lastEnd));
        return result.toString();
    }
    
    /**
     * 处理其他格式
     */
    private static void processFormatting(String text, StyledDocument doc, Style baseStyle, 
                                        Style boldStyle, Style italicStyle, Style codeStyle,
                                        Style headerStyle, Style listStyle) throws BadLocationException {
        
        String[] lines = text.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // 处理标题
            Matcher headerMatcher = HEADER_PATTERN.matcher(line);
            if (headerMatcher.matches()) {
                String headerText = headerMatcher.group(2);
                doc.insertString(doc.getLength(), headerText + "\n", headerStyle);
                continue;
            }
            
            // 处理列表
            Matcher listMatcher = LIST_PATTERN.matcher(line);
            Matcher numberedListMatcher = NUMBERED_LIST_PATTERN.matcher(line);
            if (listMatcher.matches()) {
                doc.insertString(doc.getLength(), "• " + listMatcher.group(1) + "\n", listStyle);
                continue;
            } else if (numberedListMatcher.matches()) {
                doc.insertString(doc.getLength(), "• " + numberedListMatcher.group(1) + "\n", listStyle);
                continue;
            }
            
            // 处理代码块标记
            if (line.startsWith("__CODE_BLOCK_")) {
                String codeContent = line.substring(13, line.length() - 2); // 移除标记
                doc.insertString(doc.getLength(), codeContent + "\n", codeStyle);
                continue;
            }
            
            // 处理行内格式
            processInlineFormatting(line, doc, baseStyle, boldStyle, italicStyle, codeStyle);
            
            // 添加换行（除了最后一行）
            if (i < lines.length - 1) {
                doc.insertString(doc.getLength(), "\n", baseStyle);
            }
        }
    }
    
    /**
     * 处理行内格式
     */
    private static void processInlineFormatting(String line, StyledDocument doc, Style baseStyle,
                                              Style boldStyle, Style italicStyle, Style codeStyle) throws BadLocationException {
        
        int pos = 0;
        
        // 简化处理：按顺序查找各种格式
        while (pos < line.length()) {
            int nextBold = findNext(line, pos, BOLD_PATTERN);
            int nextItalic = findNext(line, pos, ITALIC_PATTERN);
            int nextCode = findNext(line, pos, CODE_INLINE_PATTERN);
            
            // 找到最近的格式
            int nextFormat = Math.min(Math.min(nextBold, nextItalic), nextCode);
            
            if (nextFormat == Integer.MAX_VALUE) {
                // 没有更多格式，添加剩余文本
                doc.insertString(doc.getLength(), line.substring(pos), baseStyle);
                break;
            }
            
            // 添加格式前的普通文本
            if (nextFormat > pos) {
                doc.insertString(doc.getLength(), line.substring(pos, nextFormat), baseStyle);
            }
            
            // 处理格式化文本
            if (nextFormat == nextBold) {
                Matcher matcher = BOLD_PATTERN.matcher(line.substring(nextFormat));
                if (matcher.find()) {
                    doc.insertString(doc.getLength(), matcher.group(1), boldStyle);
                    pos = nextFormat + matcher.end();
                }
            } else if (nextFormat == nextItalic) {
                Matcher matcher = ITALIC_PATTERN.matcher(line.substring(nextFormat));
                if (matcher.find()) {
                    doc.insertString(doc.getLength(), matcher.group(1), italicStyle);
                    pos = nextFormat + matcher.end();
                }
            } else if (nextFormat == nextCode) {
                Matcher matcher = CODE_INLINE_PATTERN.matcher(line.substring(nextFormat));
                if (matcher.find()) {
                    doc.insertString(doc.getLength(), matcher.group(1), codeStyle);
                    pos = nextFormat + matcher.end();
                }
            }
        }
    }
    
    /**
     * 查找下一个匹配位置
     */
    private static int findNext(String text, int start, Pattern pattern) {
        Matcher matcher = pattern.matcher(text.substring(start));
        return matcher.find() ? start + matcher.start() : Integer.MAX_VALUE;
    }
    
    // 样式创建方法
    private static Style createBaseStyle(StyledDocument doc, boolean isUser) {
        Style style = doc.addStyle("base", null);
        StyleConstants.setFontFamily(style, Font.SANS_SERIF);
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setForeground(style, isUser ? ThemeUtils.getUserMessageColor() : ThemeUtils.getAIMessageColor());
        return style;
    }
    
    private static Style createBoldStyle(StyledDocument doc, boolean isUser) {
        Style style = doc.addStyle("bold", null);
        StyleConstants.setFontFamily(style, Font.SANS_SERIF);
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setBold(style, true);
        StyleConstants.setForeground(style, isUser ? ThemeUtils.getUserMessageColor() : ThemeUtils.getAIMessageColor());
        return style;
    }
    
    private static Style createItalicStyle(StyledDocument doc, boolean isUser) {
        Style style = doc.addStyle("italic", null);
        StyleConstants.setFontFamily(style, Font.SANS_SERIF);
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setItalic(style, true);
        StyleConstants.setForeground(style, isUser ? ThemeUtils.getUserMessageColor() : ThemeUtils.getAIMessageColor());
        return style;
    }
    
    private static Style createCodeStyle(StyledDocument doc, boolean isUser) {
        Style style = doc.addStyle("code", null);
        StyleConstants.setFontFamily(style, Font.MONOSPACED);
        StyleConstants.setFontSize(style, 11);
        StyleConstants.setBackground(style, ThemeUtils.isDarkTheme() ? 
            new Color(40, 40, 40) : new Color(245, 245, 245));
        StyleConstants.setForeground(style, ThemeUtils.isDarkTheme() ? 
            new Color(200, 200, 200) : new Color(50, 50, 50));
        return style;
    }
    
    private static Style createHeaderStyle(StyledDocument doc, boolean isUser) {
        Style style = doc.addStyle("header", null);
        StyleConstants.setFontFamily(style, Font.SANS_SERIF);
        StyleConstants.setFontSize(style, 14);
        StyleConstants.setBold(style, true);
        StyleConstants.setForeground(style, isUser ? ThemeUtils.getUserMessageColor() : ThemeUtils.getAIMessageColor());
        return style;
    }
    
    private static Style createListStyle(StyledDocument doc, boolean isUser) {
        Style style = doc.addStyle("list", null);
        StyleConstants.setFontFamily(style, Font.SANS_SERIF);
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setLeftIndent(style, 20);
        StyleConstants.setForeground(style, isUser ? ThemeUtils.getUserMessageColor() : ThemeUtils.getAIMessageColor());
        return style;
    }
}
