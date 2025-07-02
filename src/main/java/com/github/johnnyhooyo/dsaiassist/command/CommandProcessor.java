package com.github.johnnyhooyo.dsaiassist.command;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 命令处理器 - 处理聊天输入中的特殊命令
 */
public class CommandProcessor {

    private final Project project;
    private final Runnable onClearChat;
    private final Runnable onNewChat;
    private final Consumer<String> onAddAttachment;

    // 支持的命令列表
    private static final List<CommandInfo> COMMANDS = Arrays.asList(
        new CommandInfo("/clear", "清除当前聊天记录"),
        new CommandInfo("/newchat", "创建新的聊天标签页"),
        new CommandInfo("/@", "选择文件或文件夹")
    );

    public CommandProcessor(Project project, Runnable onClearChat, Runnable onNewChat, Consumer<String> onAddAttachment) {
        this.project = project;
        this.onClearChat = onClearChat;
        this.onNewChat = onNewChat;
        this.onAddAttachment = onAddAttachment;
    }
    
    /**
     * 检查输入是否为命令
     */
    public boolean isCommand(String input) {
        return input.trim().startsWith("/");
    }
    
    /**
     * 处理命令
     * @param input 用户输入
     * @return 是否成功处理命令
     */
    public boolean processCommand(String input) {
        String command = input.trim();

        if (command.equals("/clear")) {
            onClearChat.run();
            return true;
        } else if (command.equals("/newchat")) {
            onNewChat.run();
            return true;
        } else if (command.startsWith("/@")) {
            handleFileSelection(command);
            return true;
        }

        return false;
    }
    
    /**
     * 获取命令自动提示
     */
    public List<String> getCommandSuggestions(String input) {
        List<String> suggestions = new ArrayList<>();
        
        if (input.equals("/")) {
            // 显示所有命令
            for (CommandInfo cmd : COMMANDS) {
                suggestions.add(cmd.command + " - " + cmd.description);
            }
        } else if (input.startsWith("/")) {
            // 过滤匹配的命令
            for (CommandInfo cmd : COMMANDS) {
                if (cmd.command.startsWith(input)) {
                    suggestions.add(cmd.command + " - " + cmd.description);
                }
            }
        }
        
        // 如果是文件选择命令，添加文件建议
        if (input.startsWith("/@")) {
            suggestions.addAll(getFileSuggestions(input.substring(2)));
        }
        
        return suggestions;
    }
    
    /**
     * 获取文件建议
     */
    private List<String> getFileSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        
        if (project == null) {
            return suggestions;
        }
        
        try {
            // 搜索项目中的文件
            Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(
                project, 
                "java", 
                GlobalSearchScope.projectScope(project)
            );
            
            // 添加Go文件
            files.addAll(FilenameIndex.getAllFilesByExt(
                project, 
                "go", 
                GlobalSearchScope.projectScope(project)
            ));
            
            // 添加其他常见文件类型
            String[] extensions = {"kt", "js", "ts", "py", "md", "txt", "xml", "json", "yml", "yaml"};
            for (String ext : extensions) {
                files.addAll(FilenameIndex.getAllFilesByExt(
                    project, 
                    ext, 
                    GlobalSearchScope.projectScope(project)
                ));
            }
            
            // 过滤和格式化建议
            for (VirtualFile file : files) {
                String fileName = file.getName();
                String filePath = file.getPath();
                
                if (query.isEmpty() || fileName.toLowerCase().contains(query.toLowerCase())) {
                    // 显示相对路径
                    String relativePath = getRelativePath(filePath);
                    suggestions.add("/@" + relativePath);
                    
                    // 限制建议数量
                    if (suggestions.size() >= 10) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误，返回空建议
        }
        
        return suggestions;
    }
    
    /**
     * 获取相对路径
     */
    private String getRelativePath(String fullPath) {
        if (project == null || project.getBasePath() == null) {
            return fullPath;
        }
        
        String basePath = project.getBasePath();
        if (fullPath.startsWith(basePath)) {
            return fullPath.substring(basePath.length() + 1);
        }
        
        return fullPath;
    }
    
    /**
     * 处理文件选择
     */
    private void handleFileSelection(String command) {
        String filePath = command.substring(2); // 移除 "/@"

        if (project != null && !filePath.isEmpty()) {
            onAddAttachment.accept(filePath);
        }
    }
    
    /**
     * 命令信息类
     */
    private static class CommandInfo {
        final String command;
        final String description;
        
        CommandInfo(String command, String description) {
            this.command = command;
            this.description = description;
        }
    }
}
